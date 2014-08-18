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

package com.github.rholder.gradle.ui;

import com.github.rholder.gradle.dependency.GradleNode;
import com.github.rholder.gradle.log.ToolingLogger;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;

import static com.github.rholder.gradle.dependency.DependencyConversionUtil.loadProjectDependenciesFromModel;
import static com.github.rholder.gradle.ui.TreeUtil.convertToHierarchyTreeNode;
import static com.github.rholder.gradle.ui.TreeUtil.convertToSortedTreeNode;

public class DependencyViewerStandalone extends JFrame {

    private static final String TITLE = "Gradle Dependency Viewer";

    private final DependencyCellRenderer dependencyCellRenderer;

    private String gradleBaseDir;
    private JSplitPane splitter;
    private ToolingLogger toolingLogger;
    private JTextArea information;

    public DependencyViewerStandalone() {
        super(TITLE);
        this.dependencyCellRenderer = new DependencyCellRenderer();
        this.information = new JTextArea();
        this.information.setEnabled(false);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocation(128, 128);

        initMenu();
        initToolingLogger();
        initContent();
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem open = new JMenuItem("Open Project Folder", KeyEvent.VK_O);
        open.setMnemonic(KeyEvent.VK_O);
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                promptForGradleBaseDir();
                refresh();
            }
        });
        fileMenu.add(open);

        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        fileMenu.add(refresh);

        fileMenu.addSeparator();
        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exit);

        setJMenuBar(menuBar);
    }

    private void refresh() {
        if(gradleBaseDir != null) {
            updateView(null, null);

            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {

                    try {
                        Map<String, GradleNode> dependencyMap = loadProjectDependenciesFromModel(gradleBaseDir, toolingLogger);
                        GradleNode tree = dependencyMap.get("root");

                        GradleNode target = dependencyCellRenderer.selectedGradleNode;
                        GradleNode dependency;
                        if(target != null && target.group != null) {
                            dependency = target;
                        } else {
                            dependency = new GradleNode("No dependency selected");
                        }

                        updateView(tree, dependency);
                        return null;
                    } catch(Exception e) {
                        e.printStackTrace();
                        toolingLogger.log(ExceptionUtils.getFullStackTrace(e));
                        throw new RuntimeException(e);
                    }
                }
            }.execute();
        }
    }

    private void initToolingLogger() {
        toolingLogger = new ToolingLogger() {
            public void log(final String line) {
                // note: lots of log messages will freeze the dispatch thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (gradleBaseDir != null) {
                            setTitle("- " + gradleBaseDir + " - " + line);
                            information.append(line + "\n");
                        }
                    }
                });
            }
        };
    }

    private void initContent() {
        splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JPanel main = new JPanel(new BorderLayout());
        main.add(splitter);
        getContentPane().add(main);
        setEnabled(true);
        setVisible(true);
    }

    private void promptForGradleBaseDir() {
        JFileChooser c = new JFileChooser();
        c.setDialogTitle("Pick the top level directory to use when viewing dependencies (in case you have a multi-module project)");
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = c.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            gradleBaseDir = c.getSelectedFile().getPath();
        }
    }

    public void updateView(GradleNode rootDependency, GradleNode selectedDependency) {
        // TODO replace this hack with something that populates the GradleNode graph

        DefaultMutableTreeNode fullRoot = new DefaultMutableTreeNode(new GradleNode("Project Dependencies"));
        if(rootDependency == null) {
            DefaultMutableTreeNode loading = new DefaultMutableTreeNode(new GradleNode("Loading..."));
            fullRoot.add(loading);
        } else {
            DefaultMutableTreeNode hierarchyRoot = convertToHierarchyTreeNode(rootDependency);
            DefaultMutableTreeNode flattenedRoot = convertToSortedTreeNode(rootDependency);
            fullRoot.add(hierarchyRoot);
            fullRoot.add(flattenedRoot);
        }

        TreeModel treeModel = new DefaultTreeModel(fullRoot);
        final JTree fullTree = new JTree(treeModel);
        fullTree.setCellRenderer(dependencyCellRenderer);

        // expand path for first level from root
        //fullTree.expandPath(new TreePath(hierarchyRoot.getNextNode().getPath()));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(gradleBaseDir != null) {
                    setTitle(TITLE + " - " + gradleBaseDir);
                }
                splitter.setLeftComponent(new JScrollPane(fullTree));
                splitter.setRightComponent(new JScrollPane(information));
                splitter.setDividerLocation(0.5);
            }
        });
    }

    public static void main(String... args) {
        new DependencyViewerStandalone();
    }
}