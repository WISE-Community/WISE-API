package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;

@RunWith(EasyMockRunner.class)
public class ClassmateDataControllerTest extends AbstractClassmateDataControllerTest {

  private final String COMPONENT_ID2 = "component2";
  private final String DISCUSSION_TYPE = "Discussion";
  private final String OPEN_RESPONSE_TYPE = "OpenResponse";

  private class ClassmateDataControllerImpl extends ClassmateDataController {
  }

  @TestSubject
  private ClassmateDataController controller = new ClassmateDataControllerImpl();

  @Test
  public void isUserInRunAndPeriod_NotInRun_ShouldReturnFalse()
      throws NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(run2, run2Period2, false);
    replayAll();
    assertFalse(controller.isUserInRunAndPeriod(studentAuth, run2, run2Period2));
    verifyAll();
  }

  @Test
  public void isUserInRunAndPeriod_NotInPeriod_ShouldReturnFalse()
      throws NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(run1, run1Period2, false);
    replayAll();
    assertFalse(controller.isUserInRunAndPeriod(studentAuth, run1, run1Period2));
    verifyAll();
  }

  @Test
  public void isUserInRunAndPeriod_InRun_ShouldReturnTrue()
      throws NoSuchMethodException, ObjectNotFoundException {
    expectIsUserInRun(run1, run1Period1, true);
    replayAll();
    assertTrue(controller.isUserInRunAndPeriod(studentAuth, run1, run1Period1));
    verifyAll();
  }

  @Test
  public void isComponentType_IsNotExpectedType_ShouldReturnFalse()
      throws IOException, JSONException, ObjectNotFoundException {
    expectComponentType(NODE_ID1, COMPONENT_ID1, DISCUSSION_TYPE);
    replayAll();
    assertFalse(controller.isComponentType(run1, NODE_ID1, COMPONENT_ID1, OPEN_RESPONSE_TYPE));
    verifyAll();
  }

  @Test
  public void isComponentType_IsExpectedType_ShouldReturnTrue()
      throws IOException, JSONException, ObjectNotFoundException {
    expectComponentType(NODE_ID1, COMPONENT_ID1, DISCUSSION_TYPE);
    replayAll();
    assertTrue(controller.isComponentType(run1, NODE_ID1, COMPONENT_ID1, DISCUSSION_TYPE));
    verifyAll();
  }

  @Test
  public void getProjectComponent_InvalidComponent_ShouldReturnNull()
      throws IOException, JSONException, ObjectNotFoundException {
    expectGetProjectContent(NODE_ID1, COMPONENT_ID1, DISCUSSION_TYPE);
    replayAll();
    assertNull(controller.getProjectComponent(run1, NODE_ID1, COMPONENT_ID2));
    verifyAll();
  }

  @Test
  public void getProjectComponent_ValidComponent_ShouldReturnNotNull()
      throws IOException, JSONException, ObjectNotFoundException {
    expectGetProjectContent(NODE_ID1, COMPONENT_ID1, DISCUSSION_TYPE);
    replayAll();
    assertNotNull(controller.getProjectComponent(run1, NODE_ID1, COMPONENT_ID1));
    verifyAll();
  }

  private void expectIsUserInRun(Run run, Group period, boolean isInRun)
      throws NoSuchMethodException, ObjectNotFoundException {
    expect(userService.retrieveUser(student1UserDetails)).andReturn(student1);
    expect(runService.isUserInRunAndPeriod(student1, run, period)).andReturn(isInRun);
  }

  private void expectGetProjectContent(String nodeId, String componentId, String componentType)
      throws IOException {
    String projectJSONString = new StringBuilder()
        .append("{")
        .append("  \"nodes\": [")
        .append("    {")
        .append("      \"id\": \"" + nodeId + "\",")
        .append("      \"components\": [")
        .append("        {")
        .append("          \"id\": \"" + componentId + "\",")
        .append("          \"type\": \"" + componentType + "\"")
        .append("        }")
        .append("      ]")
        .append("    }")
        .append("  ]")
        .append("}")
        .toString();
    expect(projectService.getProjectContent(project1)).andReturn(projectJSONString);
  }
}
