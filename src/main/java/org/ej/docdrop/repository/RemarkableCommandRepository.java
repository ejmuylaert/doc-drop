package org.ej.docdrop.repository;

import org.ej.docdrop.domain.RemarkableCommand;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RemarkableCommandRepository extends CrudRepository<RemarkableCommand,
        RemarkableCommand.CommandId> {

    Iterable<RemarkableCommand> findAllByOrderByCommandNumberAsc();

    RemarkableCommand findFirstByOrderByCommandNumberDesc();
}