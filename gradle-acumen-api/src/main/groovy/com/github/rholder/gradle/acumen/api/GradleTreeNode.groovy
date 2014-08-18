package com.github.rholder.gradle.acumen.api

interface GradleTreeNode {

    String getName()

    GradleTreeNode getParent()
    String getGroup()
    String getId()
    String getVersion()
    String getReason()
    String getRequestedVersion()
    String getNodeType()

    List<GradleTreeNode> getChildren()

    boolean getSeenBefore()
}
