package sk.testlab.cvicenie07;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/** Spúšťajte túto triedu ako JUnit test alebo Maven goal test. */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class RunCucumberTest {
    // Konfigurácia krokov, reportov a sekvenčného behu: junit-platform.properties.
}
