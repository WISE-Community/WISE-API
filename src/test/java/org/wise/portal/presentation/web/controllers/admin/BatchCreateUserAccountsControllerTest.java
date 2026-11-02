package org.wise.portal.presentation.web.controllers.admin;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.easymock.Capture;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.wise.portal.domain.AccountQuestion;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.project.impl.Projectcode;
import org.wise.portal.domain.user.BatchCreateUserAccountsUpload;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.user.impl.UserImpl;
import org.wise.portal.service.student.StudentService;
import org.wise.portal.service.user.UserService;

@ExtendWith(EasyMockExtension.class)
public class BatchCreateUserAccountsControllerTest {

  @TestSubject
  private BatchCreateUserAccountsController controller = new BatchCreateUserAccountsController();

  @Mock
  private UserService userService;

  @Mock
  private StudentService studentService;

  @Test
  public void onSubmit_WithUpdatedCsvLayout_CreatesNameBasedStudentAndAddsToRun()
      throws Exception {
    String csv = "Firstname,Lastname,Gender,Password,Security Question Index,"
        + "Security Question Answer,Access Code,Class Period\n"
        + "John,Lennon,0,test,0,koala,Mole727,1\n";
    BatchCreateUserAccountsUpload upload = upload(csv);
    User createdUser = new UserImpl();
    Capture<StudentUserDetails> studentDetails = Capture.newInstance();
    expect(userService.createUser(capture(studentDetails))).andAnswer(() -> {
      ((StudentUserDetails) getCurrentArguments()[0]).setUsername("JohnL");
      return createdUser;
    });
    studentService.addStudentToRun(createdUser, new Projectcode("Mole727", "1"));
    expectLastCall();
    replay(userService, studentService);

    ModelAndView response = controller.onSubmit(upload, null);

    assertEquals("admin/account/batchcreateuseraccounts", response.getViewName());
    assertEquals("John", studentDetails.getValue().getFirstname());
    assertEquals("Lennon", studentDetails.getValue().getLastname());
    assertEquals("JohnL", studentDetails.getValue().getCoreUsername());
    assertEquals(Gender.MALE, studentDetails.getValue().getGender());
    assertEquals("test", studentDetails.getValue().getPassword());
    assertEquals(AccountQuestion.QUESTION_FOUR.toString(),
        studentDetails.getValue().getAccountQuestion());
    assertEquals("koala", studentDetails.getValue().getAccountAnswer());
    assertEquals(List.of("JohnL"), response.getModel().get("newUsernames"));
    verify(userService, studentService);
  }

  @Test
  public void onSubmit_WithLegacyBirthdayColumns_RejectsBeforeCreatingAccounts()
      throws Exception {
    String csv = "Firstname,Lastname,Gender,Birthday (Month),Birthday (Day),Password,"
        + "Security Question Index,Security Question Answer,Access Code,Class Period\n"
        + "John,Lennon,0,10,9,test,0,koala,Mole727,1\n";
    replay(userService, studentService);

    ModelAndView response = controller.onSubmit(upload(csv), null);

    assertEquals("admin/account/batchcreateuseraccounts", response.getViewName());
    assertTrue(((String) response.getModel().get("error")).contains("updated template"));
    verify(userService, studentService);
  }

  private BatchCreateUserAccountsUpload upload(String csv) {
    BatchCreateUserAccountsUpload upload = new BatchCreateUserAccountsUpload();
    upload.setFile(new MockMultipartFile("file", "students.csv", "text/csv",
        csv.getBytes(StandardCharsets.UTF_8)));
    return upload;
  }
}
