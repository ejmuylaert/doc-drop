package org.ej.docdrop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ej.docdrop.domain.CachedDocumentInfo;
import org.ej.docdrop.domain.DocumentType;
import org.ej.docdrop.domain.RemarkableMetadata;
import org.ej.docdrop.repository.CachedDocumentInfoRepository;

import java.util.List;
import java.util.UUID;

/**
 * This is the current representation of the device.
 * <p>
 * Usage of RemarkableService don't depend on a connected Remarkable.
 * <p>
 * The refreshFileTree methods for example doesn't refresh the list itself, it just asks the
 * device that if it is connected (and not busy) to execute an immediate refresh. But if the
 * device is not connected or busy, it returns without executing any action.
 * <p>
 * Even if the device is connected, it just sends the instruction to `RemarkableClient` and returns
 * control to the caller. This class just waits for the updates coming in.
 */
public class RemarkableService {

    private final RemarkableClient client;
    private final CachedDocumentInfoRepository repository;

    private final ObjectMapper mapper = new ObjectMapper();

    public RemarkableService(RemarkableClient client, CachedDocumentInfoRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public List<CachedDocumentInfo> getFolder(UUID parentId) {
        return repository.findByParentId(parentId);
    }

    public void refreshFileTree() {
        client.readFileTree(this::persistInfo, null);
    }

    private void persistInfo(UUID fileId, String metadata) {
        try {
            RemarkableMetadata remarkableMetadata = mapper.readValue(metadata,
                    RemarkableMetadata.class);

            CachedDocumentInfo info = new CachedDocumentInfo(
                    fileId,
                    remarkableMetadata.getParent(),
                    remarkableMetadata.getType() == DocumentType.FOLDER,
                    remarkableMetadata.getVisibleName());
            repository.save(info);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading Remarkable metadata", e);
        }
    }
}