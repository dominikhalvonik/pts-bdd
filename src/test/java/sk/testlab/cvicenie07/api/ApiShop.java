package sk.testlab.cvicenie07.api;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import sk.testlab.cvicenie07.domain.CartItem;
import sk.testlab.cvicenie07.domain.Delivery;
import sk.testlab.cvicenie07.domain.PriceQuote;
import sk.testlab.cvicenie07.support.ShopPort;
import sk.testlab.cvicenie07.support.TestConfig;

/** API adaptér: query parameter route, bearer identita a žiadne globálne RestAssured nastavenia. */
public final class ApiShop implements ShopPort {
    private final TestConfig config;
    private final Gson gson = new Gson();
    private final String endpoint;
    private String token;
    private String workspace = "nezistený";
    private String lastOperation = "API ešte nebolo zavolané";
    private boolean quoted;

    public ApiShop(TestConfig config) {
        this.config = config;
        this.endpoint = URI.create(config.baseUrl()).resolve("api/index.php").toString();
    }

    @Override public void start() {
        Response login = post("/auth/token", Map.of("email", config.email(), "password", config.password()));
        requireSuccess(login, 201, "/auth/token");
        token = login.jsonPath().getString("data.token");
        if (token == null || !token.matches("[a-fA-F0-9]{64}")) {
            throw new AssertionError("/auth/token nevrátil očakávaný bearer token. Hodnota sa neloguje.");
        }
        // /auth/token neobsahuje profil používateľa. Ten načítame samostatne.
        Response me = get("/auth/me");
        requireSuccess(me, 200, "/auth/me");
        String email = me.jsonPath().getString("data.user.email");
        if (email == null || !email.equalsIgnoreCase(config.email())) {
            throw new AssertionError("Bearer token neidentifikuje zadaný testovací účet.");
        }
        workspace = me.jsonPath().getString("data.user.workspace_code");
    }

    @Override public PriceQuote calculate(List<CartItem> items, String coupon, Delivery delivery) {
        if (token == null) throw new IllegalStateException("Najprv zavolajte start().");
        if (quoted) throw new IllegalStateException("Scenár má mať jedno vyhodnotenie ceny.");
        quoted = true;
        List<Map<String, Object>> lines = items.stream()
            .map(item -> Map.<String, Object>of("sku", item.sku(), "quantity", item.quantity()))
            .toList();
        Map<String, Object> body = Map.of("items", lines, "coupon_code", coupon,
            "delivery_method", delivery.apiValue());
        Response response = post("/cart/quote", body);
        requireSuccess(response, 200, "/cart/quote");
        if (!"EUR".equals(response.jsonPath().getString("data.currency"))) {
            throw new AssertionError("Cenová ponuka musí používať menu EUR.");
        }
        if (!delivery.apiValue().equals(response.jsonPath().getString("data.delivery_method"))) {
            throw new AssertionError("Cenová ponuka sa netýka zvoleného doručenia.");
        }
        verifyItems(response, items);
        return new PriceQuote(integer(response, "data.subtotal_cents"),
            integer(response, "data.discount_cents"), integer(response, "data.shipping_cents"),
            integer(response, "data.total_cents"));
    }

    private void verifyItems(Response response, List<CartItem> expected) {
        List<Map<String, Object>> actual = response.jsonPath().getList("data.items");
        if (actual == null || actual.size() != expected.size()) {
            throw new AssertionError("API vrátilo iný počet položiek košíka.");
        }
        Map<String, Integer> expectedQuantities = new LinkedHashMap<>();
        for (CartItem item : expected) expectedQuantities.put(item.sku(), item.quantity());
        Map<String, Integer> actualQuantities = new LinkedHashMap<>();
        for (Map<String, Object> line : actual) {
            if (!(line.get("sku") instanceof String sku)) {
                throw new AssertionError("Položka cenovej ponuky nemá platné SKU.");
            }
            int quantity = integerValue(line.get("quantity"), "items.quantity");
            if (actualQuantities.put(sku, quantity) != null) {
                throw new AssertionError("API vrátilo duplicitné SKU: " + sku);
            }
        }
        if (!expectedQuantities.equals(actualQuantities)) {
            throw new AssertionError("API vrátilo iné položky/množstvá. Očakávané: "
                + expectedQuantities + "; skutočné: " + actualQuantities);
        }
    }

    private RequestSpecification request(String route) {
        int ms = Math.toIntExact(config.timeout().toMillis());
        RestAssuredConfig options = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", ms)
                .setParam("http.socket.timeout", ms)
                .setParam("http.connection-manager.timeout", (long) ms))
            .redirect(RedirectConfig.redirectConfig().followRedirects(false));
        RequestSpecification request = RestAssured.given().config(options)
            .accept(ContentType.JSON).contentType(ContentType.JSON).queryParam("route", route);
        if (token != null) request.header("Authorization", "Bearer " + token);
        return request;
    }

    private Response post(String route, Object body) {
        lastOperation = "POST " + route + " (odosielanie)";
        // Bez log().all(): login telo a Authorization nesmú skončiť v reporte.
        Response response = request(route).body(gson.toJson(body)).post(endpoint);
        lastOperation = "POST " + route + " -> HTTP " + response.statusCode();
        return response;
    }

    private Response get(String route) {
        lastOperation = "GET " + route + " (odosielanie)";
        Response response = request(route).get(endpoint);
        lastOperation = "GET " + route + " -> HTTP " + response.statusCode();
        return response;
    }

    private void requireSuccess(Response response, int status, String route) {
        if (response.statusCode() != status) {
            String code = "bez JSON kódu";
            try {
                String value = response.jsonPath().getString("error.code");
                if (value != null && value.matches("[A-Z0-9_]{1,80}")) code = value;
            } catch (RuntimeException ignored) {
                // Napríklad HTML chybová stránka hostingu; nevypisujeme celý obsah.
            }
            throw new AssertionError("API " + route + ": očakávaný HTTP " + status
                + ", skutočný HTTP " + response.statusCode() + ", error.code=" + code);
        }
        String contentType = response.contentType();
        if (contentType == null || !contentType.toLowerCase(java.util.Locale.ROOT).startsWith("application/json")) {
            throw new AssertionError("API " + route + " nevrátilo application/json. Skontrolujte base.url.");
        }
    }

    private int integer(Response response, String path) {
        return integerValue(response.jsonPath().get(path), path);
    }

    private int integerValue(Object value, String path) {
        if (!(value instanceof Integer) && !(value instanceof Long)) {
            throw new AssertionError("API pole " + path + " musí byť celé JSON číslo, nie text ani desatinné číslo.");
        }
        return Math.toIntExact(((Number) value).longValue());
    }

    @Override public Optional<byte[]> screenshot() { return Optional.empty(); }

    @Override public String diagnostics() {
        return "Vrstva: api\nPracovný priestor: " + workspace + "\nPosledná operácia: " + lastOperation;
    }

    @Override public void close() {
        if (token == null) return;
        try {
            Response logout = post("/auth/logout", Map.of());
            requireSuccess(logout, 200, "/auth/logout");
        } finally {
            token = null;
        }
    }
}
