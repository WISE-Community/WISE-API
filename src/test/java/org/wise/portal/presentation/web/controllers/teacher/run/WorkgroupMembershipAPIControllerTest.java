package org.wise.portal.presentation.web.controllers.teacher.run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.domain.impl.ChangeWorkgroupParameters;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

@RunWith(EasyMockRunner.class)
public class WorkgroupMembershipAPIControllerTest extends APIControllerTest {

  @TestSubject
  private WorkgroupMembershipAPIController controller = new WorkgroupMembershipAPIController();

  @Test
  public void moveUserBetweenWorkgroups_allParamsSet_updateMembership() throws Exception {
    String paramString = "{\"workgroupIdFrom\":\"1\",\"workgroupIdTo\":\"2\",\"periodId\":\"1\"}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode postedParams = objectMapper.readTree(paramString);
    expect(userService.retrieveById(student1Id)).andReturn(student1);
    expect(workgroupService.retrieveById(1L)).andReturn(workgroup1);
    expect(workgroupService.retrieveById(2L)).andReturn(workgroup2);
    expect(workgroupService.updateWorkgroupMembership(isA(ChangeWorkgroupParameters.class)))
        .andReturn(new WorkgroupImpl());
    replay(userService, workgroupService);
    controller.moveUserBetweenWorkgroups(run1, student1Id, postedParams);
    verify(userService, workgroupService);
  }
}
