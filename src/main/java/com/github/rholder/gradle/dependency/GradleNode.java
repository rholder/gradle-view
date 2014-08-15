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

package com.github.rholder.gradle.dependency;

import java.util.ArrayList;
import java.util.List;

/**
 * A GradleNode encapsulates all the data needed to render a viewable tree
 * representation. A non-dependency node does not refer to a specific dependency
 * but instead can represent the root node or a configuration entry (such as
 * compile or runtime).
 */
public class GradleNode implements Comparable<GradleNode> {

    // name is filled in by configuration type nodes
    public String name;

    public GradleNode parent;
    public String group;
    public String id;
    public String version;
    public String nodeType;

    public boolean omitted = false;
    public String replacedByVersion = null;

    // child dependencies
    public List<GradleNode> dependencies = new ArrayList<GradleNode>();

    /**
     * Construct a non-dependency parent or root node.
     *
     * @param name name for this parent or root node
     */
    public GradleNode(String name) {
        this.name = name;
        this.parent = null;
    }

    /**
     * Construct a dependency node with the given values.
     *
     * @param parent the parent to set for this node
     * @param group the group to set for this node
     * @param id the id to set for this node
     * @param version the version to set for this node
     */
    public GradleNode(GradleNode parent, String group, String id, String version) {
        this.parent = parent;
        this.group = group;
        this.id = id;
        this.version = version;
    }

    /**
     * Return the name of a non-dependency node or the canonical
     * group:id:version of a dependency node.
     */
    public String getName() {
        return name == null ? group + ":" + id + ":" + getFinalVersion() : name;
    }

    /**
     * Return the name of a non-dependency node or the canonical
     * group:id:version of a dependency node with an -> if the version is being
     * replaced by another version.
     */
    public String getFullName() {
        return name == null ? group + ":" + id + ":" + version + (isReplaced() ? " -> " + replacedByVersion : "") : name;
    }

    /**
     * Return the actual version or the replacedByVersion if this dependency was
     * replaced by another version.
     */
    public String getFinalVersion() {
        return isReplaced() ? replacedByVersion : version;
    }

    public String getNodeType() {
        return nodeType;
    }

    /**
     * Return true if this dependency was omitted because it was included by
     * another dependency higher up in the tree, otherwise false.
     */
    public boolean isOmitted() {
        return omitted;
    }

    /**
     * Returns true if this dependency has been replaced by another dependency,
     * otherwise false.
     */
    public boolean isReplaced() {
        return replacedByVersion != null;
    }

    /**
     * Return the full traceback of everything in this instance.
     */
    public String toFullString() {
        StringBuilder b = new StringBuilder();
        String parentName = parent != null ? parent.getName() : "";
        b.append("name = " + getName() + ", parent = " + parentName);
        for(GradleNode d : dependencies) {
            b.append("\n");
            b.append(d);
        }
        return b.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Return the String comparison based on the return value of getName().
     *
     * @param o the other instance to compare against
     */
    public int compareTo(GradleNode o) {
        return getName().compareTo(o.getName());
    }
}
