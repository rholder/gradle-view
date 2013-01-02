package com.github.rholder.gradle.ui;

import com.github.rholder.gradle.dependency.GradleDependency;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Set;
import java.util.TreeSet;

public class TreeUtil {

    /**
     * Recursively convert the given dependency to a collection of nested
     * DefaultMutableTreeNode instances suitable for display in a tree
     * structure, returning the given converted dependency that was passed in.
     *
     * @param dependency
     */
    public static DefaultMutableTreeNode convertToTreeNode(GradleDependency dependency) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(dependency);
        for(GradleDependency d : dependency.dependencies) {
            node.add(convertToTreeNode(d));
        }
        return node;
    }

    /**
     * Return a lexicographically sorted and flattened DefaultMutableTreeNode
     * root where its children are all the de-duplicated nodes from the given
     * dependency node root.
     *
     * @param root
     */
    public static DefaultMutableTreeNode convertToSortedTreeNode(GradleDependency root) {
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

    /**
     * Recursively walk all the children of the given dependency and return a
     * flattened Set containing each of them (sorted lexicographically).
     *
     * @param topNode the top dependency node to start from
     */
    private static Set<GradleDependency> getChildrenFromRootNode(GradleDependency topNode) {
        Set<GradleDependency> sortedDependencies = new TreeSet<GradleDependency>();
        for(GradleDependency d : topNode.dependencies) {
            sortedDependencies.addAll(getChildNodes(d));
        }
        return sortedDependencies;
    }

    /**
     * Recursively walk all the children of the given dependency and return a
     * flattened Set containing each of them (sorted lexicographically).
     *
     * @param node the dependency node to gather child nodes from
     */
    private static Set<GradleDependency> getChildNodes(GradleDependency node) {
        Set<GradleDependency> sortedDependencies = new TreeSet<GradleDependency>();
        sortedDependencies.add(node);
        for(GradleDependency d : node.dependencies) {
            sortedDependencies.addAll(getChildNodes(d));
        }
        return sortedDependencies;
    }
}
