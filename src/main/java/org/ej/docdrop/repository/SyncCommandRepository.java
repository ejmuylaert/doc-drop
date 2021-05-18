package org.ej.docdrop.repository;

import org.ej.docdrop.domain.SyncCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncCommandRepository extends PagingAndSortingRepository<SyncCommand,
        SyncCommand.CommandId> {

    default Iterable<SyncCommand> pendingCommands() {
        return findAllBySyncedAtIsNullOrderByCommandNumberAsc();
    }

    Page<SyncCommand> findAllBySyncedAtIsNull(Pageable pageable);

    Iterable<SyncCommand> findAllBySyncedAtIsNullOrderByCommandNumberAsc();

    Iterable<SyncCommand> findAllByOrderByCommandNumberAsc();

    SyncCommand findFirstByOrderByCommandNumberDesc();
}