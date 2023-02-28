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
  public void getClassmateSummaryWorkInPeriod_NotInRun_ShouldThrowException()
      throws NoSuchMethodException, ObjectNotFoundException {
    setupStudent2NotInRunAndNotInPeriod();
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWorkInPeriod(studentAuth2, run3, run3Period4Id,
            OTHER_NODE_ID, OTHER_COMPONENT_ID));
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWorkInPeriod_NotInPeriod_ShouldThrowException()
      throws NoSuchMethodException, ObjectNotFoundException {
    setupStudent2InRunButNotInPeriod();
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWorkInPeriod(studentAuth2, run1, run1Period2Id,
            OTHER_NODE_ID, OTHER_COMPONENT_ID));
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWorkInPeriod_NotSummaryComponent_ShouldThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(OPEN_RESPONSE_TYPE);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWorkInPeriod(studentAuth, run1, run1Period1Id,
            OTHER_NODE_ID, OTHER_COMPONENT_ID));
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWorkInPeriod_InvalidOtherComponent_ShouldThrowException()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.SUMMARY_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.PERIOD_SOURCE);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWorkInPeriod(studentAuth, run1, run1Period1Id,
            OTHER_NODE_ID_NOT_ALLOWED, OTHER_COMPONENT_ID_NOT_ALLOWED));
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWorkInPeriod_InPeriodSummaryComponent_ShouldReturnWork()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.SUMMARY_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.PERIOD_SOURCE);
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectLatestStudentWork(run1, run1Period1, OTHER_NODE_ID, OTHER_COMPONENT_ID, studentWork);
    replayAll();
    try {
      List<StudentWork> classmateSummaryWork = controller.getClassmateSummaryWorkInPeriod(
          studentAuth, run1, run1Period1Id, OTHER_NODE_ID, OTHER_COMPONENT_ID);
      assertEquals(classmateSummaryWork, studentWork);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWorkInClass_InRunSummaryComponent_ShouldReturnWork()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRun();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.SUMMARY_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.ALL_PERIODS_SOURCE);
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectLatestStudentWork(run1, OTHER_NODE_ID, OTHER_COMPONENT_ID, studentWork);
    replayAll();
    try {
      List<StudentWork> classmateSummaryWork = controller
          .getClassmateSummaryWorkInClass(studentAuth, run1, OTHER_NODE_ID, OTHER_COMPONENT_ID);
      assertEquals(classmateSummaryWork, studentWork);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateSummaryScoresInPeriod_InPeriodSummaryComponent_ShouldReturnAnnotations()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRunAndInPeriod();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.SUMMARY_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.PERIOD_SOURCE);
    Annotation annotation1 = createAnnotation(workgroup1, "score", new Timestamp(1000));
    Annotation annotation2 = createAnnotation(workgroup2, "autoScore", new Timestamp(2000));
    Annotation annotation3 = createAnnotation(workgroup2, "autoScore", new Timestamp(3000));
    List<Annotation> allAnnotations = Arrays.asList(annotation1, annotation2, annotation3);
    expectAnnotations(run1, run1Period1, OTHER_NODE_ID, OTHER_COMPONENT_ID, allAnnotations);
    replayAll();
    try {
      List<Annotation> classmateSummaryAnnotations = controller.getClassmateSummaryScoresInPeriod(
          studentAuth, run1, run1Period1Id, OTHER_NODE_ID, OTHER_COMPONENT_ID);
      List<Annotation> latestAnnotations = Arrays.asList(annotation1, annotation3);
      assertEquals(classmateSummaryAnnotations, latestAnnotations);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateSummaryScoresInClass_InRunSummaryComponent_ShouldReturnAnnotations()
      throws IOException, NoSuchMethodException, ObjectNotFoundException {
    setupStudent1InRun();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.SUMMARY_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID, controller.ALL_PERIODS_SOURCE);
    Annotation annotation1 = createAnnotation(workgroup1, "score", new Timestamp(1000));
    Annotation annotation2 = createAnnotation(workgroup2, "autoScore", new Timestamp(2000));
    Annotation annotation3 = createAnnotation(workgroup2, "autoScore", new Timestamp(3000));
    List<Annotation> allAnnotations = Arrays.asList(annotation1, annotation2, annotation3);
    expectAnnotations(run1, OTHER_NODE_ID, OTHER_COMPONENT_ID, allAnnotations);
    replayAll();
    try {
      List<Annotation> classmateSummaryAnnotations = controller
          .getClassmateSummaryScoresInClass(studentAuth, run1, OTHER_NODE_ID, OTHER_COMPONENT_ID);
      List<Annotation> latestAnnotations = Arrays.asList(annotation1, annotation3);
      assertEquals(classmateSummaryAnnotations, latestAnnotations);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWorkInPeriod_TeacherOfRun_ShouldReturnWork()
      throws ObjectNotFoundException {
    expectPeriod(run1Period1Id, run1Period1);
    setupTeacher1();
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectLatestStudentWork(run1, run1Period1, OTHER_NODE_ID, OTHER_COMPONENT_ID, studentWork);
    replayAll();
    try {
      List<StudentWork> classmateSummaryWork = controller.getClassmateSummaryWorkInPeriod(
          teacherAuth, run1, run1Period1Id, OTHER_NODE_ID, OTHER_COMPONENT_ID);
      assertEquals(classmateSummaryWork, studentWork);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWorkInClass_TeacherOfRun_ShouldReturnWork()
      throws ObjectNotFoundException {
    setupTeacher1();
    List<StudentWork> studentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectLatestStudentWork(run1, OTHER_NODE_ID, OTHER_COMPONENT_ID, studentWork);
    replayAll();
    try {
      List<StudentWork> classmateSummaryWork = controller
          .getClassmateSummaryWorkInClass(teacherAuth, run1, OTHER_NODE_ID, OTHER_COMPONENT_ID);
      assertEquals(classmateSummaryWork, studentWork);
    } catch (Exception e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getClassmateSummaryWorkInPeriod_NotTeacherOfRun_ShouldThrowException()
      throws ObjectNotFoundException {
    expectPeriod(run1Period1Id, run1Period1);
    setupTeacher2();
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmateSummaryWorkInPeriod(teacherAuth2, run1, run1Period1Id,
            OTHER_NODE_ID, OTHER_COMPONENT_ID));
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
    String projectJSONString = new StringBuilder().append("{").append("  \"nodes\": [")
        .append("    {").append("      \"id\": \"" + nodeId + "\",")
        .append("      \"type\": \"node\",").append("      \"components\": [").append("        {")
        .append("          \"id\": \"" + componentId + "\",")
        .append("          \"type\": \"" + componentType + "\",")
        .append("          \"summaryNodeId\": \"" + otherNodeId + "\",")
        .append("          \"summaryComponentId\": \"" + otherComponentId + "\",")
        .append("          \"source\": \"" + source + "\",").append("        }").append("      ]")
        .append("    }").append("  ]").append("}").toString();
    expect(projectService.getProjectContent(project1)).andReturn(projectJSONString);
  }
}
