package org.wise.portal.score.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wise.portal.score.domain.Task;

import java.util.List;

/**
 * Repository for tasks
 *
 * @author Anthony Perritano
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findAllByRunId(Long runId);
  List<Task> findAllByRunIdAndPeriodId(Long runId, Long periodId);
  List<Task> findAllByRunIdAndPeriodIdAndWorkgroupId(Long runId, Long periodId, Long workgroupId);

}
