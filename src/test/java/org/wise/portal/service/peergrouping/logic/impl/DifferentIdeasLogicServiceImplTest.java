package org.wise.portal.service.peergrouping.logic.impl;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.impl.WorkgroupLogicComparable;
import org.wise.vle.domain.annotation.wise5.Annotation;

@RunWith(EasyMockRunner.class)
public class DifferentIdeasLogicServiceImplTest extends PeerGroupAnnotationLogicServiceImplTest {

  @TestSubject
  private DifferentIdeasLogicServiceImpl service = new DifferentIdeasLogicServiceImpl();

  List<Annotation> workgroup1IdeasOnly, classroomIdeaAnnotations;
  Annotation workgroup1Ideas, workgroup2Ideas, workgroup3Ideas, workgroup4Ideas, workgroup5Ideas;
  String ideas1 = createIdeaString(true, false, false, false);
  String ideas1And2 = createIdeaString(true, true, false, false);
  String ideas3 = createIdeaString(false, false, true, false);
  String ideas3And4 = createIdeaString(false, false, true, true);
  String ideas4 = createIdeaString(false, false, false, true);

  @Before
  public void setup() throws Exception {
    super.setup();
    setLogic("maximize");
    workgroup1Ideas = createIdeasAnnotation(workgroup1, ideas1And2);
    workgroup2Ideas = createIdeasAnnotation(workgroup2, ideas3);
    workgroup3Ideas = createIdeasAnnotation(workgroup3, ideas1);
    workgroup4Ideas = createIdeasAnnotation(workgroup4, ideas4);
    workgroup5Ideas = createIdeasAnnotation(workgroup5, ideas3And4);
    workgroup1IdeasOnly = Arrays.asList(workgroup1Ideas);
    classroomIdeaAnnotations = Arrays.asList(workgroup1Ideas, workgroup2Ideas, workgroup3Ideas,
        workgroup4Ideas, workgroup5Ideas);
  }

  private String createIdeaString(boolean idea1Detected, boolean idea2Detected,
      boolean idea3Detected, boolean idea4Detected) {
    return "[{\"name\":\"1\",\"detected\":" + idea1Detected + "}," + "{\"name\":\"2\",\"detected\":"
        + idea2Detected + "}," + "{\"name\":\"3\",\"detected\":" + idea3Detected + "},"
        + "{\"name\":\"4\",\"detected\":" + idea4Detected + "}]";
  }

  private Annotation createIdeasAnnotation(Workgroup workgroup, String ideas) {
    Annotation annotation = new Annotation();
    annotation.setToWorkgroup(workgroup);
    annotation.setPeriod(period1);
    annotation.setType("autoScore");
    annotation.setData("{\"ideas\":" + ideas + "}");
    return annotation;
  }

  @Test
  public void canCreatePeerGroup_WorkgroupHasNoIdeas_ReturnFalse() {
    expectAnnotations(emptyAnnotations);
    assertFalse(service.canCreatePeerGroup(workgroup1, workgroupsNotInPeerGroup, peerGrouping));
    verify(annotationDao);
  }

  @Test
  public void canCreatePeerGroup_NotEnoughUnpairedMembersWithIdeas_ReturnFalse() {
    expectAnnotations(workgroup1IdeasOnly);
    assertFalse(service.canCreatePeerGroup(workgroup1, workgroupsNotInPeerGroup, peerGrouping));
    verify(annotationDao);
  }

  @Test
  public void canCreatePeerGroup_EnoughUnpairedMembersWithIdeas_ReturnTrue() {
    expectAnnotations(classroomIdeaAnnotations);
    assertTrue(service.canCreatePeerGroup(workgroup1, workgroupsNotInPeerGroup, peerGrouping));
    verify(annotationDao);
  }

  @Test
  public void getPossibleMembersInOrder_AnyMode_RandomOrder() {
    setLogic("any");
    expectAnnotations(classroomIdeaAnnotations);
    TreeSet<WorkgroupLogicComparable> possibleMembersInOrder = service
        .getPossibleMembersInOrder(possibleMembers, workgroup1, peerGrouping);
    assertEquals(4, possibleMembersInOrder.size());
    // there's nothing else that we can test here, since the workgroups all have at least
    // one different idea and can be in any random order
    verify(annotationDao);
  }

  @Test
  public void getPossibleMembersInOrder_MaximizeMode_MaximizeOrder() {
    expectAnnotations(classroomIdeaAnnotations);
    TreeSet<WorkgroupLogicComparable> possibleMembersInOrder = service
        .getPossibleMembersInOrder(possibleMembers, workgroup1, peerGrouping);
    assertOneMatch(getWorkgroups(possibleMembersInOrder),
        Arrays.asList(workgroup3, workgroup2, workgroup4, workgroup5),
        Arrays.asList(workgroup3, workgroup4, workgroup2, workgroup5));
    verify(annotationDao);
  }

  protected String getLogicFunctionName() {
    return "differentIdeas";
  }
}
