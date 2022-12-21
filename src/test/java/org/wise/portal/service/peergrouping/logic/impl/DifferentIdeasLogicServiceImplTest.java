package org.wise.portal.service.peergrouping.logic.impl;

import java.util.ArrayList;
import java.util.Iterator;
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
    workgroup1IdeasOnly = new ArrayList<Annotation>();
    workgroup1IdeasOnly.add(workgroup1Ideas);
    classroomIdeaAnnotations = new ArrayList<Annotation>();
    classroomIdeaAnnotations.add(workgroup1Ideas);
    classroomIdeaAnnotations.add(workgroup2Ideas);
    classroomIdeaAnnotations.add(workgroup3Ideas);
    classroomIdeaAnnotations.add(workgroup4Ideas);
    classroomIdeaAnnotations.add(workgroup5Ideas);
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
    expect(annotationDao.getAnnotationsByParams(null, run, run1Period1, null, null, nodeId,
        componentId, null, null, null, "autoScore")).andReturn(emptyAnnotations);
    replay(annotationDao);
    assertFalse(service.canCreatePeerGroup(workgroup1, workgroupsNotInPeerGroup, peerGrouping));
    verify(annotationDao);
  }

  @Test
  public void canCreatePeerGroup_NotEnoughUnpairedMembersWithIdeas_ReturnFalse() {
    expect(annotationDao.getAnnotationsByParams(null, run, run1Period1, null, null, nodeId,
        componentId, null, null, null, "autoScore")).andReturn(workgroup1IdeasOnly);
    replay(annotationDao);
    assertFalse(service.canCreatePeerGroup(workgroup1, workgroupsNotInPeerGroup, peerGrouping));
    verify(annotationDao);
  }

  @Test
  public void canCreatePeerGroup_EnoughUnpairedMembersWithIdeas_ReturnTrue() {
    expect(annotationDao.getAnnotationsByParams(null, run, run1Period1, null, null, nodeId,
        componentId, null, null, null, "autoScore")).andReturn(classroomIdeaAnnotations);
    replay(annotationDao);
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
    // the workgroups can be in any random order since they all have at least one different idea
    verify(annotationDao);
  }

  @Test
  public void getPossibleMembersInOrder_MaximizeMode_MaximizeOrder() {
    expectAnnotations(classroomIdeaAnnotations);
    TreeSet<WorkgroupLogicComparable> possibleMembersInOrder = service
        .getPossibleMembersInOrder(possibleMembers, workgroup1, peerGrouping);
    assertEquals(4, possibleMembersInOrder.size());
    Iterator<WorkgroupLogicComparable> iterator = possibleMembersInOrder.iterator();
    assertEquals(workgroup3, iterator.next().getWorkgroup());
    Workgroup nextWorkgroup = iterator.next().getWorkgroup();
    assertTrue(nextWorkgroup.equals(workgroup2) || nextWorkgroup.equals(workgroup4));
    nextWorkgroup = iterator.next().getWorkgroup();
    assertTrue(nextWorkgroup.equals(workgroup2) || nextWorkgroup.equals(workgroup4));
    assertEquals(workgroup5, iterator.next().getWorkgroup());
    verify(annotationDao);
  }

  private void expectAnnotations(List<Annotation> classroomAnnotations) {
    expect(annotationDao.getAnnotationsByParams(null, run, run1Period1, null, null, nodeId,
        componentId, null, null, null, "autoScore")).andReturn(classroomAnnotations);
    replay(annotationDao);
  }

  protected String getLogicFunctionName() {
    return "differentIdeas";
  }
}
