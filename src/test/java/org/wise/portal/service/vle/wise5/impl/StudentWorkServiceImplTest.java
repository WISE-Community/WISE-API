package org.wise.portal.service.vle.wise5.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.WISEServiceTest;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class StudentWorkServiceImplTest extends WISEServiceTest {
  
  @TestSubject
  private StudentWorkServiceImpl service = new StudentWorkServiceImpl();

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;
  
  @Test
  public void getLatestStudentWork_SpecificNodeIdComponentId_ReturnLatestStudentWorkList() {
    StudentWork studentWork1 = createComponentWork(run1Workgroup1, run1Node1Id, run1Component1Id,
        true);
    StudentWork studentWork2 = createComponentWork(run1Workgroup2, run1Node1Id, run1Component1Id,
        true);
    StudentWork studentWork3 = createComponentWork(run1Workgroup1, run1Node1Id, run1Component1Id,
        true);
    HashSet<Workgroup> workgroups = new HashSet<Workgroup>();
    workgroups.add(run1Workgroup1);
    workgroups.add(run1Workgroup2);
    expectGetStudentWork(workgroups, run1Node1Id, run1Component1Id,
        createStudentWorkList(studentWork1, studentWork2, studentWork3));
    replayAll();
    assertEquals(2, service.getLatestStudentWork(workgroups, run1Node1Id, run1Component1Id).size());
    verifyAll();
  }

  private void expectGetStudentWork(Set<Workgroup> workgroups, String nodeId, String componentId,
      List<StudentWork> studentWorkList) {
    expect(studentWorkDao.getStudentWork(workgroups, nodeId, componentId))
        .andReturn(studentWorkList);
  }

  private void replayAll() {
    replay(studentWorkDao);
  }

  private void verifyAll() {
    verify(studentWorkDao);
  }
}
