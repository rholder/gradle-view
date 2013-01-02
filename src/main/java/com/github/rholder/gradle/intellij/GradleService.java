package com.github.rholder.gradle.intellij;

import com.github.rholder.gradle.ui.ViewActionListener;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class maintain IntelliJ Project specific service state and
 * manage listener callbacks.
 */
public class GradleService extends AbstractProjectComponent {

    private List<ViewActionListener> registeredListeners = new ArrayList<ViewActionListener>();

    public GradleService(Project project) {
        super(project);
    }

    public void addListener(ViewActionListener listener) {
        registeredListeners.add(listener);
    }

    public void reset() {
        for(ViewActionListener r : registeredListeners) {
            r.reset();
        }
    }

    public void refresh() {
        for(ViewActionListener r : registeredListeners) {
            r.refresh();
        }
    }
}
