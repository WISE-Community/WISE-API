package org.wise.portal.service.vle.wise5.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.vle.domain.work.StudentWork;

/**
 * @author Hiroki Terashima
 */
@ExtendWith(EasyMockExtension.class)
public class VLEServiceImplTest {

  @TestSubject
  private VLEService vleService = new VLEServiceImpl();

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  private List<StudentWork> allStudentWork;

  private StudentWork studentWork1, studentWork2, studentWork3;

  private Workgroup workgroup1, workgroup2;

  @BeforeEach
  public void setup() {
    workgroup1 = createWorkgroup(1L);
    workgroup2 = createWorkgroup(2L);
    studentWork1 = createStudentWork(workgroup1, new Timestamp(1));
    studentWork2 = createStudentWork(workgroup2, new Timestamp(2));
    studentWork3 = createStudentWork(workgroup1, new Timestamp(3));
    allStudentWork = new ArrayList<StudentWork>(
        Arrays.asList(studentWork1, studentWork2, studentWork3));
  }

  private StudentWork createStudentWork(Workgroup workgroup, Timestamp serverSaveTime) {
    StudentWork studentWork = new StudentWork();
    studentWork.setWorkgroup(workgroup);
    studentWork.setServerSaveTime(serverSaveTime);
    return studentWork;
  }

  private Workgroup createWorkgroup(long id) {
    Workgroup workgroup = new WorkgroupImpl();
    workgroup.setId(id);
    return workgroup;
  }

  @Test
  public void getStudentWorkList_LatestOnlyTrue_ReturnFilteredList() {
    expect(studentWorkDao.getStudentWorkListByParams(null, null, null, null, null, null, null, null,
        null, null)).andReturn(allStudentWork);
    replay(studentWorkDao);
    List<StudentWork> studentWorkList = vleService.getStudentWorkList(null, null, null, null, null,
        null, null, null, null, null, true);
    assertEquals(2, studentWorkList.size());
    assertEquals(studentWork3, studentWorkList.get(0));
    assertEquals(studentWork2, studentWorkList.get(1));
  }

  @Test
  public void getStudentWorkList_LatestOnlyFalse_ReturnAllList() {
    expect(studentWorkDao.getStudentWorkListByParams(null, null, null, null, null, null, null, null,
        null, null)).andReturn(allStudentWork);
    replay(studentWorkDao);
    List<StudentWork> studentWorkList = vleService.getStudentWorkList(null, null, null, null, null,
        null, null, null, null, null, false);
    assertEquals(3, studentWorkList.size());
  }
}
