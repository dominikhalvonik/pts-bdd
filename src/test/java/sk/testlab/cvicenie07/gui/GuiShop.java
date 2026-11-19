package sk.testlab.cvicenie07.gui;

import java.util.List;
import java.util.Optional;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import sk.testlab.cvicenie07.domain.CartItem;
import sk.testlab.cvicenie07.domain.Delivery;
import sk.testlab.cvicenie07.domain.PriceQuote;
import sk.testlab.cvicenie07.pages.CartPage;
import sk.testlab.cvicenie07.pages.CatalogPage;
import sk.testlab.cvicenie07.pages.LoginPage;
import sk.testlab.cvicenie07.support.BrowserFactory;
import sk.testlab.cvicenie07.support.ShopPort;
import sk.testlab.cvicenie07.support.TestConfig;

/** GUI adaptér: rovnaký business scenár obslúži cez Page Objecty. */
public final class GuiShop implements ShopPort {
    private final TestConfig config;
    private WebDriver driver;
    private CatalogPage catalog;
    private boolean calculated;

    public GuiShop(TestConfig config) {
        this.config = config;
    }

    @Override
    public void start() {
        if (driver != null) throw new IllegalStateException("GUI relácia už beží.");
        driver = BrowserFactory.create(config);
        // Driver ostáva dostupný pre screenshot a close() aj pri chybe prihlásenia.
        catalog = LoginPage.open(driver, config.timeout(), config.baseUrl())
            .loginAs(config.email(), config.password());
        catalog.requireEmptyCart();
    }

    @Override
    public PriceQuote calculate(List<CartItem> items, String coupon, Delivery delivery) {
        if (catalog == null) throw new IllegalStateException("Najprv spustite GUI reláciu.");
        if (calculated) throw new IllegalStateException("Jeden scenár má jedno cenové rozhodnutie.");
        if (items.isEmpty()) throw new IllegalArgumentException("Košík nesmie byť prázdny.");
        calculated = true;
        for (CartItem item : items) catalog.add(item.sku(), item.quantity());
        CartPage cart = catalog.openCart();
        for (CartItem item : items) {
            int actual = cart.quantity(item.sku());
            if (actual != item.quantity()) {
                throw new AssertionError("Košík obsahuje iné množstvo " + item.sku()
                    + ": očakávané=" + item.quantity() + ", skutočné=" + actual);
            }
        }
        cart.useCoupon(coupon);
        cart.chooseDelivery(delivery);
        return cart.readQuote();
    }

    @Override
    public Optional<byte[]> screenshot() {
        return driver instanceof TakesScreenshot camera
            ? Optional.of(camera.getScreenshotAs(OutputType.BYTES)) : Optional.empty();
    }

    @Override
    public String diagnostics() {
        if (driver == null) return "GUI relácia nevznikla.";
        return "Vrstva: GUI\nPrehliadač: " + config.browser()
            + "\nURL: " + driver.getCurrentUrl() + "\nTitulok: " + driver.getTitle();
    }

    @Override
    public void close() {
        if (driver != null) {
            WebDriver active = driver;
            driver = null;
            catalog = null;
            active.quit();
        }
    }
}
