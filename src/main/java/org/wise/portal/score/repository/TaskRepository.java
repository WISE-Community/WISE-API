package org.wise.portal.score.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wise.portal.score.domain.Task;

import java.util.List;
import java.util.Optional;

/**
 * Repository for tasks
 *
 * @author Anthony Perritano
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findAllByRunId(Long runId);
  List<Task> findAllByRunIdAndPeriodId(Long runId, Long periodId);
  List<Task> findAllByRunIdAndPeriodName(Long runId, String periodName);
  List<Task> findAllByRunIdAndPeriodIdAndWorkgroupId(Long runId, Long periodId, Long workgroupId);
  Optional<Task> findByRunIdAndPeriodIdAndWorkgroupIdAndActivityId(Long runId, Long periodId, Long workgrougId, String activityId);
}
