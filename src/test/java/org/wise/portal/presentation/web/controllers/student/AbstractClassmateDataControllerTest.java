package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.util.List;

import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;

public abstract class AbstractClassmateDataControllerTest extends APIControllerTest {

  String COMPONENT_ID1 = "component1";
  String NODE_ID1 = "node1";
  String OPEN_RESPONSE_TYPE = "OpenResponse";
  String OTHER_COMPONENT_ID = "component2";
  String OTHER_COMPONENT_ID_NOT_ALLOWED = "component3";
  String OTHER_NODE_ID = "node2";
  String OTHER_NODE_ID_NOT_ALLOWED = "node3";
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

  protected void expectLatestStudentWork(Run run, Group period, String nodeId, String componentId,
      List<StudentWork> studentWork) {
    expect(vleService.getLatestStudentWork(run, period, nodeId, componentId)).andReturn(studentWork);
  }

  protected void expectLatestStudentWork(Run run, String nodeId, String componentId,
      List<StudentWork> studentWork) {
    expect(vleService.getLatestStudentWork(run, nodeId, componentId)).andReturn(studentWork);
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

  protected void expectPeriod(Long periodId, Group period) throws ObjectNotFoundException {
    expect(groupService.retrieveById(periodId)).andReturn(period);
  }

  protected void expectUser(StudentUserDetails studentUserDetails, User student) {
    expect(userService.retrieveUser(studentUserDetails)).andReturn(student);
  }

  protected void expectPeriodAndUser(User student, StudentUserDetails studentUserDetails,
      Long periodId, Group period) throws ObjectNotFoundException {
    expectPeriod(periodId, period);
    expectUser(studentUserDetails, student);
  }

  protected void setupStudent1InRun() throws ObjectNotFoundException {
    expectUser(student1UserDetails, student1);
  }

  protected void setupStudent1InRunAndInPeriod() throws ObjectNotFoundException {
    expectPeriodAndUser(student1, student1UserDetails, run1Period1Id, run1Period1);
  }

  protected void setupStudent2NotInRun() throws ObjectNotFoundException {
    expectUser(student2UserDetails, student2);
  }

  protected void setupStudent2NotInRunAndNotInPeriod() throws ObjectNotFoundException {
    expectPeriodAndUser(student2, student2UserDetails, run3Period4Id, run3Period4);
  }

  protected void setupStudent2InRunButNotInPeriod() throws ObjectNotFoundException {
    expectPeriodAndUser(student2, student2UserDetails, run1Period2Id, run1Period2);
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
        .append("      \"type\": \"node\",")
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
    replay(groupService, projectService, userService, vleService);
  }

  protected void verifyAll() {
    verify(groupService, projectService, userService, vleService);
  }
}
