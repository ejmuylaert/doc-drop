package org.ej.docdrop.repository;

import org.ej.docdrop.domain.SyncCommand;
import org.ej.docdrop.domain.SyncEvent;
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

    default SyncCommand updateResult(SyncCommand command) {
        SyncCommand syncCommand = this.findById(new SyncCommand.CommandId(command.getFileId(),
                command.getCommandNumber())).get();

        syncCommand.setResult(SyncEvent.create(command, command.getSyncResult(), command.getSyncMessage()));

        return this.save(syncCommand);
    }
}