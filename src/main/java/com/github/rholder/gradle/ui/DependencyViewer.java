package com.github.rholder.gradle.ui;

import com.github.rholder.gradle.dependency.GradleNode;
import com.github.rholder.gradle.log.ToolingLogger;
import com.github.rholder.gradle.intellij.GradleService;
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
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.Consumer;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.List;
import java.util.Map;

import static com.github.rholder.gradle.dependency.DependencyConversionUtil.loadProjectDependencies;
import static com.github.rholder.gradle.ui.TreeUtil.convertToTreeNode;
import static com.github.rholder.gradle.ui.TreeUtil.convertToSortedTreeNode;

public class DependencyViewer extends SimpleToolWindowPanel {

    private final Project project;
    private final ToolWindow toolWindow;
    private final Splitter splitter;
    private final ToolingLogger toolingLogger;
    private String gradleBaseDir;
    private boolean shouldPromptForCurrentProject;

    public DependencyViewer(Project p, ToolWindow t) {
        super(true, true);
        this.project = p;
        this.toolWindow = t;
        this.splitter = new Splitter();
        this.toolingLogger = initToolingLogger();
        this.shouldPromptForCurrentProject = true;

        // TODO clean all of this up
        GradleService gradleService = ServiceManager.getService(project, GradleService.class);
        gradleService.addListener(new ViewActionListener() {
            public void refresh() {
                if(shouldPromptForCurrentProject) {
                    switch(useCurrentProjectBuild()) {
                        case 0: gradleBaseDir = project.getBasePath();
                                break;
                        default: // do nothing, stay null
                    }
                    shouldPromptForCurrentProject = false;
                }

                if(gradleBaseDir == null) {
                    promptForGradleBaseDir();
                }

                updateView(new GradleNode("Loading..."));

                new SwingWorker<GradleNode, Void>() {
                    protected GradleNode doInBackground() throws Exception {
                        Map<String, GradleNode> dependencyMap = loadProjectDependencies(gradleBaseDir, toolingLogger);
                        GradleNode root = dependencyMap.get("root");
                        updateView(root);
                        return root;
                    }
                }.execute();
            }
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
                        }
                    }
                });
            }
        };
    }

    public void updateView(GradleNode dependency) {
        // TODO replace this hack with something that populates the GradleNode graph

        TreeModel leftModel = new DefaultTreeModel(convertToTreeNode(dependency));
        final SimpleTree leftTree = new SimpleTree(leftModel);
        leftTree.setCellRenderer(new DependencyCellRenderer());

        TreeModel rightModel = new DefaultTreeModel(convertToSortedTreeNode(dependency));
        final SimpleTree rightTree = new SimpleTree(rightModel);
        rightTree.setCellRenderer(new DependencyCellRenderer());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(gradleBaseDir != null) {
                    toolWindow.setTitle("- " + gradleBaseDir);
                }
                splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(leftTree));
                splitter.setSecondComponent(ScrollPaneFactory.createScrollPane(rightTree));
            }
        });
    }

    private void promptForGradleBaseDir() {
        FileChooserDescriptor fcd = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        fcd.setShowFileSystemRoots(true);
        fcd.setTitle("Choose a Gradle folder...");
        fcd.setDescription("Pick the top level directory to use when viewing dependencies (in case you have a multi-module project)");
        fcd.setHideIgnored(false);

        FileChooser.chooseFiles(fcd, project, project.getBaseDir(), new Consumer<List<VirtualFile>>() {
            @Override
            public void consume(List<VirtualFile> files) {
                gradleBaseDir = files.get(0).getPath();
            }
        });
    }

    private int useCurrentProjectBuild() {
        return Messages.showYesNoDialog(
                "Would you like to view the current project's Gradle dependencies?",
                "Gradle Dependency Viewer",
                null);
    }
}
