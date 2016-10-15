package de.m3y.maven.inject.it;

/**
 *
 */
public class ExampleInjection {
    // Constants
    public static final String VERSION = "To be replaced";
    public static final String VERSION2 = VERSION; // Won't get replaced, as compiler inlines constants
    private String BUILD_TIME = "";
    public static final String REVISION = "To be replaced";

    // Variables
    private String buildTimePrivate = "";
    String buildTimeDefault = "";
    public String buildTimePublic = "To be replaced";
    public final String buildTimePublicFinal = "To be replaced";

    public String getVersionAsString() {
        return "To be replaced";
    }

    public static String getVersionAsStringStatic() {
        return "To be replaced";
    }

    public String getBuildTime() {
        return null;
    }

    public String getScmBranch() {
        return "To be replaced";
    }

    public static class InnerClass {
        public static final String VERSION = "To be replaced";

        public String getVersionAsString() {
            return "To be replaced";
        }

        public static String getVersionAsStringStatic() {
            return "To be replaced";
        }
    }
}
