package de.m3y.maven.inject;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Injects properties into compiled Java code, as constant or method return value.
 */
@Mojo(name = "inject",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true)
@Execute(goal = "inject", lifecycle = "process-classes")
public class MavenInjectMojo extends AbstractMojo {

    /**
     * Configures the version injection target.
     * <p>
     * Example:
     * <pre>
     * &lt;injection&gt;
     *   &lt;value>${maven.build.timestamp}&lt;/value&gt;
     *   &lt;pointCut>de.m3y.maven.inject.it.ExampleInjection.getBuildTimeStamp&lt;/pointCut&gt;
     *   &lt;pointCuts&gt;
     *     &lt;pointCut>de.m3y.maven.inject.it.ExampleInjection.getBuildTimeStampAgain&lt;/pointCut&gt;
     *     &lt;pointCut>de.m3y.maven.inject.it.ExampleInjection.getBuildTimeStampAgainAgain&lt;/pointCut&gt;
     *     ...
     *   &lt;/pointCuts&gt;
     * &lt;/injection&gt;
     * </pre>
     */
    @Parameter(required = true)
    private Injection[] injections;

    /**
     * The current Maven project.
     * <p>
     * Used for building classpath of classes to patch.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Configures the output directory of compiled Java class files.
     * <p>
     * Usually you do not need to configure this.
     */
    @Parameter(defaultValue = "${project.build.directory}/classes")
    private File outputDirectory;

    public void execute() throws MojoFailureException {
        ClassLoader classLoader = createClassLoader();
        for (Injection injection : injections) {
            doInject(classLoader, injection);
        }
    }

    private void doInject(ClassLoader classLoader, Injection injection) throws MojoFailureException {
        String value = injection.getValue();
        if (null == value) {
            throw new MojoFailureException("Value is null for injection " + injection);
        }

        // Single injection?
        if (null != injection.getPointCut()) {
            handleInject(classLoader, injection.getPointCut(), value);
        }

        // Bulk injections?
        if (null != injection.getPointCuts()) {
            for (String pointcut : injection.getPointCuts()) {
                handleInject(classLoader, pointcut, value);
            }
        }

    }

    private void handleInject(ClassLoader classLoader, String pointcut, String value) throws MojoFailureException {
        SourceTarget sourceTarget = parseSourceTarget(pointcut);

        getLog().info("Injecting value '" + value + "' into " + pointcut);
        try {
            final Class<?> clazz = classLoader.loadClass(sourceTarget.clazzName);
            DynamicType.Builder<?> builder = new ByteBuddy()
                    .redefine(clazz);
            if (hasField(clazz, sourceTarget)) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Found field " + clazz.getDeclaredField(sourceTarget.attribute));
                }
                builder = builder
                        .field(named(sourceTarget.attribute))
                        .value(value);
                final Field field = clazz.getDeclaredField(sourceTarget.attribute);
                final int modifiers = field.getModifiers();
                if (!Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                    getLog().warn("Non-final variable " +
                            sourceTarget.clazzName + "." + sourceTarget.attribute +
                            " initialization conflicts with default-constructor-based" +
                            " initialisation. Make variable final for expected behaviour.");
                }
            } else {
                final List<Method> declaredMethods = findMethodCandidates(clazz, sourceTarget.attribute);
                if (declaredMethods.size() == 1) {
                    Method method = declaredMethods.get(0);
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Found method " + method);
                    }
                    builder = builder
                            .method(named(sourceTarget.attribute))
                            .intercept(FixedValue.value(value));
                } else {
                    throw new MojoFailureException("Found more than one target for method: " + declaredMethods);
                }
            }
            try (final DynamicType.Unloaded<?> dynamicType = builder.make()) {
                dynamicType.saveIn(outputDirectory);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new MojoFailureException("Can not load " + sourceTarget.clazzName, e);
        }
    }

    private static List<Method> findMethodCandidates(Class<?> clazz, String methodName) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .collect(Collectors.toList());
    }

    private static boolean hasField(Class<?> clazz, SourceTarget sourceTarget) {
        try {
            clazz.getDeclaredField(sourceTarget.attribute);
            return true;
        } catch (NoSuchFieldException ex) {
            return false;
        }
    }

    private SourceTarget parseSourceTarget(String target) throws MojoFailureException {
        int idx = target.lastIndexOf('.');
        if (idx < 0) {
            throw new MojoFailureException("Can not parse " + target
                    + " into pattern <FULL_CLASS_NAME>.<ATTRIBUTE|METHOD>. Expected pattern like foo.Bar.MY_VERSION or foo.Bar.getSomeValue");
        }
        SourceTarget sourceTarget = new SourceTarget();
        sourceTarget.clazzName = target.substring(0, idx);
        sourceTarget.attribute = target.substring(idx + 1);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Extract class " + sourceTarget.clazzName + " and attribute/method " + sourceTarget.attribute);
        }
        return sourceTarget;
    }

    private static class SourceTarget {
        String clazzName;
        String attribute;
    }

    private URLClassLoader createClassLoader() throws MojoFailureException {
        try {
            List<String> classpathElements = project.getCompileClasspathElements();
            List<URL> urls = new ArrayList<>();
            for (Object element : classpathElements) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Adding " + element + " to classpath");
                }
                urls.add(new URL("file://" + element.toString() + "/"));
            }
            return new URLClassLoader(urls.toArray(new URL[0]), ByteBuddy.class.getClassLoader());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoFailureException("Can not load project compile classpath", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
