package org.wise.portal.service.work.impl;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.service.WISEServiceTest;
import org.wise.portal.spring.data.redis.MessagePublisher;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class BroadcastStudentWorkServiceImplTest extends WISEServiceTest {

  @TestSubject
  private BroadcastStudentWorkServiceImpl service = new BroadcastStudentWorkServiceImpl();

  @Mock
  private MessagePublisher redisPublisher;

  private StudentWork studentWork;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    studentWork = new StudentWork();
    studentWork.setRun(run1);
    PersistentGroup period = new PersistentGroup();
    period.setId(2L);
    studentWork.setPeriod(period);
  }

  @Test
  public void broadcastToClassroom_PublishClassroomTopic() {
    redisPublisher.publish("{\"topic\":\"/topic/classroom/1/2\"," +
        "\"studentWork\":{\"periodId\":2,\"runId\":1},\"type\":\"studentWorkToClassroom\"}");
    expectLastCall();
    replay(redisPublisher);
    service.broadcastToClassroom(studentWork);
    verify(redisPublisher);
  }

  @Test
  public void broadcastToTeacher_PublishTeacherRunTopic() {
    redisPublisher.publish("{\"topic\":\"/topic/teacher/1\"," +
        "\"studentWork\":{\"periodId\":2,\"runId\":1},\"type\":\"studentWorkToTeacher\"}");
    expectLastCall();
    replay(redisPublisher);
    service.broadcastToTeacher(studentWork);
    verify(redisPublisher);
  }
}
