package org.ej.docdrop.repository;

import org.ej.docdrop.domain.CachedDocumentInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CachedDocumentInfoRepository extends CrudRepository<CachedDocumentInfo, UUID> {

    List<CachedDocumentInfo> findByParentId(UUID parentId);
}