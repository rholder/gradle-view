package com.github.rholder.gradle.ui;

/**
 * Implementations of this class define behaviors for actions that are
 * coordinated through asynchronous UI bindings.
 */
public interface ViewActionListener {

    /**
     * Perform the reset action on the Gradle dependency view.
     */
    void reset();

    /**
     * Perform the refresh action on the Gradle dependency view.
     */
    void refresh();
}