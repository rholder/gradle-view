package com.github.rholder.gradle.service;

import com.github.rholder.gradle.dependency.GradleDependency;
import com.github.rholder.gradle.log.ToolingLogger;
import com.github.rholder.gradle.log.ToolingLoggerOutputStream;
import com.google.common.collect.Maps;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.rholder.gradle.dependency.DependencyConversionUtil.loadDependenciesFromText;

/**
 * Instances of this class maintain IntelliJ Project specific service state and
 * manage calls out to the Gradle Tooling API.
 */
public class GradleService extends AbstractProjectComponent {

    private List<GradleServiceListener> registeredListeners = new ArrayList<GradleServiceListener>();

    public GradleService(Project project) {
        super(project);
    }

    public void addListener(GradleServiceListener listener) {
        registeredListeners.add(listener);
    }

    public void reset() {
        for(GradleServiceListener r : registeredListeners) {
            r.reset();
        }
    }

    public void refresh() {
        for(GradleServiceListener r : registeredListeners) {
            r.refresh();
        }
    }

    public static Map<String, GradleDependency> loadProjectDependencies(String projectPath, ToolingLogger toolingLogger) {
        // TODO find all sub-dir's with build.gradle, run gradle dependencies in each one
        // TODO store cache of computed dependency tree
        if(projectPath == null) {
            return Collections.singletonMap("root", new GradleDependency("No Gradle project directory selected..."));
        }

        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(new File(projectPath))
                .connect();

        ByteArrayOutputStream rawOutputStream = new ByteArrayOutputStream();
        OutputStream outputStream = new ToolingLoggerOutputStream(rawOutputStream, toolingLogger);

        try {
            BuildLauncher launcher = connection.newBuild().forTasks("dependencies");
            launcher.setStandardOutput(outputStream);
            launcher.setStandardError(outputStream);

            launcher.run();
        } finally {
            connection.close();
        }

        Map<String, GradleDependency> dependencyMap = Maps.newHashMap();
        GradleDependency dependency;
        try {
            outputStream.close();
            dependency = loadDependenciesFromText(new ByteArrayInputStream(rawOutputStream.toByteArray()));
            dependencyMap.put("root", dependency);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dependencyMap;
    }
}
