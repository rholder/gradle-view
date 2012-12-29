package com.github.rholder.gradle.ui;

import com.github.rholder.gradle.service.GradleService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

/**
 * Reset the Gradle dependency views, coordinating via the shared Project
 * specific GradleService. See plugin.xml for additional wiring.
 */
public class ResetTreeAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);

        GradleService gradleService = ServiceManager.getService(project, GradleService.class);
        gradleService.reset();
    }
}
