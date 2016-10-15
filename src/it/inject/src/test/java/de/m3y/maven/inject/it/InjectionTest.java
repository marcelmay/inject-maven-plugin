package de.m3y.maven.inject.it;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class InjectionTest {
    @Test
    public void testVersion() {
        // Constants
        assertEquals(ExampleInjection.VERSION, "0.1-SNAPSHOT");
        assertEquals(ExampleInjection.VERSION2, "To be replaced");
        // Methods
        assertEquals(new ExampleInjection().getVersionAsString(), "0.1-SNAPSHOT");
        assertEquals(ExampleInjection.getVersionAsStringStatic(), "0.1-SNAPSHOT");
        // Inner class
        assertEquals(ExampleInjection.InnerClass.VERSION, "0.1-SNAPSHOT");
        assertEquals(new ExampleInjection.InnerClass().getVersionAsString(), "0.1-SNAPSHOT");
        assertEquals(ExampleInjection.InnerClass.getVersionAsStringStatic(), "0.1-SNAPSHOT");
        // Build time
        assertEquals(new ExampleInjection().buildTimeDefault, ""); // Last initialization wins, which is the original one
        assertEquals(new ExampleInjection().buildTimePublic, "To be replaced"); // Last initialization wins, which is the original one
        assertEquals(new ExampleInjection().buildTimePublicFinal, "To be replaced"); // Last initialization wins, which is the original one
        assertTrue(new ExampleInjection().getBuildTime().startsWith("20"));
        // Build branch
        assertEquals( new ExampleInjection().getScmBranch(), "master");
    }
}
