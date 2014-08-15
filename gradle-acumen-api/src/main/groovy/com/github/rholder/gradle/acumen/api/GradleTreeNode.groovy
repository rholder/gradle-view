package com.github.rholder.gradle.acumen.api

interface GradleTreeNode {

    String getName()

    GradleTreeNode getParent()
    String getGroup()
    String getId()
    String getVersion()
    String getNodeType()

    List<GradleTreeNode> getChildren()

    boolean getSeenBefore()
}
