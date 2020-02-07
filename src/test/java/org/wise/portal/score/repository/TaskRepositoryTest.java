package repository;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.wise.Application;
import org.wise.portal.score.domain.Task;
import org.wise.portal.score.repository.TaskRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Tests for tasks and requests
 *
 * @author Anthony Perritano
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Application.class})
@SpringBootTest
public class TaskRepositoryTest extends TestCase {


  @Autowired
  TaskRepository taskRepository;
  List<Task> tasks;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.tasks = this.createTasks();
    this.taskRepository.saveAll(this.tasks);
  }

  @Test
  public void testTaskCreation() {
    Optional<Task> found = this.taskRepository.findById(tasks.get(0).getId());
    assertTrue(found.isPresent());
  }

  @Test
  public void testFindAllTasksByRunId() {
    Long runId = Long.valueOf(7);
    List<Task> allByRunId = this.taskRepository.findAllByRunId(runId);
    if(allByRunId.isEmpty()) {
      assert(false);
    }
    for (Task task: allByRunId) {
      if(!task.getRunId().equals(runId)) {
        assert(false);
      }
    }
    assert(true);
  }

  @Test
  public void testFindAllTasksByRunIdAndPeriodId() {
    Long runId = Long.valueOf(7);
    Long periodId = Long.valueOf(2);
    List<Task> allByRunId = this.taskRepository.findAllByRunIdAndPeriodId(runId,periodId);
    if(allByRunId.isEmpty()) {
      assert(false);
    }
    for (Task task: allByRunId) {
      if(!(task.getRunId().equals(runId) && task.getPeriodId().equals(periodId))) {
        assert(false);
      }
    }
   assert(true);
  }
  @Test
  public void testFindAllTasksByRunIdAndPeriodIdAndWorkgroupId() {
    Long runId = Long.valueOf(7);
    Long periodId = Long.valueOf(2);
    Long workgroupId = Long.valueOf(3);
    List<Task> allByRunId = this.taskRepository.findAllByRunIdAndPeriodIdAndWorkgroupId(runId,periodId, workgroupId);
    if(allByRunId.isEmpty()) {
      assert(false);
    }
    for (Task task: allByRunId) {
      if(!(task.getRunId().equals(runId) && task.getPeriodId().equals(periodId) && task.getWorkgroupId().equals(workgroupId))) {
        assert(false);
      }
    }
    assert(true);
  }

  private List<Task> createTasks() {
    List<Task> tasks = new ArrayList<Task>();
    Calendar now = Calendar.getInstance();
    Timestamp startTimestamp = new Timestamp(now.getTimeInMillis());
    Timestamp endTimestamp = new Timestamp(now.getTimeInMillis() + 1000);
    Task task = Task.builder().
      name("activity 1")
//      .startTime(startTimestamp)
//      .endTime(endTimestamp)
      .runId((long) 7)
      .periodId((long) 2)
      .projectId((long) 5)
      .workgroupId((long) 1)
//      .complete(false)
      .build();

    tasks.add(task);

    task = Task.builder().
      name("activity 1")
//      .startTime(startTimestamp)
//      .endTime(endTimestamp)
      .runId((long) 7)
      .periodId((long) 2)
      .projectId((long) 5)
      .workgroupId((long) 3)
//      .complete(false)
      .build();

    tasks.add(task);

    task = Task.builder().
      name("activity 2")
//      .startTime(startTimestamp)
//      .endTime(endTimestamp)
      .runId((long) 6)
      .periodId((long) 3)
      .projectId((long) 5)
      .workgroupId((long) 2)
//      .complete(true)
      .build();

    tasks.add(task);

    return tasks;
  }
}
