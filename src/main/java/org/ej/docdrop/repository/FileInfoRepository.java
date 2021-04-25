package org.ej.docdrop.repository;

import org.ej.docdrop.domain.FileInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileInfoRepository extends CrudRepository<FileInfo, UUID> {

    List<FileInfo> getFileInfoByParentId(UUID parentId);
}