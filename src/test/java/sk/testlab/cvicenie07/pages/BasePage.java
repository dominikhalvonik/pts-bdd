package sk.testlab.cvicenie07.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Spoločné operácie nad stabilnými data-testid selektormi. */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final Duration timeout;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.timeout = timeout;
        this.wait = new WebDriverWait(driver, timeout);
        wait.pollingEvery(Duration.ofMillis(150));
        wait.ignoring(StaleElementReferenceException.class);
    }

    protected static By testId(String id) {
        if (!id.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Nepovolený data-testid: " + id);
        }
        return By.cssSelector("[data-testid='" + id + "']");
    }

    protected WebElement visible(By locator) {
        return wait.withMessage("Element sa nezobrazil: " + locator).until(d -> {
            failOnVisibleErrors("startup-error");
            return ExpectedConditions.visibilityOfElementLocated(locator).apply(d);
        });
    }

    protected void click(By locator) {
        WebElement element = wait.withMessage("Element nie je klikateľný: " + locator)
            .until(ExpectedConditions.elementToBeClickable(locator));
        // Akcia je mimo pollingu. Zápisový krok sa pri čakaní neopakuje.
        element.click();
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected int intAttribute(By locator, String name) {
        String raw = visible(locator).getDomAttribute(name);
        if (raw == null || !raw.matches("[0-9]+")) {
            throw new AssertionError("Chýba celočíselný atribút " + name + " na " + locator);
        }
        return Integer.parseInt(raw);
    }

    protected boolean isVisible(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    /** Chyba aplikácie sa hlási priamo, nezmení sa iba na neurčitý timeout. */
    protected void failOnVisibleErrors(String... ids) {
        for (String id : ids) {
            List<WebElement> elements = driver.findElements(testId(id));
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    throw new AssertionError("TestLab Shop [" + id + "]: " + element.getText());
                }
            }
        }
    }
}
