package sk.testlab.cvicenie07.support;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

/** Každé volanie vytvorí vlastný dočasný profil, bez zdieľanej session. */
public final class BrowserFactory {
    private BrowserFactory() {}

    public static WebDriver create(TestConfig config) {
        File executable = executable(config.driverPath());
        if (!config.browserBinary().isBlank() && !Files.isRegularFile(Path.of(config.browserBinary()))) {
            throw new IllegalArgumentException("browser.binary neukazuje na existujúci súbor.");
        }
        WebDriver driver = switch (config.browser()) {
            case "chrome" -> chrome(config, executable);
            case "edge" -> edge(config, executable);
            case "firefox" -> firefox(config, executable);
            default -> throw new IllegalArgumentException("Podporované prehliadače: chrome, edge, firefox.");
        };
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            driver.manage().timeouts().pageLoadTimeout(config.timeout());
            driver.manage().timeouts().scriptTimeout(config.timeout());
            driver.manage().window().setSize(new Dimension(1440, 1000));
            return driver;
        } catch (RuntimeException error) {
            try { driver.quit(); } catch (RuntimeException ignored) { /* Pôvodná príčina má prednosť. */ }
            throw error;
        }
    }

    private static File executable(String path) {
        if (path.isBlank()) return null; // Selenium Manager vyberie ovládač.
        Path file = Path.of(path);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("driver.path neukazuje na existujúci súbor.");
        }
        return file.toFile();
    }

    private static WebDriver chrome(TestConfig config, File executable) {
        ChromeOptions options = new ChromeOptions();
        if (config.headless()) options.addArguments("--headless=new");
        if (!config.browserBinary().isBlank()) options.setBinary(config.browserBinary());
        return executable == null ? new ChromeDriver(options)
            : new ChromeDriver(new ChromeDriverService.Builder()
                .usingDriverExecutable(executable).build(), options);
    }

    private static WebDriver edge(TestConfig config, File executable) {
        EdgeOptions options = new EdgeOptions();
        if (config.headless()) options.addArguments("--headless=new");
        if (!config.browserBinary().isBlank()) options.setBinary(config.browserBinary());
        return executable == null ? new EdgeDriver(options)
            : new EdgeDriver(new EdgeDriverService.Builder()
                .usingDriverExecutable(executable).build(), options);
    }

    private static WebDriver firefox(TestConfig config, File executable) {
        FirefoxOptions options = new FirefoxOptions();
        if (config.headless()) options.addArguments("-headless");
        if (!config.browserBinary().isBlank()) options.setBinary(config.browserBinary());
        return executable == null ? new FirefoxDriver(options)
            : new FirefoxDriver(new GeckoDriverService.Builder()
                .usingDriverExecutable(executable).build(), options);
    }
}
