package org.wise.vle.web.wise5.student;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.vle.domain.work.StudentWork;

@SpringBootTest
@RunWith(EasyMockRunner.class)
public class StudentGetDataControllerTest extends APIControllerTest {

  @TestSubject
  private StudentGetDataController controller = new StudentGetDataController();

  private boolean getStudentWork;
  private boolean getEvents;
  private boolean getAnnotations;
  private Integer id;
  private Integer runId;
  private Integer periodId;
  private Integer workgroupId;
  private Boolean isAutoSave;
  private Boolean isSubmit;
  private String nodeId;
  private String componentId;
  private String componentType;
  private String context;
  private String category;
  private String event;
  private Integer fromWorkgroupId;
  private Integer toWorkgroupId;
  private Integer studentWorkId;
  private String localNotebookItemId;
  private Integer notebookItemId;
  private String annotationType;
  private List<JSONObject> components;
  private Boolean onlyGetLatest;

  @Before
  public void init() {
    getStudentWork = false;
    getEvents = false;
    getAnnotations = false;
    id = null;
    runId = null;
    periodId = null;
    workgroupId = null;
    isAutoSave = false;
    isSubmit = false;
    nodeId = null;
    componentId = null;
    componentType = null;
    context = null;
    category = null;
    event = null;
    fromWorkgroupId = null;
    toWorkgroupId = null;
    studentWorkId = null;
    localNotebookItemId = null;
    notebookItemId = null;
    annotationType = null;
    components = null;
    onlyGetLatest = false;
  }

  @Test
  public void getWISE5StudentData_NotAllowedToGetData_DoesNotRetrieveStudentWork() {
    try {
      getStudentWork = true;
      runId = Integer.valueOf(runId1.intValue());
      workgroupId = Integer.valueOf(workgroup2Id.intValue());
      expectRetrieveUser(student1UserDetails, student1);
      expectRetrieveRun(runId1, run1);
      expectRetrieveWorkgroup(workgroup2Id, workgroup2);
      expectIsUserInWorkgroupForRun(student1, run1, workgroup2, false);
      replayAll();
      controller.getStudentData(studentAuth, getStudentWork, getEvents, getAnnotations, id, runId,
          periodId, workgroupId, isAutoSave, isSubmit, nodeId, componentId, componentType, context,
          category, event, fromWorkgroupId, toWorkgroupId, studentWorkId, localNotebookItemId,
          notebookItemId, annotationType, components, onlyGetLatest);
    } catch (ObjectNotFoundException e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  @Test
  public void getWISE5StudentData_AllowedToGetData_RetrievesStudentWork() {
    try {
      getStudentWork = true;
      runId = Integer.valueOf(runId1.intValue());
      workgroupId = Integer.valueOf(workgroup1Id.intValue());
      List<StudentWork> studentWorkList = Arrays.asList(new StudentWork(), new StudentWork());
      expectRetrieveUser(student1UserDetails, student1);
      expectRetrieveRun(runId1, run1);
      expectRetrieveWorkgroup(workgroup1Id, workgroup1);
      expectIsUserInWorkgroupForRun(student1, run1, workgroup1, true);
      expectGetStudentWorkList(id, runId, periodId, workgroupId, isAutoSave, isSubmit, nodeId,
          componentId, componentType, components, onlyGetLatest, studentWorkList);
      replayAll();
      controller.getStudentData(studentAuth, getStudentWork, getEvents, getAnnotations, id, runId,
          periodId, workgroupId, isAutoSave, isSubmit, nodeId, componentId, componentType, context,
          category, event, fromWorkgroupId, toWorkgroupId, studentWorkId, localNotebookItemId,
          notebookItemId, annotationType, components, onlyGetLatest);
    } catch (ObjectNotFoundException e) {
      fail(SHOULD_NOT_HAVE_THROWN_EXCEPTION);
    }
    verifyAll();
  }

  private void expectRetrieveUser(StudentUserDetails studentUserDetails, User user) {
    expect(userService.retrieveUser(studentUserDetails)).andReturn(user);
  }

  private void expectRetrieveRun(Long runId, Run run) throws ObjectNotFoundException {
    expect(runService.retrieveById(runId)).andReturn(run);
  }

  private void expectRetrieveWorkgroup(Long workgroupId, Workgroup workgroup)
      throws ObjectNotFoundException {
    expect(workgroupService.retrieveById(workgroupId)).andReturn(workgroup);
  }

  private void expectIsUserInWorkgroupForRun(User user, Run run, Workgroup workgroup,
      boolean result) {
    expect(workgroupService.isUserInWorkgroupForRun(user, run, workgroup)).andReturn(result);
  }

  private void expectGetStudentWorkList(Integer id, Integer runId, Integer periodId,
      Integer workgroupId, Boolean isAutoSave, Boolean isSubmit, String nodeId, String componentId,
      String componentType, List<JSONObject> components, Boolean onlyGetLatest,
      List<StudentWork> result) {
    expect(vleService.getStudentWorkList(id, runId, periodId, workgroupId, isAutoSave, isSubmit,
        nodeId, componentId, componentType, components, onlyGetLatest)).andReturn(result);
  }

  private void replayAll() {
    replay(runService, userService, vleService, workgroupService);
  }

  private void verifyAll() {
    verify(runService, userService, vleService, workgroupService);
  }
}
