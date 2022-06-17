package org.wise.portal.service.vle.wise5.impl;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.service.WISEServiceTest;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class TeacherWorkServiceImplTest extends WISEServiceTest {

  @TestSubject
  private TeacherWorkServiceImpl service = new TeacherWorkServiceImpl();

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Test
  public void save_SetServerTimeAndReturn() {
    StudentWork studentWork = createComponentWork(run1Workgroup1, run1Node1Id, run1Component1Id,
        true);
    assertNull(studentWork.getServerSaveTime());
    studentWorkDao.save(studentWork);
    expectLastCall();
    replay(studentWorkDao);
    service.save(studentWork);
    assertNotNull(studentWork.getServerSaveTime());
    verify(studentWorkDao);
  }
}
