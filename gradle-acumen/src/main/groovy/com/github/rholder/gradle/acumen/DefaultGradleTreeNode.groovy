package com.github.rholder.gradle.acumen

class DefaultGradleTreeNode implements GradleTreeNode, Serializable {

    String name

    GradleTreeNode parent
    String group
    String id
    String version
    List<GradleTreeNode> children = new ArrayList<GradleTreeNode>()

    boolean seenBefore = false
    String unresolvedVersion

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof DefaultGradleTreeNode)) return false

        DefaultGradleTreeNode that = (DefaultGradleTreeNode) o

        if (group != that.group) return false
        if (id != that.id) return false
        if (name != that.name) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (group != null ? group.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }
}
