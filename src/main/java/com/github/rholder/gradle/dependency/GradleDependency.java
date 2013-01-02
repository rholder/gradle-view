package com.github.rholder.gradle.dependency;

import java.util.ArrayList;
import java.util.List;

// TODO rename to GradleNode
/**
 * A GradleDependency encapsulates all the data needed to render a viewable
 * tree representation. A non-dependency node does not refer to a specific
 * dependency but instead can represent the root node or a configuration entry
 * (such as compile or runtime).
 */
public class GradleDependency implements Comparable<GradleDependency> {

    // name is filled in by configuration type nodes
    public String name;

    public GradleDependency parent;
    public String group;
    public String id;
    public String version;

    // used for internal tracking of how deep a node is within the tree
    public int level;

    public boolean omitted = false;
    public String replacedByVersion = null;

    // child dependencies
    public List<GradleDependency> dependencies = new ArrayList<GradleDependency>();

    /**
     * Construct a non-dependency parent or root node.
     *
     * @param name name for this parent or root node
     */
    public GradleDependency(String name) {
        this.name = name;
        this.parent = null;
        this.level = 0;
    }

    /**
     * Construct a dependency node with the given values.
     *
     * @param parent the parent to set for this node
     * @param group the group to set for this node
     * @param id the id to set for this node
     * @param version the version to set for this node
     */
    public GradleDependency(GradleDependency parent, String group, String id, String version) {
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
        return name == null ? group + ":" + id + ":" + version : name;
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
        for(int i = 0; i < level; i++) {
            b.append("    ");
        }
        String parentName = parent != null ? parent.getName() : "";
        b.append("name = " + getName() + ", level = " + level + ", parent = " + parentName);
        for(GradleDependency d : dependencies) {
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
    public int compareTo(GradleDependency o) {
        return getName().compareTo(o.getName());
    }
}
