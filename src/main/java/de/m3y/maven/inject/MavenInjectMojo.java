package de.m3y.maven.inject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javassist.*;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

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

    /**
     * Project compile classpath
     */
    @Parameter(property = "project.compileClasspathElements", required = true, readonly = true)
    private List<String> classpathElements;

    public void execute() throws MojoFailureException {
        ClassPool pool = createClassPool();
        for (Injection injection : injections) {
            doInject(injection, pool);
        }
    }

    private void doInject(Injection injection, ClassPool pool) throws MojoFailureException {
        String value = injection.getValue();
        if (null == value) {
            throw new MojoFailureException("Value is null for injection " + injection);
        }

        // Single injection?
        if (null != injection.getPointCut()) {
            handleInject(pool, injection.getPointCut(), value);
        }

        // Bulk injections?
        if (null != injection.getPointCuts()) {
            for (String pointcut : injection.getPointCuts()) {
                handleInject(pool, pointcut, value);
            }
        }

    }

    private void handleInject(ClassPool pool, String pointcut, String value) throws MojoFailureException {
        SourceTarget sourceTarget = parseSourceTarget(pointcut);

        getLog().info("Injecting value '" + value + "' into " + pointcut);
        try {
            final CtClass clazz = pool.get(sourceTarget.clazzName);
            if (clazz.isFrozen()) {
                clazz.defrost();
            }

            try {
                handle(clazz, sourceTarget.attribute, value);
            } catch (NotFoundException e) {
                throw new MojoFailureException("Can not load " + sourceTarget.clazzName + " attribute/method "
                        + sourceTarget.attribute, e);
            }

            clazz.writeFile(outputDirectory.getAbsolutePath());
        } catch (NotFoundException e) {
            throw new MojoFailureException("Can not load " + sourceTarget.clazzName, e);
        } catch (CannotCompileException e) {
            throw new MojoFailureException("Can not compile", e);
        } catch (IOException e) {
            throw new MojoFailureException("Can write class ", e);
        }
    }

    private void handle(CtClass clazz, String attribute, String value) throws NotFoundException, CannotCompileException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Injecting " + clazz.getName() + "#" + attribute + " with value " + value);
        }

        // Try fields
        for (CtField field : clazz.getFields()) {
            if (field.getName().equals(attribute)) {
                final CtField clazzField = clazz.getField(attribute);
                clazz.removeField(clazzField);
                clazz.addField(clazzField, CtField.Initializer.constant(value));
                return;
            }
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("Did not find field " + attribute + ", trying methods ...");
        }

        // Try method
        final CtMethod clazzMethod = clazz.getDeclaredMethod(attribute);
        clazzMethod.setBody("return \"" + value + "\";");
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


    private ClassPool createClassPool() throws MojoFailureException {
        try {
            List<String> classpathElements = project.getCompileClasspathElements();
            ClassPool pool = ClassPool.getDefault();
            for (Object element : classpathElements) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Adding " + element + " to classpath");
                }
                pool.insertClassPath(element.toString());
            }
            return pool;
        } catch (NotFoundException | DependencyResolutionRequiredException e) {
            throw new MojoFailureException("Can not load project compile classpath", e);
        }
    }
}
