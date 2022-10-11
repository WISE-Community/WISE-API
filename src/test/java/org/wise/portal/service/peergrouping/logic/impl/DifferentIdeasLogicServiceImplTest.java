package org.wise.portal.service.peergrouping.logic.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.annotation.wise5.Annotation;

@RunWith(EasyMockRunner.class)
public class DifferentIdeasLogicServiceImplTest extends PeerGroupLogicServiceImplTest {

  @TestSubject
  private PeerGroupLogicServiceImpl service = new DifferentIdeasLogicServiceImpl();

  @Mock
  private AnnotationDao<Annotation> annotationDao;

  List<Annotation> workgroup1IdeasOnly, classroomIdeaAnnotations;
  private String componentId = "componentA";
  List<Annotation> emptyAnnotations = new ArrayList<Annotation>();
  private String nodeId = "node1";
  PeerGrouping peerGrouping = new PeerGroupingImpl();
  private Run run = new RunImpl();
  Set<Workgroup> workgroupsNotInPeerGroup;
  Annotation workgroup1Ideas, workgroup2Ideas, workgroup3Ideas, workgroup4Ideas;
  String ideas1And2 = createIdeaString(true, true, false, false);
  String ideas3 = createIdeaString(false, false, true, false);
  String ideas3And4 = createIdeaString(false, false, true, true);
  String ideas4 = createIdeaString(false, false, false, true);

  @Before
  public void setup() throws Exception {
    super.setup();
    peerGrouping.setRun(run);
    peerGrouping.setLogic("differentIdeas(\"" + nodeId + "\", \"" + componentId + "\")");
    peerGrouping.setMaxMembershipCount(2);
    workgroupsNotInPeerGroup = new HashSet<Workgroup>();
    workgroupsNotInPeerGroup.add(workgroup1);
    workgroupsNotInPeerGroup.add(workgroup2);
    workgroupsNotInPeerGroup.add(workgroup3);
    workgroupsNotInPeerGroup.add(workgroup4);
    workgroup1Ideas = createIdeasAnnotation(workgroup1, ideas1And2);
    workgroup2Ideas = createIdeasAnnotation(workgroup2, ideas3);
    workgroup3Ideas = createIdeasAnnotation(workgroup3, ideas3And4);
    workgroup4Ideas = createIdeasAnnotation(workgroup4, ideas4);
    workgroup1IdeasOnly = new ArrayList<Annotation>();
    workgroup1IdeasOnly.add(workgroup1Ideas);
    classroomIdeaAnnotations = new ArrayList<Annotation>();
    classroomIdeaAnnotations.add(workgroup1Ideas);
    classroomIdeaAnnotations.add(workgroup2Ideas);
    classroomIdeaAnnotations.add(workgroup3Ideas);
    classroomIdeaAnnotations.add(workgroup4Ideas);
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
  public void groupMembersUpToMaxMembership_MaximizeDifferentIdeas() {
    expect(annotationDao.getAnnotationsByParams(null, run, run1Period1, null, null, nodeId,
        componentId, null, null, null, "autoScore")).andReturn(classroomIdeaAnnotations);
    replay(annotationDao);
    int maxMembers = peerGrouping.getMaxMembershipCount();
    Set<Workgroup> peerGroupMembers = service.groupMembersUpToMaxMembership(workgroup1,
        peerGrouping, possibleMembers);
    assertEquals(maxMembers, peerGroupMembers.size());
    Iterator<Workgroup> iterator = peerGroupMembers.iterator();
    assertEquals(1, iterator.next().getId().longValue());
    assertEquals(3, iterator.next().getId().longValue());
    verify(annotationDao);
  }
}
