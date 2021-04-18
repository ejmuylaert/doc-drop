package org.ej.docdrop.service;

// No CONNECTING status, because it is an USB connection, so timeouts are low.
enum RemarkableStatus {
    DISCONNECTED,
    AVAILABLE,
    BUSY,
    UNKNOWN
}