package sk.testlab.cvicenie07.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import sk.testlab.cvicenie07.api.ApiShop;
import sk.testlab.cvicenie07.domain.CartItem;
import sk.testlab.cvicenie07.domain.Delivery;
import sk.testlab.cvicenie07.domain.PriceQuote;
import sk.testlab.cvicenie07.gui.GuiShop;

/** PicoContainer vytvorí nový kontext pre KAŽDÝ scenár, vrátane každého riadku Examples. */
public final class ScenarioContext {
    private final List<CartItem> items = new ArrayList<>();
    private String coupon = "";
    private Delivery delivery;
    private PriceQuote quote;
    private boolean requested;
    private ShopPort shop;
    private TestConfig config;

    public void start() {
        config = TestConfig.load();
        shop = config.layer().equals("api") ? new ApiShop(config) : new GuiShop(config);
        // Inštancia už existuje aj pri zlyhaní prihlásenia: @After ju dokáže zatvoriť.
        shop.start();
    }

    public void addItem(String sku, int quantity) {
        requirePreparation();
        if (items.stream().anyMatch(item -> item.sku().equals(sku))) {
            throw new IllegalArgumentException("SKU sa má v scenári uviesť iba raz: " + sku);
        }
        items.add(new CartItem(sku, quantity));
    }

    public void coupon(String value) {
        requirePreparation();
        coupon = value;
    }

    public void calculate(Delivery delivery) {
        requirePreparation();
        if (items.isEmpty()) throw new IllegalStateException("Given musí pripraviť aspoň jednu položku.");
        requested = true;
        this.delivery = delivery;
        quote = shop.calculate(List.copyOf(items), coupon, delivery);
    }

    public PriceQuote actualQuote() {
        if (quote == null) throw new IllegalStateException("Chýba výsledok When kroku s vyhodnotením ceny.");
        return quote;
    }

    private void requirePreparation() {
        if (requested) throw new IllegalStateException("Scenár má jedno When. Given vstupy musia byť pred ním.");
    }

    public String summary() {
        return "Vrstva: " + (config == null ? "nenačítaná" : config.layer())
            + "\nKošík: " + items + "\nKupón: " + (coupon.isEmpty() ? "bez kupónu" : coupon)
            + "\nDoručenie: " + delivery + "\nSkutočný výsledok: " + quote;
    }

    public String diagnostics() {
        return summary() + "\n" + (shop == null ? "Relácia ešte nevznikla." : shop.diagnostics());
    }

    public Optional<byte[]> screenshot() {
        return shop == null ? Optional.empty() : shop.screenshot();
    }

    public void close() {
        if (shop != null) shop.close();
    }
}
