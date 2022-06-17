package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class ClassmateSummaryDataControllerTest extends AbstractClassmateDataControllerTest {

  @TestSubject
  private ClassmateSummaryDataController controller = new ClassmateSummaryDataController();

  @Test
  public void getClassmateSummaryWork_NotInRun_ThrowException()
      throws NoSuchMethodException, ObjectNotFoundException {
    setupStudent2NotInRunAndNotInPeriod();
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWork(studentAuth2, run3, run3Period4Id, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.PERIOD_SOURCE));
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWork_NotInPeriod_ThrowException()
      throws NoSuchMethodException, ObjectNotFoundException {
    setupStudent2InRunButNotInPeriod();
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWork(studentAuth2, run1, run1Period2Id, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.PERIOD_SOURCE));
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWork_NotSummaryComponent_ThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(OPEN_RESPONSE_TYPE);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWork(studentAuth, run1, run1Period1Id, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.PERIOD_SOURCE));
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWork_InvalidOtherComponent_ThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.SUMMARY_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.PERIOD_SOURCE);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWork(studentAuth, run1, run1Period1Id,
            OTHER_NODE_ID_NOT_ALLOWED, OTHER_COMPONENT_ID_NOT_ALLOWED, controller.PERIOD_SOURCE));
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWork_InRunSummaryComponentPeriod_ReturnWork()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    getClassmateSummaryWork_InRunSummaryComponent_ReturnWork(controller.PERIOD_SOURCE);
  }

  @Test
  public void getClassmateSummaryWork_InRunSummaryComponentAllPeriods_ReturnWork()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    getClassmateSummaryWork_InRunSummaryComponent_ReturnWork(controller.ALL_PERIODS_SOURCE);
  }

  private void getClassmateSummaryWork_InRunSummaryComponent_ReturnWork(String source)
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.SUMMARY_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, source);
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    if (controller.PERIOD_SOURCE.equals(source)) {
      expectLatestStudentWork(run1, run1Period1, OTHER_NODE_ID, OTHER_COMPONENT_ID, studentWork);
    } else if (controller.ALL_PERIODS_SOURCE.equals(source)) {
      expectLatestStudentWork(run1, OTHER_NODE_ID, OTHER_COMPONENT_ID, studentWork);
    }
    replayAll();
    try {
      List<StudentWork> classmateSummaryWork = controller.getClassmateSummaryWork(studentAuth,
          run1, run1Period1Id, OTHER_NODE_ID, OTHER_COMPONENT_ID, source);
      assertEquals(classmateSummaryWork, studentWork);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateSummaryAnnotations_InRunSummaryComponentPeriod_ReturnAnnotations()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    getClassmateSummaryAnnotations_InRunSummaryComponent_ReturnAnnotations(
        controller.PERIOD_SOURCE);
  }

  @Test
  public void getClassmateSummaryAnnotations_InRunSummaryComponentAllPeriods_ReturnAnnotations()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    getClassmateSummaryAnnotations_InRunSummaryComponent_ReturnAnnotations(
        controller.ALL_PERIODS_SOURCE);
  }

  private void getClassmateSummaryAnnotations_InRunSummaryComponent_ReturnAnnotations(String source)
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.SUMMARY_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, source);
    Annotation annotation1 = createAnnotation(workgroup1, "score", new Timestamp(1000));
    Annotation annotation2 = createAnnotation(workgroup2, "autoScore", new Timestamp(2000));
    Annotation annotation3 = createAnnotation(workgroup2, "autoScore", new Timestamp(3000));
    List<Annotation> allAnnotations = Arrays.asList(annotation1, annotation2, annotation3);
    if (controller.PERIOD_SOURCE.equals(source)) {
      expectAnnotations(run1, run1Period1, OTHER_NODE_ID, OTHER_COMPONENT_ID, allAnnotations);
    } else if (controller.ALL_PERIODS_SOURCE.equals(source)) {
      expectAnnotations(run1, OTHER_NODE_ID, OTHER_COMPONENT_ID, allAnnotations);
    }
    replayAll();
    try {
      List<Annotation> classmateSummaryAnnotations = controller.getClassmateSummaryScores(
          studentAuth, run1, run1Period1Id, OTHER_NODE_ID, OTHER_COMPONENT_ID, source);
      List<Annotation> latestAnnotations = Arrays.asList(annotation1, annotation3);
      assertEquals(classmateSummaryAnnotations, latestAnnotations);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  private Annotation createAnnotation(Workgroup toWorkgroup, String type,
      Timestamp serverSaveTime) {
    Annotation annotation = new Annotation();
    annotation.setToWorkgroup(toWorkgroup);
    annotation.setType(type);
    annotation.setServerSaveTime(serverSaveTime);
    return annotation;
  }

  protected void expectComponentType(String nodeId, String componentId, String componentType,
      String otherNodeId, String otherComponentId, String source)
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
        .append("          \"summaryNodeId\": \"" + otherNodeId + "\",")
        .append("          \"summaryComponentId\": \"" + otherComponentId + "\",")
        .append("          \"source\": \"" + source + "\",")
        .append("        }")
        .append("      ]")
        .append("    }")
        .append("  ]")
        .append("}")
        .toString();
    expect(projectService.getProjectContent(project1)).andReturn(projectJSONString);
  }
}
