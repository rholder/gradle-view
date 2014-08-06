package com.github.rholder.gradle.acumen.api

import org.gradle.tooling.model.Model

public interface AcumenTreeModel extends Model {
    GradleTreeNode getNodeTree()
}
