package de.m3y.maven.inject.it;

/**
 * Example singleton containing build info.
 *
 * See https://sites.google.com/site/io/effective-java-reloaded for suggested Enum singleton.
 */
public enum BuildInfo {
    INSTANCE;

    /**
     * @return the project version such as 1.1-SNAPSHOT.
     */
    public String getProjectVersion() {
        return "<will be replaced>";
    }
}
