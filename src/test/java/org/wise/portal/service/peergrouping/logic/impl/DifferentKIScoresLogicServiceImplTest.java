package org.wise.portal.service.peergrouping.logic.impl;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.impl.WorkgroupLogicComparable;
import org.wise.vle.domain.annotation.wise5.Annotation;

@RunWith(EasyMockRunner.class)
public class DifferentKIScoresLogicServiceImplTest extends PeerGroupAnnotationLogicServiceImplTest {

  @TestSubject
  private DifferentKIScoresLogicServiceImpl service = new DifferentKIScoresLogicServiceImpl();

  List<Annotation> classroomAnnotations;
  Annotation workgroup1Score, workgroup2Score, workgroup3Score, workgroup4Score, workgroup5Score;

  @Before
  public void setup() throws Exception {
    super.setup();
    workgroup1Score = createKIScoreAnnotation(workgroup1, 3);
    workgroup2Score = createKIScoreAnnotation(workgroup2, 2);
    workgroup3Score = createKIScoreAnnotation(workgroup3, 3);
    workgroup4Score = createKIScoreAnnotation(workgroup4, 4);
    workgroup5Score = createKIScoreAnnotation(workgroup5, 5);
    classroomAnnotations = new ArrayList<Annotation>();
    classroomAnnotations.add(workgroup1Score);
    classroomAnnotations.add(workgroup2Score);
    classroomAnnotations.add(workgroup3Score);
    classroomAnnotations.add(workgroup4Score);
    classroomAnnotations.add(workgroup5Score);
  }

  private Annotation createKIScoreAnnotation(Workgroup workgroup, int score) {
    Annotation annotation = new Annotation();
    annotation.setToWorkgroup(workgroup);
    annotation.setPeriod(period1);
    annotation.setType("autoScore");
    annotation.setData("{\"scores\": [{\"id\":\"ki\",\"score\":" + score + "}]}");
    return annotation;
  }

  @Test
  public void getPossibleMembersInOrder_RunHasNoAutoScoreAnnotation_EmptySet() {
    setLogic("any");
    expectAnnotations(emptyAnnotations);
    replay(annotationDao);
    assertEquals(0,
        service.getPossibleMembersInOrder(possibleMembers, workgroup1, peerGrouping).size());
    verify(annotationDao);
  }

  @Test
  public void getPossibleMembersInOrder_AnyMode_RandomOrder() {
    setLogic("any");
    expectAnnotations(classroomAnnotations);
    replay(annotationDao);
    TreeSet<WorkgroupLogicComparable> possibleMembersInOrder = service
        .getPossibleMembersInOrder(possibleMembers, workgroup1, peerGrouping);
    assertEquals(4, possibleMembersInOrder.size());
    Iterator<WorkgroupLogicComparable> iterator = possibleMembersInOrder.iterator();
    assertEquals(workgroup3, iterator.next().getWorkgroup()); // has same KI score; should be at the beginning (least-value)
    verify(annotationDao);
  }

  @Test
  public void getPossibleMembersInOrder_MaximizeMode_MaximizeOrder() {
    setLogic("maximize");
    expectAnnotations(classroomAnnotations);
    replay(annotationDao);
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
  }

  private void setLogic(String logic) {
    peerGrouping.setLogic(
        "differentKIScores(\"" + nodeId + "\", \"" + componentId + "\", \"" + logic + "\")");
  }
}
