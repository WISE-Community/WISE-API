package org.wise.portal.presentation.web.controllers.student;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.vle.domain.work.StudentWork;

@RunWith(EasyMockRunner.class)
public class ClassmatePeerChatDataControllerTest extends AbstractClassmateDataControllerTest {

  protected final Long PEER_GROUP_ID1 = 1L;
  protected final Long PEER_GROUP_ID2 = 2L;
  protected final String STUDENT3_FIRSTNAME = "Pearl";
  protected final Long STUDENT3_ID = 94680L;
  protected final String STUDENT3_LASTNAME = "Krabs";
  protected final String STUDENT3_USERNAME = "PearlK0101";
  protected PeerGroup peerGroup1;
  protected PeerGroup peerGroup2;
  protected PeerGroupActivity peerGroupActivity;
  protected StudentUserDetails student3UserDetails;
  protected Group period1;
  protected User student3;
  protected Workgroup workgroup3;

  @Mock
  PeerGroupService peerGroupService;

  @TestSubject
  private ClassmatePeerChatDataController controller = new ClassmatePeerChatDataController();

  @Before
  public void before() throws JSONException {
    createPeerGroupActivity();
    setUpPeerGroup1(peerGroupActivity);
  }

  private void createPeerGroupActivity() throws JSONException {
    ProjectComponent component = new ProjectComponent(
        createComponentJSONObject(COMPONENT_ID1, "PeerChat", "", 2, 50, 2));
    peerGroupActivity = new PeerGroupActivityImpl(run1, NODE_ID1, component);
  }

  private void setUpPeerGroup1(PeerGroupActivity peerGroupActivity) {
    Set<Workgroup> peerGroup1Members = new HashSet<Workgroup>(
        Arrays.asList(workgroup1, workgroup2));
    peerGroup1 = new PeerGroupImpl(peerGroupActivity, period1, peerGroup1Members);
  }

  private void createWorkgroup3() {
    student3UserDetails = createStudentUserDetails(STUDENT3_FIRSTNAME, STUDENT3_LASTNAME,
        STUDENT3_USERNAME, Gender.FEMALE, 15, null);
    student3 = createStudent(STUDENT3_ID, student3UserDetails);
    workgroup3 = new WorkgroupImpl();
    workgroup3.addMember(student3);
    workgroup3.setPeriod(run1Period1);
    workgroup3.setRun(run1);
  }

  private void setUpPeerGroup2(PeerGroupActivity peerGroupActivity) {
    Set<Workgroup> peerGroup2Members = new HashSet<Workgroup>(Arrays.asList(workgroup3));
    peerGroup2 = new PeerGroupImpl(peerGroupActivity, period1, peerGroup2Members);
  }

  private JSONObject createComponentJSONObject(String componentId, String componentType,
      String logic, Integer logicThresholdCount, Integer logicThresholdPercent,
      Integer maxMembershipCount) throws JSONException {
    String projectJSONString = new StringBuilder()
        .append("{")
        .append("  \"id\": \"" + componentId + "\",")
        .append("  \"type\": \"" + componentType + "\",")
        .append("  \"logic\": \"" + logic + "\",")
        .append("  \"logicThresholdCount\": " + logicThresholdCount + ",")
        .append("  \"logicThresholdPercent\": " + logicThresholdPercent + ",")
        .append("  \"maxMembershipCount\": " + maxMembershipCount)
        .append("}")
        .toString();
    return new JSONObject(projectJSONString);
  }

  @Test
  public void getClassmatePeerChatWork_NotInPeerGroup_ShouldThrowException()
      throws IOException, ObjectNotFoundException {
    createWorkgroup3();
    setUpPeerGroup2(peerGroupActivity);
    expectGetPeerGroup(PEER_GROUP_ID2, peerGroup2);
    expectRetrieveUser();
    replayAll();
    assertThrows(AccessDeniedException.class, () -> controller.getClassmatePeerChatWork(studentAuth,
        PEER_GROUP_ID2, NODE_ID1, COMPONENT_ID1, OTHER_NODE_ID, OTHER_COMPONENT_ID));
    verifyAll();
  }

  @Test
  public void getClassmatePeerChatWork_InvalidPeerChatComponent_ShouldThrowException()
      throws IOException, ObjectNotFoundException {
    expectGetPeerGroup(PEER_GROUP_ID1, peerGroup1);
    expectRetrieveUser();
    expectComponentType(NODE_ID1, COMPONENT_ID1, OPEN_RESPONSE_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID);
    replayAll();
    assertThrows(AccessDeniedException.class, () -> controller.getClassmatePeerChatWork(studentAuth,
        PEER_GROUP_ID1, NODE_ID1, COMPONENT_ID1, OTHER_NODE_ID, OTHER_COMPONENT_ID));
    verifyAll();
  }

  @Test
  public void getClassmatePeerChatWork_InvalidOtherComponent_ShouldThrowException()
      throws IOException, ObjectNotFoundException {
    expectGetPeerGroup(PEER_GROUP_ID1, peerGroup1);
    expectRetrieveUser();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.PEER_CHAT_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID);
    replayAll();
    assertThrows(AccessDeniedException.class,
        () -> controller.getClassmatePeerChatWork(studentAuth, PEER_GROUP_ID1, NODE_ID1,
            COMPONENT_ID1, OTHER_NODE_ID_NOT_ALLOWED, OTHER_COMPONENT_ID_NOT_ALLOWED));
    verifyAll();
  }

  @Test
  public void getClassmatePeerChatWork_ValidOtherComponent_ShouldReturnStudentWork()
      throws IOException, JSONException, ObjectNotFoundException {
    expectGetPeerGroup(PEER_GROUP_ID1, peerGroup1);
    expectRetrieveUser();
    expectComponentType(NODE_ID1, COMPONENT_ID1, controller.PEER_CHAT_TYPE, OTHER_NODE_ID,
        OTHER_COMPONENT_ID);
    List<StudentWork> expectedStudentWork = Arrays.asList(new StudentWork(), new StudentWork());
    expectGetLatestStudentWork(peerGroup1, OTHER_NODE_ID, OTHER_COMPONENT_ID, expectedStudentWork);
    replayAll();
    List<StudentWork> actualStudentWork = controller.getClassmatePeerChatWork(studentAuth,
        PEER_GROUP_ID1, NODE_ID1, COMPONENT_ID1, OTHER_NODE_ID, OTHER_COMPONENT_ID);
    assertEquals(expectedStudentWork, actualStudentWork);
    verifyAll();
  }

  private void expectGetPeerGroup(Long peerGroupId, PeerGroup peerGroup)
      throws ObjectNotFoundException {
    expect(peerGroupService.getById(peerGroupId)).andReturn(peerGroup);
  }

  private void expectRetrieveUser() {
    expect(userService.retrieveUser(student1UserDetails)).andReturn(student1);
  }

  protected void expectComponentType(String nodeId, String componentId, String componentType,
      String otherNodeId, String otherComponentId) throws IOException, ObjectNotFoundException {
    String projectJSONString = new StringBuilder()
        .append("{")
        .append("  \"nodes\": [")
        .append("    {")
        .append("      \"id\": \"" + nodeId + "\",")
        .append("      \"components\": [")
        .append("        {")
        .append("          \"id\": \"" + componentId + "\",")
        .append("          \"type\": \"" + componentType + "\",")
        .append("          \"showWorkNodeId\": \"" + otherNodeId + "\",")
        .append("          \"showWorkComponentId\": \"" + otherComponentId + "\"")
        .append("        }")
        .append("      ]")
        .append("    }")
        .append("  ]")
        .append("}")
        .toString();
    expect(projectService.getProjectContent(project1)).andReturn(projectJSONString);
  }

  private void expectGetLatestStudentWork(PeerGroup peerGroup, String nodeId, String componentId,
      List<StudentWork> studentWork) {
    expect(peerGroupService.getLatestStudentWork(peerGroup, nodeId, componentId))
        .andReturn(studentWork);
  }

  protected void replayAll() {
    replay(peerGroupService, projectService, userService);
  }

  protected void verifyAll() {
    verify(peerGroupService, projectService, userService);
  }
}
