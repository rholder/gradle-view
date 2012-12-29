package com.github.rholder.gradle.log;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Fire off ToolingLogger log messages for each line that goes through the
 * target underlying OutputStream.
 */
public class ToolingLoggerOutputStream extends FilterOutputStream {

    private volatile String lastLine = "";
    private ToolingLogger toolingLogger;

    public ToolingLoggerOutputStream(OutputStream outputStream, ToolingLogger toolingLogger) {
        super(outputStream);
        this.toolingLogger = toolingLogger;
    }

    @Override
    public void write(int b) throws IOException {
        char value = (char) b;
        if(value != '\n') {
            lastLine += value;
        } else {
            toolingLogger.log(lastLine);
            lastLine = "";
        }
        super.write(b);
    }

    @Override
    public synchronized void write(byte b[], int off, int len) throws IOException {
        for(int i = off; i < len; i++) {
            char value = (char) b[i];
            if(value != '\n') {
                lastLine += value;
            } else {
                toolingLogger.log(lastLine);
                lastLine = "";
            }
        }
        super.write(b, off, len);
    }
}
