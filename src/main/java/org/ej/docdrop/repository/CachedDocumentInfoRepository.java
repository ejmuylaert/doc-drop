package org.ej.docdrop.repository;

import org.ej.docdrop.domain.CachedDocumentInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface CachedDocumentInfoRepository extends CrudRepository<CachedDocumentInfo, UUID> {

    List<CachedDocumentInfo> findByParentId(UUID parentId);

    @Modifying
    @Transactional
    @Query("update CachedDocumentInfo doc set doc.isRecent = :isRecent")
    void setRecentStatus(boolean isRecent);

    @Modifying
    @Transactional
    @Query("delete from CachedDocumentInfo doc where doc.isRecent = :isRecent")
    void deleteDocumentsWithRecentStatus(boolean isRecent);
}