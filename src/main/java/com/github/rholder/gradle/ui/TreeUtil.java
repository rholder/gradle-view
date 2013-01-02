package com.github.rholder.gradle.ui;

import com.github.rholder.gradle.dependency.GradleNode;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is a utility class for working with DefaultMutableTreeNode instances and
 * converting the GradleNode graph.
 */
public class TreeUtil {

    /**
     * Recursively convert the given dependency to a collection of nested
     * DefaultMutableTreeNode instances suitable for display in a tree
     * structure, returning the given converted dependency that was passed in.
     *
     * @param dependency
     */
    public static DefaultMutableTreeNode convertToTreeNode(GradleNode dependency) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(dependency);
        for(GradleNode d : dependency.dependencies) {
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
    public static DefaultMutableTreeNode convertToSortedTreeNode(GradleNode root) {
        // top level GradleNode instances are actually the configuration strings
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root);
        for(GradleNode configuration : root.dependencies) {
            DefaultMutableTreeNode configurationNode = new DefaultMutableTreeNode(configuration);
            rootNode.add(configurationNode);

            // TODO filter dupes here by fixing equals/hashCode, though there are no dupes unless Gradle is broken...
            Set<GradleNode> childDependencies = getChildrenFromRootNode(configuration);
            for(GradleNode d : childDependencies) {
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
    private static Set<GradleNode> getChildrenFromRootNode(GradleNode topNode) {
        Set<GradleNode> sortedDependencies = new TreeSet<GradleNode>();
        for(GradleNode d : topNode.dependencies) {
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
    private static Set<GradleNode> getChildNodes(GradleNode node) {
        Set<GradleNode> sortedDependencies = new TreeSet<GradleNode>();
        sortedDependencies.add(node);
        for(GradleNode d : node.dependencies) {
            sortedDependencies.addAll(getChildNodes(d));
        }
        return sortedDependencies;
    }
}
