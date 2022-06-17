package org.wise.portal.presentation.web.controllers.teacher.management;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.PeriodNotFoundException;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(EasyMockRunner.class)
public class ChangeWorkgroupPeriodControllerTest extends APIControllerTest {

  @TestSubject
  private ChangeWorkgroupPeriodController controller = new ChangeWorkgroupPeriodController();

  @Test
  public void changeWorkgroupPeriod_TeacherWithNoWritePermission_ThrowAccessDenied()
      throws PeriodNotFoundException, ObjectNotFoundException {
    expect(runService.retrieveById(runId3)).andReturn(run3);
    expect(runService.hasWritePermission(teacherAuth, run3)).andReturn(false);
    replay(runService);
    try {
      controller.changeWorkgroupPeriod(teacherAuth, runId3, workgroup1.getId(), 2L);
      fail("AccessDeniedException expected but was not thrown");
    } catch (AccessDeniedException e) {
    }
    verify(runService);
  }

  @Test
  public void changeWorkgroupPeriod_TeacherWithPermission_ChangePeriod()
      throws ObjectNotFoundException {
    expect(runService.retrieveById(run1.getId())).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(workgroupService.retrieveById(workgroup1.getId())).andReturn(workgroup1);
    expect(groupService.retrieveById(2L)).andReturn(run1Period2);
    workgroupService.changePeriod(workgroup1, run1Period2);
    expectLastCall();
    replay(runService, groupService, workgroupService);
    controller.changeWorkgroupPeriod(teacherAuth, runId1, workgroup1.getId(), 2L);
    verify(runService, groupService, workgroupService);
  }
}
