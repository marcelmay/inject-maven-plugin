package de.m3y.maven.inject.it;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InjectionTest {
    @Test
    public void testVersion() {
        String expectedVersion = System.getProperty("expected.version");
        String expectedBranch = System.getProperty("expected.branch");
        String expectedBuildTime = System.getProperty("expected.build.time");
        String expectedScmVersion = System.getProperty("expected.scm.version");

        // Constants
        assertEquals(expectedVersion, ExampleInjection.VERSION);
        assertEquals("To be replaced", ExampleInjection.VERSION2);
        // Methods
        assertEquals(expectedVersion, new ExampleInjection().getVersionAsString());
        assertEquals(expectedVersion, ExampleInjection.getVersionAsStringStatic());
        // Inner class
        assertEquals(expectedVersion, ExampleInjection.InnerClass.VERSION);
        assertEquals(expectedVersion, new ExampleInjection.InnerClass().getVersionAsString());
        assertEquals(expectedVersion, ExampleInjection.InnerClass.getVersionAsStringStatic());
        // Build time
        assertEquals("", // Last initialization wins, which is the original one
                new ExampleInjection().buildTimeDefault);

        assertEquals("To be replaced", // Last initialization wins, which is the original one
                new ExampleInjection().buildTimePublic);
        assertEquals("To be replaced", // Last initialization wins, which is the original one
                new ExampleInjection().buildTimePublicFinal);
        assertEquals(expectedBuildTime, new ExampleInjection().getBuildTime());
        // Build branch
        assertEquals(expectedBranch, new ExampleInjection().getScmBranch());
        // SCM Version
        assertEquals(expectedScmVersion, new ExampleInjection().getScmVersion());

        assertEquals(expectedVersion, BuildInfo.INSTANCE.getProjectVersion());
    }
}
