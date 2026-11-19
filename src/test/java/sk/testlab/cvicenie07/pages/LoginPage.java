package sk.testlab.cvicenie07.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class LoginPage extends BasePage {
    private static final By EMAIL = testId("login-email");
    private static final By PASSWORD = testId("login-password");
    private static final By SUBMIT = testId("login-submit");

    private LoginPage(WebDriver driver, Duration timeout) {
        super(driver, timeout);
        visible(EMAIL);
    }

    public static LoginPage open(WebDriver driver, Duration timeout, String baseUrl) {
        driver.get(baseUrl + "#/login");
        return new LoginPage(driver, timeout);
    }

    public CatalogPage loginAs(String email, String password) {
        type(EMAIL, email);
        type(PASSWORD, password);
        click(SUBMIT);
        return new CatalogPage(driver, timeout);
    }
}
