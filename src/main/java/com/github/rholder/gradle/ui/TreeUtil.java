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
     * @param dependency the dependency to start from (may also be a root node)
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
     * @param root the root node to start from
     */
    public static DefaultMutableTreeNode convertToSortedTreeNode(GradleNode root) {
        // top level GradleNode instances are actually the configuration strings
        GradleNode sortedNode = new GradleNode("Flattened Project Dependencies");
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(sortedNode);
        for(GradleNode module : root.dependencies) {

            DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(module);
            rootNode.add(moduleNode);

            for(GradleNode configuration : module.dependencies) {
                DefaultMutableTreeNode configurationNode = new DefaultMutableTreeNode(configuration);
                moduleNode.add(configurationNode);

                // TODO filter dupes here by fixing equals/hashCode, though there are no dupes unless Gradle is broken...
                Set<GradleNode> childDependencies = getChildrenFromRootNode(configuration);
                for(GradleNode d : childDependencies) {
                    if(!d.isOmitted() && d.parent != null) {
                        configurationNode.add(new DefaultMutableTreeNode(d));
                    }
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
