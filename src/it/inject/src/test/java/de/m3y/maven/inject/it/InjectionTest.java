package de.m3y.maven.inject.it;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class InjectionTest {
    @Test
    public void testVersion() {
        String expectedVersion = System.getProperty("expected.version");
        String expectedBranch = System.getProperty("expected.branch");
        String expectedBuildTime = System.getProperty("expected.build.time");
        String expectedScmVersion = System.getProperty("expected.scm.version");

        // Constants
        assertEquals(ExampleInjection.VERSION, expectedVersion);
        assertEquals(ExampleInjection.VERSION2, "To be replaced");
        // Methods
        assertEquals(new ExampleInjection().getVersionAsString(), expectedVersion);
        assertEquals(ExampleInjection.getVersionAsStringStatic(), expectedVersion);
        // Inner class
        assertEquals(ExampleInjection.InnerClass.VERSION, expectedVersion);
        assertEquals(new ExampleInjection.InnerClass().getVersionAsString(), expectedVersion);
        assertEquals(ExampleInjection.InnerClass.getVersionAsStringStatic(), expectedVersion);
        // Build time
        assertEquals(new ExampleInjection().buildTimeDefault, ""); // Last initialization wins, which is the original one
        assertEquals(new ExampleInjection().buildTimePublic, "To be replaced"); // Last initialization wins, which is the original one
        assertEquals(new ExampleInjection().buildTimePublicFinal, "To be replaced"); // Last initialization wins, which is the original one
        assertEquals(new ExampleInjection().getBuildTime(), expectedBuildTime);
        // Build branch
        assertEquals( new ExampleInjection().getScmBranch(), expectedBranch);
        // SCM Version
        assertEquals( new ExampleInjection().getScmVersion(), expectedScmVersion);

        assertEquals(BuildInfo.INSTANCE.getProjectVersion(), expectedVersion);
    }
}
