package com.github.rholder.gradle.intellij;

import com.github.rholder.gradle.ui.DependencyViewer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;

/**
 * Bind the tooling windows together with IntelliJ's API, creating a dependency
 * view content pane. See plugin.xml for additional wiring.
 */
public class GradleViewToolWindowFactory implements ToolWindowFactory {

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        DependencyViewer dependencyViewer = new DependencyViewer(project, toolWindow);
        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(dependencyViewer, "", false));
    }
}
