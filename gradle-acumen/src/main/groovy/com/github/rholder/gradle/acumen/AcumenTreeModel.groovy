package com.github.rholder.gradle.acumen

import org.gradle.tooling.model.Model

public interface AcumenTreeModel extends Model {
    GradleTreeNode getNodeTree()
}
