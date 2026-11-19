package sk.testlab.cvicenie07.pages;

import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import sk.testlab.cvicenie07.domain.Delivery;
import sk.testlab.cvicenie07.domain.PriceQuote;

public final class CartPage extends BasePage {
    private static final By DELIVERY = testId("delivery-method");
    private static final By COUPON = testId("coupon-input");
    private static final By TOTAL = testId("quote-total");
    private static final By LOADING = testId("quote-loading");

    public CartPage(WebDriver driver, Duration timeout) {
        super(driver, timeout);
        visible(DELIVERY);
        awaitQuote();
    }

    public void useCoupon(String coupon) {
        // Prázdny kupón v novom košíku už znamená „bez zľavy“.
        if (coupon.isEmpty() && visible(COUPON).getDomProperty("value").isEmpty()) return;
        type(COUPON, coupon);
        WebElement previous = visible(TOTAL);
        click(testId("coupon-apply"));
        awaitReplacement(previous);
    }

    public void chooseDelivery(Delivery delivery) {
        Select select = new Select(visible(DELIVERY));
        String selected = select.getFirstSelectedOption().getDomAttribute("value");
        if (delivery.apiValue().equals(selected)) return;
        WebElement previous = visible(TOTAL);
        select.selectByValue(delivery.apiValue());
        awaitReplacement(previous);
    }

    public int quantity(String sku) {
        return Integer.parseInt(visible(testId("quantity-" + sku)).getDomProperty("value"));
    }

    private void awaitReplacement(WebElement previous) {
        // Vue pri prepočte zruší starý súhrn a po odpovedi vytvorí nový.
        // Čakáme na dokončenie operácie, nikdy na očakávanú business cenu.
        wait.withMessage("Pri prepočte nebol nahradený predchádzajúci cenový súhrn.")
            .until(ExpectedConditions.stalenessOf(previous));
        awaitQuote();
    }

    private void awaitQuote() {
        wait.withMessage("Cenová ponuka sa nedokončila.").until(d -> {
            failOnVisibleErrors("startup-error", "quote-error");
            return !isVisible(LOADING) && isVisible(TOTAL);
        });
    }

    public PriceQuote readQuote() {
        awaitQuote();
        int subtotal = readMoney("quote-subtotal", false);
        int discount = readMoney("quote-discount", true);
        int shipping = readMoney("quote-shipping", false);
        int total = readMoney("quote-total", false);
        return new PriceQuote(subtotal, discount, shipping, total);
    }

    private int readMoney(String id, boolean negativePrefix) {
        WebElement element = visible(testId(id));
        String raw = element.getDomAttribute("data-cents");
        if (raw == null || !raw.matches("[0-9]+")) {
            throw new AssertionError("Neplatná suma v atribúte data-cents: " + id);
        }
        int cents = Integer.parseInt(raw);
        String actualText = normalizedMoneyText(element.getText());
        String expectedText = (negativePrefix ? "−" : "")
            + (cents / 100) + "," + String.format(Locale.ROOT, "%02d", cents % 100) + "€";
        // Kontrola konzistencie viditeľného textu a atribútu. Business očakávania
        // sú samostatne vo .feature a porovnajú sa v step definitions.
        if (!expectedText.equals(actualText)) {
            throw new AssertionError("GUI text a data-cents sa nezhodujú pre " + id
                + ": text='" + element.getText() + "', data-cents=" + cents);
        }
        return cents;
    }

    private static String normalizedMoneyText(String value) {
        return value.replace("\u00a0", "").replace("\u202f", "")
            .replace(" ", "").replace("\n", "").replace("\r", "").trim();
    }
}
