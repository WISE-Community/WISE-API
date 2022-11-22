package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.vle.wise5.AnnotationService;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;
import org.wise.vle.domain.work.StudentWorkAnnotation;

@RunWith(EasyMockRunner.class)
public class PeerGroupStudentWorkAnnotationControllerTest
    extends AbstractPeerGroupWorkControllerTest {

  @TestSubject
  private PeerGroupStudentWorkAnnotationController controller = new PeerGroupStudentWorkAnnotationController();

  @Mock
  private AnnotationService annotationService;

  @Test
  public void getStudentDataForDynamicPrompt_UserNotInPeerGroup_AccessDenied() {
    expectUserNotInPeerGroup();
    replayAll();
    try {
      controller.getStudentDataForDynamicPrompt(peerGroup1, run1Node2Id, run1Component2Id,
          studentAuth);
      fail("Expected AccessDeniedException, but was not thrown");
    } catch (AccessDeniedException e) {
    } catch (Exception e) {
      fail("Expected AccessDeniedException, but was not thrown");
    }
    verifyAll();
  }

  @Test
  public void getStudentDataForDynamicPrompt_InvalidDynamicPromptContent_Exception()
      throws IOException {
    expectUserInPeerGroup();
    expectInvalidDynamicPromptContent();
    replayAll();
    try {
      controller.getStudentDataForDynamicPrompt(peerGroup1, run1Node2Id, run1Component2Id,
          studentAuth);
      fail("Expected Exception, but was not thrown");
    } catch (Exception e) {
    }
    verifyAll();
  }

  private void expectInvalidDynamicPromptContent() throws IOException {
    String project_sans_reference_component = "{\"nodes\": [{\"id\": \"" + run1Node2Id
        + "\",\"type\": \"node\"," + "\"components\": [{\"id\": \"" + run1Component2Id + "\"}]}]}";
    expect(projectService.getProjectContent(project1)).andReturn(project_sans_reference_component);
  }

  @Test
  public void getStudentDataForDynamicPrompt_ReturnReferenceComponentWork() throws Exception {
    expectUserInPeerGroup();
    expectValidDynamicPromptContent();
    expect(annotationService.getLatest(peerGroup1.getMembers(), run1Node1Id, run1Component1Id,
        "autoScore"))
        .andReturn(Arrays.asList(createAnnotation(workgroup1), createAnnotation(workgroup2)));
    replayAll();
    List<StudentWorkAnnotation> studentWorkAnnotation = controller
        .getStudentDataForDynamicPrompt(peerGroup1, run1Node2Id, run1Component2Id, studentAuth);
    assertEquals(2, studentWorkAnnotation.size());
    assertEquals(workgroup1, studentWorkAnnotation.get(0).getWorkgroup());
    assertEquals(workgroup2, studentWorkAnnotation.get(1).getWorkgroup());
    verifyAll();
  }

  private void expectValidDynamicPromptContent() throws IOException {
    String project_with_reference_component = "{\"nodes\": [{\"id\": \"" + run1Node2Id
        + "\",\"type\": \"node\"," + "\"components\": [{\"id\": \"" + run1Component2Id
        + "\",\"dynamicPrompt\":{\"peerGroupingTag\":\"" + peerGrouping1Tag
        + "\", \"referenceComponent\": {\"nodeId\":\"" + run1Node1Id + "\",\"componentId\":\""
        + run1Component1Id + "\"}}}]}]}";
    expect(projectService.getProjectContent(project1)).andReturn(project_with_reference_component);
  }

  private Annotation createAnnotation(Workgroup workgroup) {
    Annotation annotation = new Annotation();
    annotation.setToWorkgroup(workgroup);
    annotation.setStudentWork(new StudentWork());
    return annotation;
  }

  @Override
  public void replayAll() {
    super.replayAll();
    replay(annotationService);
  }

  @Override
  public void verifyAll() {
    super.verifyAll();
    verify(annotationService);
  }
}
