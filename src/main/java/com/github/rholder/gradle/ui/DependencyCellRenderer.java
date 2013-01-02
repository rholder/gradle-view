package com.github.rholder.gradle.ui;

import com.github.rholder.gradle.dependency.GradleNode;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * Add some text highlighting and other visual indicators to the view of
 * GradleNode nodes in the tree depending on whether they've been replaced by
 * another dependency or omitted because they've been included elsewhere up the
 * tree.
 */
public class DependencyCellRenderer extends JLabel implements TreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        GradleNode gradleDependency = (GradleNode) ((DefaultMutableTreeNode) value).getUserObject();
        String text = gradleDependency.getName();

        if(gradleDependency.isReplaced()) {
            text += " -> " + gradleDependency.replacedByVersion;
        }

        if(gradleDependency.isOmitted()) {
            setForeground(selected ? Color.MAGENTA : Color.LIGHT_GRAY);
        } else {
            setForeground(selected ? Color.RED : Color.BLACK);
        }
        setText(text);
        return this;
    }
}
