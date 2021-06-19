package org.extvos.restlet.generator;

import org.extvos.common.Validator;
import org.extvos.restlet.generator.gen.Generator;
import org.extvos.restlet.generator.gen.PackageOption;
import org.extvos.restlet.generator.utils.ClasspathPackageScanner;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author shenmc
 */
@Mojo(name = "gen", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class RestletGenerator extends AbstractMojo {

    @Parameter(name = "packages", required = true, readonly = true)
    private List<PackageOption> packages;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File classesDirectory;

    @Parameter(name = "author", property = "author")
    private String author;

    @Parameter(name = "forceOverWrite", property = "forceOverWrite")
    private boolean forceOverWrite;

    @SneakyThrows
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("project base dir:> " + project.getBasedir());
        getLog().info("project base source dir:> " + project.getBasedir() + "/src/main/java");
        getLog().info("project build output dir:> " + classesDirectory);
        if (packages == null || packages.size() < 1) {
            throw new MojoExecutionException("at lease one package should configured");
        }

        if (Validator.isEmpty(author)) {
            author = "Quick Lab";
        }

        List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
        URL[] runtimeUrls = new URL[runtimeClasspathElements.size() + 1];
        for (int i = 0; i < runtimeClasspathElements.size(); i++) {
            String element = (String) runtimeClasspathElements.get(i);
            getLog().debug("Class Path:> " + element);
            runtimeUrls[i] = new File(element).toURI().toURL();
        }
        runtimeUrls[runtimeClasspathElements.size()] = classesDirectory.toURI().toURL();
        URLClassLoader newLoader = new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
        for (PackageOption opts : packages) {
            getLog().debug("Processing package " + opts.getSource() + " to " + (opts.getTarget() != null ? opts.getTarget() : opts.getSource()) + " ...");
            if (Validator.isEmpty(opts.getAuthor())) {
                opts.setAuthor(author);
            }
            if (forceOverWrite) {
                getLog().debug("forceOverWrite:> " + forceOverWrite);
                opts.setForceOverride(forceOverWrite);
            }
            ClasspathPackageScanner cps = new ClasspathPackageScanner(opts.getSource(), newLoader);
            try {
                for (Class<?> cls : cps.getFullyQualifiedClasses()) {
                    getLog().debug(">> Scanning class " + cls.getName() + " ...");
                    Generator generator = Generator.from(cls);
                    if (null != generator) {
                        generator.config(getLog())
                                .config(project.getBasedir() + "/src/main/java", opts)
                                .generate();
                    }
                }
            } catch (MojoExecutionException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new MojoExecutionException(e.getMessage());
            }
        }
        getLog().info("Done.");
    }

}