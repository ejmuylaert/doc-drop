package org.ej.docdrop.repository;

import org.ej.docdrop.domain.RemarkableCommand;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RemarkableCommandRepository extends CrudRepository<RemarkableCommand,
        RemarkableCommand.CommandId> {

    Iterable<RemarkableCommand> findByExecutionStartedAtIsNotNullAndExecutedAtIsNull();

    Optional<RemarkableCommand> findFirstByExecutionStartedAtIsNullAndExecutedAtIsNullOrderByCommandNumberAsc();

    Iterable<RemarkableCommand> findAllByOrderByCommandNumberAsc();

    RemarkableCommand findFirstByOrderByCommandNumberDesc();
}