package org.wise.portal.presentation.web.controllers.author.project;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.anyObject;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpSession;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.portal.impl.PortalImpl;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.presentation.web.response.SimpleResponse;
import org.wise.portal.service.peergrouping.PeerGroupingService;
import org.wise.portal.service.session.SessionService;
import org.wise.portal.spring.data.redis.MessagePublisher;

@RunWith(EasyMockRunner.class)
public class AuthorAPIControllerTest extends APIControllerTest {

  @TestSubject
  private AuthorAPIController authorAPIController = new AuthorAPIController(new MockEnvironment());

  @Mock
  private SessionService sessionService;

  @Mock
  private PeerGroupingService peerGroupingService;

  @Mock
  private MessagePublisher redisPublisher;

  private String projectJSONString = "";

  @Test
  public void getAuthorProjectConfig_HasProjectRun_ReturnCanGradeStudentWork() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1).times(2);
    replay(userService);
    expect(projectService.getProjectList(teacher1)).andReturn(new ArrayList<Project>());
    expect(projectService.getSharedProjectList(teacher1)).andReturn(new ArrayList<Project>());
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    replay(projectService);
    expect(runService.getRunListByOwner(teacher1)).andReturn(new ArrayList<Run>());
    expect(runService.getRunListBySharedOwner(teacher1)).andReturn(new ArrayList<Run>());
    ArrayList<Run> projectRunList = new ArrayList<Run>();
    projectRunList.add(run1);
    expect(runService.getProjectRuns(projectId1)).andReturn(projectRunList);
    expect(runService.isAllowedToGradeStudentWork(run1, teacher1)).andReturn(true);
    replay(runService);
    expect(portalService.getById(new Integer(1))).andReturn(new PortalImpl());
    expect(portalService.getDefaultProjectMetadataSettings()).andReturn("");
    replay(portalService);
    expect(request.getLocale()).andReturn(Locale.US);
    expect(request.getContextPath()).andReturn("wise").times(3);
    expect(request.getSession()).andReturn(new MockHttpSession());
    expect(request.getHeader("Host")).andReturn("");
    expect(request.getScheme()).andReturn("http").times(2);
    expect(request.getServerName()).andReturn("");
    expect(request.getServerPort()).andReturn(8080);
    replay(request);
    Map<String, Object> config = authorAPIController.getAuthorProjectConfig(teacherAuth, request,
        project1);
    assertTrue((boolean) config.get("canEditProject"));
    assertTrue((boolean) config.get("canGradeStudentWork"));
    assertEquals(runId1, config.get("runId"));
    verify(userService, projectService);
  }

  @Test
  public void authorProjectBegin_CanNotAuthor_NotifyAuthors() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(false);
    replay(projectService);
    authorAPIController.notifyAuthorBeginEnd(teacherAuth, project1, true);
    verify(userService, projectService);
  }

  @Test
  public void authorProjectBegin_CanAuthor_NotifyAuthors() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    replay(projectService);
    sessionService.addCurrentAuthor(projectId1, TEACHER_USERNAME);
    expectLastCall();
    expect(sessionService.getCurrentAuthors(projectId1)).andReturn(new HashSet<String>());
    expectLastCall();
    replay(sessionService);
    redisPublisher.publish(isA(String.class));
    expectLastCall();
    replay(redisPublisher);
    authorAPIController.notifyAuthorBeginEnd(teacherAuth, project1, true);
    verify(userService, projectService, sessionService, redisPublisher);
  }

  @Test
  public void authorProjectEnd_CanNotAuthor_NotifyAuthors() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(false);
    replay(projectService);
    authorAPIController.notifyAuthorBeginEnd(teacherAuth, project1, false);
    verify(userService, projectService);
  }

  @Test
  public void authorProjectEnd_CanAuthor_NotifyAuthors() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    replay(projectService);
    sessionService.removeCurrentAuthor(projectId1, TEACHER_USERNAME);
    expectLastCall();
    expect(sessionService.getCurrentAuthors(projectId1)).andReturn(new HashSet<String>());
    expectLastCall();
    replay(sessionService);
    redisPublisher.publish(isA(String.class));
    expectLastCall();
    replay(redisPublisher);
    authorAPIController.notifyAuthorBeginEnd(teacherAuth, project1, false);
    verify(userService, projectService, sessionService, redisPublisher);
  }

  @Test
  public void saveProject_CanNotAuthor_Return() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(false);
    replay(projectService);
    authorAPIController.saveProject(teacherAuth, project1, projectJSONString);
    verify(userService, projectService);
  }

  @Test
  public void copyProject_CanReadProject_ReturnCopiedProject() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.getById(projectId1)).andReturn(project1);
    expect(projectService.canReadProject(project1, teacher1)).andReturn(true);
    expect(projectService.copyProject(projectId1, teacher1)).andReturn(new ProjectImpl());
    replay(projectService);
    authorAPIController.copyProject(teacherAuth, projectId1);
    verify(userService);
    verify(projectService);
  }

  @Test
  public void copyProject_CanNotReadProject_ReturnNull() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.getById(projectId1)).andReturn(project1);
    expect(projectService.canReadProject(project1, teacher1)).andReturn(false);
    replay(projectService);
    Project copiedProject = authorAPIController.copyProject(teacherAuth, projectId1);
    assertNull(copiedProject);
    verify(userService);
    verify(projectService);
  }

  @Test
  public void saveProject_whenNotAllowedToEdit_shouldReturnNotAllowedToEditError()
      throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    ProjectImpl project = new ProjectImpl();
    expect(projectService.canAuthorProject(project, teacher1)).andReturn(false);
    replay(projectService);
    String projectJSONString = "{}";
    SimpleResponse response = authorAPIController.saveProject(teacherAuth, project,
        projectJSONString);
    assertEquals("error", response.getStatus());
    assertEquals("notAllowedToEditThisProject", response.getMessageCode());
    verify(userService, projectService);
  }

  @Test
  public void saveProject_whenErrorSavingProjectFile_shouldReturnErrorSavingProject()
      throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    projectService.evictProjectContentCache(projectId1);
    expectLastCall();
    String projectJSONString = "{}";
    projectService.saveProjectContentToDisk(projectJSONString, project1);
    expectLastCall().andThrow(new IOException());
    replay(projectService);
    SimpleResponse response = authorAPIController.saveProject(teacherAuth, project1,
        projectJSONString);
    assertEquals("error", response.getStatus());
    assertEquals("errorSavingProject", response.getMessageCode());
    verify(userService, projectService);
  }

  @Test
  public void saveProject_whenErrorSavingProjectToDatabase_shouldReturnErrorSavingProject()
      throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    replay(userService);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    projectService.evictProjectContentCache(projectId1);
    expectLastCall();
    String projectJSONString = "{\"metadata\":{\"title\":\"New Title\"}}";
    projectService.saveProjectContentToDisk(projectJSONString, project1);
    expectLastCall();
    projectService.updateMetadataAndLicenseIfNecessary(anyObject(), anyObject());
    expectLastCall();
    projectService.saveProjectToDatabase(project1, teacher1, projectJSONString);
    expectLastCall().andThrow(new JSONException(""));
    replay(projectService);
    SimpleResponse response = authorAPIController.saveProject(teacherAuth, project1,
        projectJSONString);
    assertEquals("error", response.getStatus());
    assertEquals("errorSavingProject", response.getMessageCode());
    verify(userService, projectService);
  }

  @Test
  public void saveProject_NoAssociatedRun_shouldReturnProjectSaved() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    project1.setMetadata("{\"title\":\"Old Title\"}");
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    projectService.evictProjectContentCache(projectId1);
    expectLastCall();
    String projectJSONString = "{\"metadata\":{\"title\":\"New Title\"}}";
    projectService.saveProjectContentToDisk(projectJSONString, project1);
    expectLastCall();
    projectService.updateMetadataAndLicenseIfNecessary(anyObject(), anyObject());
    expectLastCall();
    projectService.saveProjectToDatabase(project1, teacher1, projectJSONString);
    expectLastCall();
    replay(userService, projectService);
    SimpleResponse response = authorAPIController.saveProject(teacherAuth, project1,
        projectJSONString);
    assertEquals("success", response.getStatus());
    assertEquals("projectSaved", response.getMessageCode());
    verify(userService, projectService);
  }

  @Test
  public void saveProject_ProjectHasAssociatedRun_shouldScanForPeerGroupingsAndReturnProjectSaved()
      throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    project1.setMetadata("{\"title\":\"Old Title\"}");
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    projectService.evictProjectContentCache(projectId1);
    expectLastCall();
    String projectJSONString = "{\"metadata\":{\"title\":\"New Title\"}}";
    projectService.saveProjectContentToDisk(projectJSONString, project1);
    expectLastCall();
    projectService.updateMetadataAndLicenseIfNecessary(anyObject(), anyObject());
    expectLastCall();
    projectService.saveProjectToDatabase(project1, teacher1, projectJSONString);
    expectLastCall();
    replay(userService, projectService);
    SimpleResponse response = authorAPIController.saveProject(teacherAuth, project1,
        projectJSONString);
    assertEquals("success", response.getStatus());
    assertEquals("projectSaved", response.getMessageCode());
    verify(userService, projectService);
  }

  @Test
  public void getAssetFileNames_withDuplicateReferences_shouldReturnUniqueFileNames()
      throws Exception {
    String stepsText = "<img src=\"carbon.png\"/><img src=\"carbon.png\"/>";
    List<String> fileNames = authorAPIController.getAssetFileNames(stepsText);
    assertEquals(fileNames.size(), 1);
    assertEquals(fileNames.get(0), "carbon.png");
  }

  @Test
  public void getAssetFileNames_withASpace_shouldReturnFileNames() throws Exception {
    String stepsText = "<img src=\"carbon%20dioxide.png\"/><img src=\"carbon monoxide.png\"/>";
    List<String> fileNames = authorAPIController.getAssetFileNames(stepsText);
    assertEquals(fileNames.size(), 2);
    assertTrue(fileNames.contains("carbon dioxide.png"));
    assertTrue(fileNames.contains("carbon monoxide.png"));
  }
}
