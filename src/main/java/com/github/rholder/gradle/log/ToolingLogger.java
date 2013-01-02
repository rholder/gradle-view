package com.github.rholder.gradle.log;

/**
 * Implementations of this interface are meant to process tool logging
 * information in some way.
 */
public interface ToolingLogger {

    /**
     * Log the given message.
     *
     * @param message the message to be logged
     */
    void log(String message);
}