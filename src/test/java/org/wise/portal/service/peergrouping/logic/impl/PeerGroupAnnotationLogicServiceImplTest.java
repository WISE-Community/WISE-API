package org.wise.portal.service.peergrouping.logic.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.Mock;
import org.junit.Before;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.annotation.wise5.Annotation;

public class PeerGroupAnnotationLogicServiceImplTest extends PeerGroupLogicServiceImplTest {

  @Mock
  AnnotationDao<Annotation> annotationDao;
  String componentId = "componentA";
  List<Annotation> emptyAnnotations = new ArrayList<Annotation>();
  String nodeId = "node1";
  PeerGrouping peerGrouping = new PeerGroupingImpl();
  Run run = new RunImpl();
  Set<Workgroup> workgroupsNotInPeerGroup;

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
}
