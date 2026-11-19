package sk.testlab.cvicenie07.support;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;

/** Priorita: JVM -D, environment, UTF-8 súbor. Heslo sa netrimuje a neloguje. */
public record TestConfig(String baseUrl, String email, String password,
        String browser, boolean headless, Duration timeout,
        String driverPath, String browserBinary, String layer) {

    public static TestConfig load() {
        Path file = Path.of(System.getProperty("test.config", "config/test.properties"));
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Nedá sa načítať " + file.toAbsolutePath()
                + ". Nastavte pracovný priečinok na koreň projektu s pom.xml.", e);
        }
        String url = value(p, "base.url", "BASE_URL", "http://127.0.0.1:8080/");
        URI uri = URI.create(url);
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("base.url musí byť HTTP(S) koreň webu bez query, # a prihlasovacích údajov.");
        }
        if (!url.endsWith("/")) url += "/";
        String email = value(p, "test.email", "TEST_EMAIL", "");
        String password = raw(p, "test.password", "TEST_PASSWORD", "");
        if (email.isBlank() || !email.contains("@") || email.startsWith("SEM_")
                || password.isBlank() || password.startsWith("SEM_")) {
            throw new IllegalArgumentException("Vyplňte test.email a test.password v config/test.properties.");
        }
        String layer = value(p, "test.layer", "TEST_LAYER", "gui").toLowerCase(Locale.ROOT);
        if (!layer.equals("gui") && !layer.equals("api")) {
            throw new IllegalArgumentException("test.layer musí byť gui alebo api.");
        }
        String browser = value(p, "browser", "BROWSER", "chrome").toLowerCase(Locale.ROOT);
        if (!browser.equals("chrome") && !browser.equals("edge") && !browser.equals("firefox")) {
            throw new IllegalArgumentException("browser musí byť chrome, edge alebo firefox.");
        }
        int seconds;
        try {
            seconds = Integer.parseInt(value(p, "timeout.seconds", "TIMEOUT_SECONDS", "30"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("timeout.seconds musí byť celé číslo od 1 do 120.", e);
        }
        if (seconds < 1 || seconds > 120) {
            throw new IllegalArgumentException("timeout.seconds musí byť od 1 do 120.");
        }
        String headless = value(p, "headless", "HEADLESS", "false");
        if (!headless.equalsIgnoreCase("true") && !headless.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("headless musí byť true alebo false.");
        }
        return new TestConfig(url, email, password, browser, Boolean.parseBoolean(headless),
            Duration.ofSeconds(seconds), value(p, "driver.path", "DRIVER_PATH", ""),
            value(p, "browser.binary", "BROWSER_BINARY", ""), layer);
    }

    private static String value(Properties p, String key, String env, String fallback) {
        return raw(p, key, env, fallback).trim();
    }

    private static String raw(Properties p, String key, String env, String fallback) {
        String system = System.getProperty(key);
        if (system != null) return system;
        String environment = System.getenv(env);
        return environment != null ? environment : p.getProperty(key, fallback);
    }

    @Override public String toString() {
        return "TestConfig[layer=" + layer + ", browser=" + browser + ", credentials=REDACTED]";
    }
}
