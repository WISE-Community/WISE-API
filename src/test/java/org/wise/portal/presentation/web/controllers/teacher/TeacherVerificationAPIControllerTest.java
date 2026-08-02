package org.wise.portal.presentation.web.controllers.teacher;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.presentation.web.TeacherVerificationAPIController;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.mail.MailService;

@ExtendWith(EasyMockExtension.class)
public class TeacherVerificationAPIControllerTest extends APIControllerTest {
  
  @TestSubject
  private TeacherVerificationAPIController teacherVerificationAPIController = new TeacherVerificationAPIController();

  @Mock
  private HttpServletResponse response;
  
  @Mock
  private MailService mailService;

  @Test
  public void verifyTeacherAndRedirect_TeacherUnverified_RedirectsWithVerifiedTrue() throws IOException {
    createTeachers();
    expect(userService.retrieveTeacherByVerificationCode("efgh5678")).andReturn(teacher2);
    userService.updateUser(teacher2);
    expectLastCall();
    replay(userService);
    TeacherUserDetails tud = (TeacherUserDetails) teacher2.getUserDetails();
    mailService.sendWelcomeTeacherEmail(tud.getEmailAddress(), tud.getDisplayname(), tud.getUsername(), false, null, request);
    expectLastCall();
    replay(mailService);
    response.sendRedirect("/login?verified=true&username=" + tud.getUsername());
    expectLastCall();
    replay(response);
    teacherVerificationAPIController.verifyTeacherAndRedirect("efgh5678", response, request);
    verify(userService);
    verify(mailService);
    verify(response);
  }

  @Test
  public void verifyTeacherAndRedirect_TeacherAlreadyVerified_RedirectsWithVerifiedFalse() throws IOException {
    createTeachers();
    expect(userService.retrieveTeacherByVerificationCode("abcd1234")).andReturn(teacher1);
    replay(userService);
    response.sendRedirect("/login?verified=false&username=" + teacher1.getUserDetails().getUsername());
    expectLastCall();
    replay(response);
    teacherVerificationAPIController.verifyTeacherAndRedirect("abcd1234", response, request);
    verify(userService);
    verify(response);
  }

  @Test
  public void verifyTeacherAndRedirect_InvalidVerificationCode_RedirectsWithVerificationError() throws IOException {
    expect(userService.retrieveTeacherByVerificationCode("")).andReturn(null);
    replay(userService);
    response.sendRedirect("/login?verified=error");
    expectLastCall();
    replay(response);
    teacherVerificationAPIController.verifyTeacherAndRedirect("", response, request);
    verify(userService);
    verify(response);
  }
}
