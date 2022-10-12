package org.wise.portal.service.peergrouping.logic.impl;

import java.util.HashSet;

import org.junit.Before;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.peergroup.impl.PeerGroupServiceTest;

public class PeerGroupLogicServiceImplTest extends PeerGroupServiceTest {

  HashSet<Workgroup> members = new HashSet<Workgroup>();
  Group period1 = new PersistentGroup();
  HashSet<Workgroup> possibleMembers = new HashSet<Workgroup>();
  Workgroup workgroup1, workgroup2, workgroup3, workgroup4, workgroup5;

  @Before
  public void setup() throws Exception {
    super.setUp();
    workgroup1 = createWorkgroup(1L);
    workgroup2 = createWorkgroup(2L);
    workgroup3 = createWorkgroup(3L);
    workgroup4 = createWorkgroup(4L);
    workgroup5 = createWorkgroup(5L);
    members.add(workgroup1);
    possibleMembers.add(workgroup2);
    possibleMembers.add(workgroup3);
    possibleMembers.add(workgroup4);
    possibleMembers.add(workgroup5);
  }

  private Workgroup createWorkgroup(Long id) {
    Workgroup workgroup = new WorkgroupImpl();
    workgroup.setId(id);
    PersistentGroup group = new PersistentGroup();
    group.setName(id.toString());
    workgroup.setGroup(group);
    workgroup.setPeriod(period1);
    return workgroup;
  }
}
