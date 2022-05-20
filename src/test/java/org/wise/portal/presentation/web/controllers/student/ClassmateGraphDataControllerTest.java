package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class ClassmateGraphDataControllerTest extends AbstractClassmateDataControllerTest {

  String SHOW_WORK_COMPONENT_ID = "component2";
  String SHOW_WORK_COMPONENT_ID_NOT_ALLOWED = "component3";
  String SHOW_WORK_NODE_ID = "node2";
  String SHOW_WORK_NODE_ID_NOT_ALLOWED = "node3";

  @TestSubject
  private ClassmateGraphDataController controller = new ClassmateGraphDataController();

  @Test
  public void getClassmateGraphWorkInPeriod_NotInRun_ThrowException()
      throws NoSuchMethodException, ObjectNotFoundException {
    setupStudent2NotInRunAndNotInPeriod();
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateGraphWorkInPeriod(studentAuth2, run3, NODE_ID1,
            COMPONENT_ID1, SHOW_WORK_NODE_ID, SHOW_WORK_COMPONENT_ID, run3Period4Id));
    verifyAll();
  }

  @Test
  public void getClassmateGraphWorkInPeriod_NotInPeriod_ThrowException()
      throws NoSuchMethodException, ObjectNotFoundException {
    setupStudent2InRunButNotInPeriod();
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateGraphWorkInPeriod(studentAuth2, run1, NODE_ID1,
            COMPONENT_ID1, SHOW_WORK_NODE_ID, SHOW_WORK_COMPONENT_ID, run1Period2Id));
    verifyAll();
  }

  @Test
  public void getClassmateGraphWorkInPeriod_NotGraphComponent_ThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(OPEN_RESPONSE_TYPE);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateGraphWorkInPeriod(studentAuth, run1, NODE_ID1,
            COMPONENT_ID1, SHOW_WORK_NODE_ID, SHOW_WORK_COMPONENT_ID, run1Period1Id));
    verifyAll();
  }

  @Test
  public void getClassmateGraphWorkInPeriod_InvalidNodeId_ThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    getClassmateGraphWorkInPeriod_InvalidField_ThrowException(SHOW_WORK_NODE_ID,
        SHOW_WORK_COMPONENT_ID, controller.PERIOD_SOURCE, SHOW_WORK_NODE_ID_NOT_ALLOWED,
        SHOW_WORK_COMPONENT_ID);
  }

  @Test
  public void getClassmateGraphWorkInPeriod_InvalidComponentId_ThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    getClassmateGraphWorkInPeriod_InvalidField_ThrowException(SHOW_WORK_NODE_ID,
        SHOW_WORK_COMPONENT_ID, controller.PERIOD_SOURCE, SHOW_WORK_NODE_ID,
        SHOW_WORK_COMPONENT_ID_NOT_ALLOWED);
  }

  private void getClassmateGraphWorkInPeriod_InvalidField_ThrowException(String actualNodeId,
      String actualComponentId, String actualSource, String requestedNodeId,
      String requestedComponentId)
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.GRAPH_TYPE, actualNodeId,
        actualComponentId, controller.SHOW_CLASSMATE_WORK_TYPE, actualSource);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateGraphWorkInPeriod(studentAuth, run1, NODE_ID1,
            COMPONENT_ID1, requestedNodeId, requestedComponentId, run1Period1Id));
    verifyAll();
  }

  @Test
  public void getClassmateGraphWorkInClass_InvalidSource_ThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRun();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.GRAPH_TYPE, SHOW_WORK_NODE_ID,
        SHOW_WORK_COMPONENT_ID, controller.SHOW_CLASSMATE_WORK_TYPE, controller.PERIOD_SOURCE);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateGraphWorkInClass(studentAuth, run1, NODE_ID1,
            COMPONENT_ID1, SHOW_WORK_NODE_ID, SHOW_WORK_COMPONENT_ID));
    verifyAll();
  }

  @Test
  public void getClassmateGraphWorkInPeriod_ValidParams_ReturnWork()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.GRAPH_TYPE, SHOW_WORK_NODE_ID,
        SHOW_WORK_COMPONENT_ID, controller.SHOW_CLASSMATE_WORK_TYPE, controller.PERIOD_SOURCE);
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectLatestStudentWork(run1, run1Period1, SHOW_WORK_NODE_ID, SHOW_WORK_COMPONENT_ID,
        studentWork);
    replayAll();
    try {
      List<StudentWork> classmateGraphWork = controller.getClassmateGraphWorkInPeriod(studentAuth,
          run1, NODE_ID1, COMPONENT_ID1, SHOW_WORK_NODE_ID, SHOW_WORK_COMPONENT_ID, run1Period1Id);
      assertEquals(classmateGraphWork, studentWork);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateGraphWorkInClass_ValidParams_ReturnWork()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRun();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.GRAPH_TYPE, SHOW_WORK_NODE_ID,
        SHOW_WORK_COMPONENT_ID, controller.SHOW_CLASSMATE_WORK_TYPE, controller.CLASS_SOURCE);
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectLatestStudentWork(run1, SHOW_WORK_NODE_ID, SHOW_WORK_COMPONENT_ID, studentWork);
    replayAll();
    try {
      List<StudentWork> classmateGraphWork = controller.getClassmateGraphWorkInClass(studentAuth,
          run1, NODE_ID1, COMPONENT_ID1, SHOW_WORK_NODE_ID, SHOW_WORK_COMPONENT_ID);
      assertEquals(classmateGraphWork, studentWork);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  protected void expectComponentType(String nodeId, String componentId, String componentType,
      String connectedComponentNodeId, String connectedComponentComponentId,
      String connectedComponentType, String showClassmateWorkSource)
      throws IOException, ObjectNotFoundException {
    String projectJSONString = new StringBuilder()
        .append("{")
        .append("  \"nodes\": [")
        .append("    {")
        .append("      \"id\": \"" + nodeId + "\",")
        .append("      \"type\": \"node\",")
        .append("      \"components\": [")
        .append("        {")
        .append("          \"id\": \"" + componentId + "\",")
        .append("          \"type\": \"" + componentType + "\",")
        .append("          \"connectedComponents\": [")
        .append("            {")
        .append("              \"nodeId\": \"" + connectedComponentNodeId + "\",")
        .append("              \"componentId\": \"" + connectedComponentComponentId + "\",")
        .append("              \"type\": \"" + connectedComponentType + "\",")
        .append("              \"showClassmateWorkSource\": \"" + showClassmateWorkSource + "\"")
        .append("            }")
        .append("          ]")
        .append("        }")
        .append("      ]")
        .append("    }")
        .append("  ]")
        .append("}")
        .toString();
    expect(projectService.getProjectContent(project1)).andReturn(projectJSONString);
  }
}
