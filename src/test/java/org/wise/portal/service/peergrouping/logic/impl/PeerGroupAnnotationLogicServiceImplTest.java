package org.wise.portal.service.peergrouping.logic.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.Mock;
import org.junit.Before;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.peergroup.impl.WorkgroupLogicComparable;
import org.wise.vle.domain.annotation.wise5.Annotation;

public abstract class PeerGroupAnnotationLogicServiceImplTest
    extends PeerGroupLogicServiceImplTest {

  @Mock
  AnnotationDao<Annotation> annotationDao;
  String componentId = "componentA";
  List<Annotation> emptyAnnotations = new ArrayList<Annotation>();
  String nodeId = "node1";
  PeerGrouping peerGrouping = new PeerGroupingImpl();
  Run run = new RunImpl();
  Set<Workgroup> workgroupsNotInPeerGroup;
  Map<Workgroup, Annotation> workgroupToAnnotation = new HashMap<Workgroup, Annotation>();

  @Before
  public void setup() throws Exception {
    super.setup();
    workgroupsNotInPeerGroup = new HashSet<Workgroup>();
    workgroupsNotInPeerGroup.add(workgroup1);
    workgroupsNotInPeerGroup.add(workgroup2);
    workgroupsNotInPeerGroup.add(workgroup3);
    workgroupsNotInPeerGroup.add(workgroup4);
    workgroupsNotInPeerGroup.add(workgroup5);
    peerGrouping.setRun(run);
    peerGrouping.setMaxMembershipCount(2);
  }

  void setLogic(String mode) {
    peerGrouping.setLogic(
        getLogicFunctionName() + "(\"" + nodeId + "\", \"" + componentId + "\", \"" + mode + "\")");
  }

  void expectAnnotations(List<Annotation> annotations) {
    expect(annotationDao.getAnnotationsByParams(null, run, run1Period1, null, null, nodeId,
        componentId, null, null, null, "autoScore")).andReturn(annotations);
    replay(annotationDao);
  }

  @SafeVarargs
  final void assertOneMatch(List<Workgroup> workgroupsMatchTo,
      List<Workgroup>... workgroupsMatchFrom) {
    boolean match = false;
    for (List<Workgroup> workgroups : workgroupsMatchFrom) {
      match |= workgroups.equals(workgroupsMatchTo);
    }
    assertTrue(match);
  }

  List<Workgroup> getWorkgroups(TreeSet<WorkgroupLogicComparable> workgroupLogicComparables) {
    return workgroupLogicComparables.stream().map(workgroupLogic -> workgroupLogic.getWorkgroup())
        .collect(Collectors.toList());
  }

  abstract String getLogicFunctionName();
}
