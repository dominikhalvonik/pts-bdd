package sk.testlab.cvicenie07.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import sk.testlab.cvicenie07.support.ScenarioContext;

/** Hooks zdieľajú iba kontext aktuálneho scenára, nie statický driver alebo token. */
public final class ScenarioHooks {
    private final ScenarioContext context;

    public ScenarioHooks(ScenarioContext context) { this.context = context; }

    @Before
    public void before(Scenario scenario) {
        scenario.log("Začína scenár: " + scenario.getName());
        context.start();
    }

    @After
    public void after(Scenario scenario) {
        boolean alreadyFailed = scenario.isFailed();
        try {
            scenario.attach(context.summary(), "text/plain", "Vstupy a skutočný výsledok");
            if (alreadyFailed) saveFailure(scenario);
        } finally {
            try {
                context.close();
            } catch (RuntimeException | AssertionError cleanupError) {
                if (alreadyFailed) {
                    // Pôvodné zlyhanie zachováme, problém s uprataním je v reporte samostatne.
                    scenario.log("Upratanie relácie zlyhalo: " + cleanupError.getClass().getSimpleName());
                } else {
                    throw new IllegalStateException("Scenár overil cenu, ale upratanie relácie zlyhalo.", cleanupError);
                }
            }
        }
    }

    private void saveFailure(Scenario scenario) {
        String diagnostic;
        try {
            diagnostic = context.diagnostics();
        } catch (RuntimeException e) {
            diagnostic = context.summary() + "\nDiagnostika prehliadača nie je dostupná.";
        }
        scenario.attach(diagnostic, "text/plain", "Diagnostika zlyhania");
        Optional<byte[]> screenshot = Optional.empty();
        try {
            screenshot = context.screenshot();
        } catch (RuntimeException e) {
            scenario.log("Screenshot nie je dostupný: " + e.getClass().getSimpleName());
        }
        if (screenshot.isPresent()) scenario.attach(screenshot.get(), "image/png", "Stav GUI pri zlyhaní");
        Path folder = Path.of("target", "artifacts", "scenario-" + UUID.randomUUID());
        try {
            Files.createDirectories(folder);
            Files.writeString(folder.resolve("diagnostika.txt"),
                "Scenár: " + scenario.getName() + "\n" + diagnostic, StandardCharsets.UTF_8);
            if (screenshot.isPresent()) Files.write(folder.resolve("failure.png"), screenshot.get());
            scenario.log("Artefakty: " + folder.toAbsolutePath());
        } catch (IOException e) {
            scenario.log("Artefakty sa nepodarilo zapísať; diagnostika je priložená v Cucumber reporte.");
        }
    }
}
