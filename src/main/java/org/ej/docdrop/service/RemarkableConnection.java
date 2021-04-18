package org.ej.docdrop.service;

import net.schmizz.sshj.sftp.RemoteResourceInfo;

import java.util.List;

/**
 * Abstracts avoid lots of mocking, boundary with external system.
 */
class RemarkableConnection {

    List<RemoteResourceInfo> readFileTree() throws ConnectionException {
        throw new ConnectionException("Not implemented", null);
    }

    String readFile(String path) throws ConnectionException {
        throw new ConnectionException("Not implemented", null);
    }
}