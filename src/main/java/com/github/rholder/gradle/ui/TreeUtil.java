package com.github.rholder.gradle.ui;

import com.github.rholder.gradle.dependency.GradleDependency;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Set;
import java.util.TreeSet;

public class TreeUtil {

    public static DefaultMutableTreeNode convertToTreeNode(GradleDependency dependency) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(dependency);
        for(GradleDependency d : dependency.dependencies) {
            node.add(convertToTreeNode(d));
        }
        return node;
    }

    // --- begin sorted deps
    public static DefaultMutableTreeNode generateSortedDependencies(GradleDependency root) {
        // top level GradleDependency instances are actually the configuration strings
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root);
        for(GradleDependency configuration : root.dependencies) {
            DefaultMutableTreeNode configurationNode = new DefaultMutableTreeNode(configuration);
            rootNode.add(configurationNode);

            // TODO filter dupes here by fixing equals/hashCode, though there are no dupes unless Gradle is broken...
            Set<GradleDependency> childDependencies = getChildrenFromRootNode(configuration);
            for(GradleDependency d : childDependencies) {
                if(!d.isOmitted()) {
                    configurationNode.add(new DefaultMutableTreeNode(d));
                }
            }
        }
        return rootNode;
    }

    private static Set<GradleDependency> getChildrenFromRootNode(GradleDependency dependency) {
        Set<GradleDependency> sortedDependencies = new TreeSet<GradleDependency>();
        for(GradleDependency d : dependency.dependencies) {
            sortedDependencies.addAll(getChildrenNodes(d));
        }
        return sortedDependencies;
    }

    private static Set<GradleDependency> getChildrenNodes(GradleDependency dependency) {
        Set<GradleDependency> sortedDependencies = new TreeSet<GradleDependency>();
        sortedDependencies.add(dependency);
        for(GradleDependency d : dependency.dependencies) {
            sortedDependencies.addAll(getChildrenNodes(d));
        }
        return sortedDependencies;
    }
    // --- end sorted deps

}
