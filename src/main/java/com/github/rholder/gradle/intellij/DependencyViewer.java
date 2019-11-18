/*
 * Copyright 2013 Ray Holder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rholder.gradle.intellij;

import com.github.rholder.gradle.dependency.GradleNode;
import com.github.rholder.gradle.log.ToolingLogger;
import com.github.rholder.gradle.ui.DependencyCellRenderer;
import com.github.rholder.gradle.ui.ViewActionListener;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.Consumer;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.List;
import java.util.Map;

import static com.github.rholder.gradle.dependency.DependencyConversionUtil.loadProjectDependenciesFromModel;
import static com.github.rholder.gradle.ui.TreeUtil.convertToHierarchyTreeNode;
import static com.github.rholder.gradle.ui.TreeUtil.convertToSortedTreeNode;

public class DependencyViewer extends SimpleToolWindowPanel {

    private final Project project;
    private final ToolWindow toolWindow;
    private final Splitter splitter;
    private final ToolingLogger toolingLogger;
    private final DependencyCellRenderer dependencyCellRenderer;
    private final JTextArea information;
    private String gradleBaseDir;
    private boolean shouldPromptForCurrentProject;

    public DependencyViewer(Project p, ToolWindow t) {
        super(true, true);
        this.project = p;
        this.toolWindow = t;
        this.splitter = new Splitter(false, 0.75f);
        this.information = new JTextArea();
        this.toolingLogger = initToolingLogger();

        this.dependencyCellRenderer = new DependencyCellRenderer();
        this.dependencyCellRenderer.omittedSelected = JBColor.MAGENTA;
        this.dependencyCellRenderer.omittedUnselected = JBColor.GRAY;
        this.dependencyCellRenderer.normalSelected = JBColor.foreground();
        this.dependencyCellRenderer.normalUnselected = JBColor.BLACK;
        this.information.setEditable(false);

        this.shouldPromptForCurrentProject = true;

        // TODO clean all of this up
        GradleService gradleService = ServiceManager.getService(project, GradleService.class);
        gradleService.addListener(new ViewActionListener() {
            @Override
            public void refresh() {
                // prompt only on first use of tool panel
                if (shouldPromptForCurrentProject) {
                    // ask to initialize view for this project to the current project
                    if (useCurrentProjectBuild()) {
                        gradleBaseDir = project.getBasePath();
                    }
                    shouldPromptForCurrentProject = false;
                }

                // there's nothing to do when there is no gradleBaseDir set, instead issue a prompt
                if(gradleBaseDir == null) {
                    promptForGradleBaseDir();
                }

                // initialize an empty view even if the gradleBaseDir is set while we load everything in the background
                updateView(null, null);

                new SwingWorker<GradleNode, Void>() {
                    protected GradleNode doInBackground() throws Exception {
                        try {
                            Map<String, GradleNode> dependencyMap = loadProjectDependenciesFromModel(gradleBaseDir, toolingLogger);
                            GradleNode rootDependency = dependencyMap.get("root");

                            GradleNode target = dependencyCellRenderer.selectedGradleNode;
                            GradleNode selectedDependency;
                            if(target != null && target.group != null) {
                                selectedDependency = target;
                            } else {
                                selectedDependency = new GradleNode("No dependency selected");
                            }

                            updateView(rootDependency, selectedDependency);
                            return rootDependency;
                        } catch(Exception e) {
                            e.printStackTrace();
                            toolingLogger.log(ExceptionUtils.getFullStackTrace(e));
                            throw new RuntimeException(e);
                        }
                    }
                }.execute();
            }

            @Override
            public void toggleShowReplaced() {
                dependencyCellRenderer.showReplaced = !dependencyCellRenderer.showReplaced;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        splitter.repaint();
                        splitter.validate();
                    }
                });
            }

            @Override
            public void reset() {
                gradleBaseDir = null;
                refresh();
            }
        });
        gradleService.refresh();

        setContent(splitter);
        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Gradle View Toolbar",
                (DefaultActionGroup)actionManager.getAction("GradleView.NavigatorActionsToolbar"), true);

        actionToolbar.setTargetComponent(splitter);
        setToolbar(actionToolbar.getComponent());
    }

    private ToolingLogger initToolingLogger() {
        return new ToolingLogger() {
            public void log(final String line) {
                // note: lots of log messages will freeze the dispatch thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(gradleBaseDir != null) {
                            toolWindow.setTitle("- " + gradleBaseDir + " - " + line);
                            information.append(line + "\n");
                        }
                    }
                });
            }
        };
    }

    public void updateView(GradleNode rootDependency, final GradleNode selectedDependency) {
        // TODO replace this hack with something that populates the GradleNode graph

        DefaultMutableTreeNode fullRoot = new DefaultMutableTreeNode(new GradleNode("Project Dependencies"));
        if(rootDependency == null) {
            DefaultMutableTreeNode loading = new DefaultMutableTreeNode(new GradleNode("Loading..."));
            fullRoot.add(loading);
        } else {
            DefaultMutableTreeNode flattenedRoot = convertToSortedTreeNode(rootDependency);
            DefaultMutableTreeNode hierarchyRoot = convertToHierarchyTreeNode(rootDependency);
            fullRoot.add(flattenedRoot);
            fullRoot.add(hierarchyRoot);
        }

        TreeModel treeModel = new DefaultTreeModel(fullRoot);
        final SimpleTree fullTree = new SimpleTree(treeModel);
        fullTree.setCellRenderer(dependencyCellRenderer);

        // expand path for first level from root
        //fullTree.expandPath(new TreePath(hierarchyRoot.getNextNode().getPath()));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(gradleBaseDir != null) {
                    toolWindow.setTitle("- " + gradleBaseDir);
                }
                splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(fullTree));
                splitter.setSecondComponent(ScrollPaneFactory.createScrollPane(information));
            }
        });
    }

    private void promptForGradleBaseDir() {
        FileChooserDescriptor fcd = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        fcd.setShowFileSystemRoots(true);
        fcd.setTitle("Choose a Project Folder");
        fcd.setDescription("Pick the top level directory to use when viewing dependencies (in case you have a multi-module project).");
        fcd.setHideIgnored(false);

        FileChooser.chooseFiles(fcd, project, project.getBaseDir(), new Consumer<List<VirtualFile>>() {
            @Override
            public void consume(List<VirtualFile> files) {
                gradleBaseDir = files.get(0).getPath();
            }
        });
    }

    private boolean useCurrentProjectBuild() {
        int answer = Messages.showYesNoDialog(
                "Would you like to view the current project's Gradle dependencies?",
                "Gradle Dependency Viewer",
                Messages.getQuestionIcon());
        return answer == Messages.YES;
    }
}
