package org.wise.portal.presentation.web.controllers.teacher.run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.domain.impl.ChangeWorkgroupParameters;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

@RunWith(EasyMockRunner.class)
public class WorkgroupMembershipAPIControllerTest extends APIControllerTest {

  JsonNode postedParams;

  @TestSubject
  private WorkgroupMembershipAPIController controller = new WorkgroupMembershipAPIController();

  @Before
  public void init() throws Exception {
    String paramString = "{\"workgroupIdTo\":\"2\",\"periodId\":\"1\"}";
    ObjectMapper objectMapper = new ObjectMapper();
    postedParams = objectMapper.readTree(paramString);
  }

  @Test
  public void moveUserBetweenWorkgroups_allParamsSet_updateMembership() throws Exception {
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveById(student1Id)).andReturn(student1);
    expect(workgroupService.retrieveById(2L)).andReturn(workgroup2);
    expect(workgroupService.updateWorkgroupMembership(isA(ChangeWorkgroupParameters.class)))
        .andReturn(new WorkgroupImpl());
    replay(runService, userService, workgroupService);
    controller.moveUserBetweenWorkgroups(teacherAuth, run1, student1Id, postedParams);
    verify(runService, userService, workgroupService);
  }

  @Test
  public void moveUserBetweenWorkgroups_teacherDoesNotHavePermission_throwsException()
      throws Exception {
    expect(runService.hasWritePermission(teacherAuth, run3)).andReturn(false);
    replay(runService);
    try {
      controller.moveUserBetweenWorkgroups(teacherAuth, run3, student1Id, postedParams);
      fail("Expected ResponseStatusException");
    } catch (ResponseStatusException e) {
    }
    verify(runService);
  }

  @Test
  public void moveUserBetweenWorkgroups_workgroupServiceUnableToMoveUser_throwsException()
      throws Exception {
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveById(student1Id)).andReturn(student1);
    expect(workgroupService.retrieveById(2L)).andReturn(workgroup2);
    expect(workgroupService.updateWorkgroupMembership(isA(ChangeWorkgroupParameters.class)))
        .andReturn(null);
    replay(runService, userService, workgroupService);
    try {
      controller.moveUserBetweenWorkgroups(teacherAuth, run1, student1Id, postedParams);
      fail("Expected ResponseStatusException");
    } catch (ResponseStatusException e) {
    }
    verify(runService, userService, workgroupService);
  }

}
