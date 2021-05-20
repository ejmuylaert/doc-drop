package org.ej.docdrop.service;

import java.io.IOException;

/**
 * Thrown by RemarkableConnection, wraps the underlying SSH exceptions.
 */
public class ConnectionException extends IOException {
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}