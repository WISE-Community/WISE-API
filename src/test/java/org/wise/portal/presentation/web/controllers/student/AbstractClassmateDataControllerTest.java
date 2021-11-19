package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.util.List;

import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;

public abstract class AbstractClassmateDataControllerTest extends APIControllerTest {

  String COMPONENT_ID1 = "component1";
  String NODE_ID1 = "node1";
  String OPEN_RESPONSE_TYPE = "OpenResponse";
  String SHOULD_NOT_HAVE_THROWN_EXCEPTION = "Should not have thrown an exception";

  protected void expectStudentWork(List<StudentWork> studentWork) {
    expectStudentWork(run1, run1Period1, NODE_ID1, COMPONENT_ID1, studentWork);
  }

  protected void expectStudentWork(Run run, String nodeId, String componentId,
      List<StudentWork> studentWork) {
    expect(vleService.getStudentWork(run, nodeId, componentId)).andReturn(studentWork);
  }

  protected void expectStudentWork(Run run, Group period, String nodeId, String componentId,
      List<StudentWork> studentWork) {
    expect(vleService.getStudentWork(run, period, nodeId, componentId)).andReturn(studentWork);
  }

  protected void expectAnnotations(List<Annotation> annotations) {
    expectAnnotations(run1, run1Period1, NODE_ID1, COMPONENT_ID1, annotations);
  }

  protected void expectAnnotations(Run run, String nodeId, String componentId,
      List<Annotation> annotations) {
    expect(vleService.getAnnotations(run, nodeId, componentId)).andReturn(annotations);
  }

  protected void expectAnnotations(Run run, Group period, String nodeId, String componentId,
      List<Annotation> annotations) {
    expect(vleService.getAnnotations(run, period, nodeId, componentId)).andReturn(annotations);
  }

  protected void expectIsUserInRun(boolean isInRun)
      throws NoSuchMethodException, ObjectNotFoundException {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(groupService.retrieveById(run1Period1Id)).andReturn(run1Period1);
    expect(userService.retrieveUser(student1UserDetails)).andReturn(student1);
    expect(runService.isUserInRunAndPeriod(student1, run1, run1Period1)).andReturn(isInRun);
  }

  protected void expectComponentType(String componentType)
      throws IOException, ObjectNotFoundException {
    expectComponentType(NODE_ID1, COMPONENT_ID1, componentType);
  }

  protected void expectComponentType(String nodeId, String componentId, String componentType)
      throws IOException, ObjectNotFoundException {
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

  protected void replayAll() {
    replay(groupService, projectService, runService, userService, vleService);
  }

  protected void verifyAll() {
    verify(groupService, projectService, runService, userService, vleService);
  }
}