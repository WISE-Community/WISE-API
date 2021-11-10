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
import org.wise.portal.presentation.web.controllers.APIControllerTest;

@RunWith(EasyMockRunner.class)
public class ClassmateDataControllerTest extends APIControllerTest {

  private final String DISCUSSION_TYPE = "Discussion";
  private final String OPEN_RESPONSE_TYPE = "OpenResponse";

  private String componentId1 = "component1";
  private String componentId2 = "component2";
  private String nodeId1 = "node1";

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
    expectComponentType(DISCUSSION_TYPE);
    replayAll();
    assertFalse(controller.isComponentType(run1, nodeId1, componentId1, OPEN_RESPONSE_TYPE));
    verifyAll();
  }

  @Test
  public void isComponentType_IsExpectedType_ShouldReturnTrue()
      throws IOException, JSONException, ObjectNotFoundException {
    expectComponentType(DISCUSSION_TYPE);
    replayAll();
    assertTrue(controller.isComponentType(run1, nodeId1, componentId1, DISCUSSION_TYPE));
    verifyAll();
  }

  @Test
  public void getProjectComponent_InvalidComponent_ShouldReturnNull()
      throws IOException, JSONException, ObjectNotFoundException {
    expectGetProjectContent(nodeId1, componentId1, DISCUSSION_TYPE);
    replayAll();
    assertNull(controller.getProjectComponent(run1, nodeId1, componentId2));
    verifyAll();
  }

  @Test
  public void getProjectComponent_ValidComponent_ShouldReturnNotNull()
      throws IOException, JSONException, ObjectNotFoundException {
    expectGetProjectContent(nodeId1, componentId1, DISCUSSION_TYPE);
    replayAll();
    assertNotNull(controller.getProjectComponent(run1, nodeId1, componentId1));
    verifyAll();
  }

  private void expectIsUserInRun(Run run, Group period, boolean isInRun)
      throws NoSuchMethodException, ObjectNotFoundException {
    expect(userService.retrieveUser(student1UserDetails)).andReturn(student1);
    expect(runService.isUserInRunAndPeriod(student1, run, period)).andReturn(isInRun);
  }

  private void expectComponentType(String componentType)
      throws IOException, ObjectNotFoundException {
    expect(projectService.getProjectContent(project1)).andReturn(
        "{ \"nodes\": [ { \"id\": \"node1\", \"components\": [ { \"id\": \"component1\", \"type\": \""
            + componentType + "\" } ] } ] }");
  }

  private void expectGetProjectContent(String nodeId, String componentId, String componentType)
      throws IOException {
    expect(projectService.getProjectContent(project1))
        .andReturn("{ \"nodes\": [ { \"id\": \"" + nodeId + "\", \"components\": [ { \"id\": \""
            + componentId + "\", \"type\": \"" + componentType + "\" } ] } ] }");
  }

  private void replayAll() {
    replay(groupService, projectService, runService, userService, vleService);
  }

  private void verifyAll() {
    verify(groupService, projectService, runService, userService, vleService);
  }
}
