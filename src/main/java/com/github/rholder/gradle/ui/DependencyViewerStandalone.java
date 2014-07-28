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

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;

import static com.github.rholder.gradle.dependency.DependencyConversionUtil.loadProjectDependenciesFromModel;
import static com.github.rholder.gradle.ui.TreeUtil.convertToSortedTreeNode;
import static com.github.rholder.gradle.ui.TreeUtil.convertToTreeNode;

public class DependencyViewerStandalone extends JFrame {

    private static final String TITLE = "Gradle Dependency Viewer";

    private final DependencyCellRenderer dependencyCellRenderer;

    private String gradleBaseDir;
    private JSplitPane splitter;
    private ToolingLogger toolingLogger;

    public DependencyViewerStandalone() {
        super(TITLE);
        this.dependencyCellRenderer = new DependencyCellRenderer();

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
            updateView(new GradleNode("Loading..."), new GradleNode("No dependency selected"));

            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {

                    Map<String, GradleNode> dependencyMap = loadProjectDependenciesFromModel(gradleBaseDir, toolingLogger);
                    GradleNode tree = dependencyMap.get("root");

                    // TODO wire in loadDependencyInsight task when it's working
                    /*
                    GradleNode target = dependencyCellRenderer.selectedGradleNode;
                    GradleNode dependency;
                    if(target != null && target.group != null) {
                        Map<String, GradleNode> dependencyInsightMap = loadDependencyInsight(gradleBaseDir, toolingLogger, target.group, target.id);
                        dependency = dependencyInsightMap.get("root");
                    } else {
                        dependency = new GradleNode("No dependency selected");
                    }
                    */

                    updateView(tree, null);
                    return null;
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

    public void updateView(GradleNode tree, GradleNode dependency) {
        // TODO replace this hack with something that populates the GradleNode graph

        TreeModel leftModel = new DefaultTreeModel(convertToTreeNode(tree));
        final JTree leftTree = new JTree(leftModel);
        leftTree.setCellRenderer(dependencyCellRenderer);

        TreeModel rightModel = new DefaultTreeModel(convertToSortedTreeNode(tree));
        final JTree rightTree = new JTree(rightModel);
        rightTree.setCellRenderer(dependencyCellRenderer);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(gradleBaseDir != null) {
                    setTitle(TITLE + " - " + gradleBaseDir);
                }
                splitter.setLeftComponent(new JScrollPane(leftTree));
                splitter.setRightComponent(new JScrollPane(rightTree));
                splitter.setDividerLocation(0.5);
            }
        });
    }

    public static void main(String... args) {
        new DependencyViewerStandalone();
    }
}