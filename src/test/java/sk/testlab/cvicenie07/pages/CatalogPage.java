package sk.testlab.cvicenie07.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public final class CatalogPage extends BasePage {
    private static final By READY = testId("catalog-ready");
    private static final By USER = testId("current-user");
    private static final By COUNT = testId("cart-count");

    public CatalogPage(WebDriver driver, Duration timeout) {
        super(driver, timeout);
        wait.withMessage("Po prihlásení sa nenačítal katalóg.").until(d -> {
            failOnVisibleErrors("startup-error", "login-error", "catalog-error", "catalog-empty");
            return isVisible(USER) && isVisible(READY);
        });
    }

    public int cartCount() {
        return Integer.parseInt(visible(COUNT).getText().trim());
    }

    public void requireEmptyCart() {
        if (cartCount() != 0) {
            throw new AssertionError("Scenár musí začínať s prázdnym košíkom v novej relácii prehliadača.");
        }
    }

    public void add(String sku, int quantity) {
        if (quantity < 1 || quantity > 20) {
            throw new IllegalArgumentException("Množstvo musí byť 1 až 20.");
        }
        int stock = intAttribute(testId("stock-" + sku), "data-stock");
        if (stock < quantity) {
            throw new AssertionError("Testovacie dáta: " + sku + " má sklad " + stock
                + ", scenár potrebuje " + quantity + ". Použite pripravený fixture účet.");
        }
        for (int i = 0; i < quantity; i++) {
            int before = cartCount();
            click(testId("add-to-cart-" + sku));
            wait.withMessage("Po pridaní " + sku + " sa nezmenil počet kusov v košíku.")
                .until(ExpectedConditions.textToBe(COUNT, Integer.toString(before + 1)));
        }
    }

    public CartPage openCart() {
        click(testId("nav-cart"));
        return new CartPage(driver, timeout);
    }
}
