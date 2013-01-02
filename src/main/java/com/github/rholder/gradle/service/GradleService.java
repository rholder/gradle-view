package com.github.rholder.gradle.service;

import com.github.rholder.gradle.dependency.GradleDependency;
import com.github.rholder.gradle.log.ToolingLogger;
import com.google.common.collect.Maps;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.rholder.gradle.dependency.DependencyConversionUtil.loadDependenciesFromText;

/**
 * Instances of this class maintain IntelliJ Project specific service state and
 * manage calls out to the Gradle Tooling API.
 */
public class GradleService extends AbstractProjectComponent implements GradleServiceListener {

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
}
