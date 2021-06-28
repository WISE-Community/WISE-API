package org.wise.portal.score.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wise.portal.score.domain.TaskRequest;

@Repository
public interface TaskRequestRepository extends JpaRepository<TaskRequest, Long> {
}
