package org.wise.portal.presentation.web.controllers.teacher.management;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

@RunWith(EasyMockRunner.class)
public class CreateWorkgroupControllerTest extends APIControllerTest {

  @TestSubject
  private CreateWorkgroupController controller = new CreateWorkgroupController();

  @Test
  public void createWorkgroup_NoWritePermission_ThrowAccessDenied() throws ObjectNotFoundException {
    expect(runService.retrieveById(runId3)).andReturn(run3);
    expect(runService.hasWritePermission(teacherAuth, run3)).andReturn(false);
    replay(runService);
    try {
      List<Long> userIds = Stream.of(2L).collect(Collectors.toList());
      controller.createWorkgroup(teacherAuth, runId3, run1Period1.getId(), userIds);
      fail("ResponseStatusException expected but was not thrown");
    } catch (ResponseStatusException e) {
    }
    verify(runService);
  }

  @Test
  public void createWorkgroup_UserInWorkgroup_CallRemoveMembers() throws ObjectNotFoundException {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(groupService.retrieveById(run1Period1Id)).andReturn(run1Period1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveById(student1.getId())).andReturn(student1);
    expect(userService.retrieveById(student1.getId())).andReturn(student1);
    workgroupService.removeUserFromAllWorkgroupsInRun(run1, student1);
    expectLastCall();
    expect(userService.retrieveById(student1.getId())).andReturn(student1);
    Workgroup newWorkgroup = new WorkgroupImpl();
    newWorkgroup.setId(2L);
    expect(workgroupService.createWorkgroup(isA(String.class), isA(HashSet.class), isA(Run.class),
        isA(Group.class))).andReturn(newWorkgroup);
    replay(runService, userService, workgroupService, groupService);
    List<Long> userIds = Stream.of(student1.getId()).collect(Collectors.toList());
    controller.createWorkgroup(teacherAuth, runId1, run1Period1.getId(), userIds);
    verify(runService, userService, workgroupService, groupService);
  }
}
