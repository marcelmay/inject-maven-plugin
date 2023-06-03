package de.m3y.maven.inject;

import java.util.Arrays;

/**
 * Defines the injection by one or more pointcut(s) and value.
 * <p>
 * Example pointcut: <code>de.m3y.maven.inject.it.ExampleInjection.getBuildTime</code>
 */
public class Injection {
    private String value;
    private String pointCut;
    private String[] pointCuts;

    public String getValue() {
        return value;
    }

    public String getPointCut() {
        return pointCut;
    }

    public void setPointCut(String pointCut) {
        this.pointCut = pointCut;
    }


    public String[] getPointCuts() {
        return pointCuts;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setPointCuts(String[] pointCuts) {
        this.pointCuts = pointCuts;
    }

    @Override
    public String toString() {
        return "Injection{" +
                "value='" + value + '\'' +
                ", pointCut='" + pointCut + '\'' +
                ", pointCuts=" + Arrays.toString(pointCuts) +
                '}';
    }
}
