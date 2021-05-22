package org.ej.docdrop.service;

import java.io.IOException;

/**
 * Thrown by RemarkableConnection, wraps the underlying SSH exceptions.
 */
public class RemarkableConnectionException extends IOException {
    public RemarkableConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}