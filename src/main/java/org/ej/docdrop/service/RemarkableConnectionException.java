package org.ej.docdrop.service;

/**
 * Thrown by RemarkableConnection, wraps the underlying SSH exceptions.
 */
public class RemarkableConnectionException extends Throwable {
    public RemarkableConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}