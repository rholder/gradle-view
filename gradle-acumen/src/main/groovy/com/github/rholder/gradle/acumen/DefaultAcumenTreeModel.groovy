package com.github.rholder.gradle.acumen

import com.github.rholder.gradle.acumen.api.AcumenTreeModel
import com.github.rholder.gradle.acumen.api.GradleTreeNode

class DefaultAcumenTreeModel implements Serializable, AcumenTreeModel {
    GradleTreeNode nodeTree
}