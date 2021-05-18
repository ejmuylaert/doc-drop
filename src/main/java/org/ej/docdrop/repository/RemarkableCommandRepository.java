package org.ej.docdrop.repository;

import org.ej.docdrop.domain.RemarkableCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RemarkableCommandRepository extends PagingAndSortingRepository<RemarkableCommand,
        RemarkableCommand.CommandId> {

    default Iterable<RemarkableCommand> pendingCommands() {
        return findAllByExecutionStartedAtIsNullAndExecutedAtIsNullOrderByCommandNumberAsc();
    }

    Page<RemarkableCommand> findAllByExecutionStartedAtIsNullAndExecutedAtIsNull(Pageable pageable);

    Iterable<RemarkableCommand> findAllByExecutionStartedAtIsNullAndExecutedAtIsNullOrderByCommandNumberAsc();


    Iterable<RemarkableCommand> findByExecutionStartedAtIsNotNullAndExecutedAtIsNull();

    Optional<RemarkableCommand> findFirstByExecutionStartedAtIsNullAndExecutedAtIsNullOrderByCommandNumberAsc();

    Iterable<RemarkableCommand> findAllByOrderByCommandNumberAsc();

    RemarkableCommand findFirstByOrderByCommandNumberDesc();
}