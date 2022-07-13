package org.wise.portal.presentation.web.controllers.admin;

import static org.junit.Assert.assertEquals;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ui.ModelMap;
import org.wise.portal.service.authentication.UserDetailsService;

@RunWith(EasyMockRunner.class)
public class ShowAllUsersControllerTest {

  private ModelMap modelMap = new ModelMap();

  @TestSubject
  private ShowAllUsersController controller = new ShowAllUsersController();

  @Mock
  private UserDetailsService userDetailsService;

  @Test
  public void show_Student_ShouldRetrieveAndAddStudentsInModel() {
    List<String> students = Stream.of("student1", "student2", "student3")
        .collect(Collectors.toCollection(ArrayList::new));
    expect(userDetailsService.retrieveAllStudentUsernames()).andReturn(students);
    replay(userDetailsService);
    String view = controller.show("student", modelMap);
    assertEquals("admin/account/manageusers", view);
    assertEquals(students, modelMap.get("students"));
    verify(userDetailsService);
  }

  @Test
  public void show_Teacher_ShouldRetrieveAndAddTeachersInModel() {
    List<String> teachers = Stream.of("teacher1", "teacher2")
        .collect(Collectors.toCollection(ArrayList::new));
    expect(userDetailsService.retrieveAllTeacherUsernames()).andReturn(teachers);
    replay(userDetailsService);
    String view = controller.show("teacher", modelMap);
    assertEquals("admin/account/manageusers", view);
    assertEquals(teachers, modelMap.get("teachers"));
    verify(userDetailsService);
  }
}
