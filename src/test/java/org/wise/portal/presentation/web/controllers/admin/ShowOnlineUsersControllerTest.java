package org.wise.portal.presentation.web.controllers.admin;

import static org.junit.Assert.assertEquals;
import static org.easymock.EasyMock.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ui.ModelMap;
import org.wise.portal.service.session.SessionService;

@RunWith(EasyMockRunner.class)
public class ShowOnlineUsersControllerTest {

  private ModelMap modelMap = new ModelMap();

  @TestSubject
  private ShowOnlineUsersController controller = new ShowOnlineUsersController();

  @Mock
  private SessionService sessionService;

  @Test
  public void show_ShouldRetrieveAndAddUsersInModel() {
    Set<String> students = Stream.of("student1", "student2", "student3")
        .collect(Collectors.toCollection(HashSet::new));
    Set<String> teachers = Stream.of("teacher1", "teacher2")
        .collect(Collectors.toCollection(HashSet::new));
    expect(sessionService.getLoggedInStudents()).andReturn(students);
    expect(sessionService.getLoggedInTeachers()).andReturn(teachers);
    replay(sessionService);
    String view = controller.show(modelMap);
    assertEquals("admin/account/manageusers", view);
    assertEquals(students, modelMap.get("loggedInStudentUsernames"));
    assertEquals(teachers, modelMap.get("loggedInTeacherUsernames"));
    verify(sessionService);
  }
}
