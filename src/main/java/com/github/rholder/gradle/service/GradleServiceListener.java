package com.github.rholder.gradle.service;

/**
 * Implementations of this class define behaviours for actions that are
 * coordinated through the shared GradleService.
 */
public interface GradleServiceListener {

    /**
     * Perform the reset action on the Gradle dependency view.
     */
    void reset();

    /**
     * Perform the refresh action on the Gradle dependency view.
     */
    void refresh();
}