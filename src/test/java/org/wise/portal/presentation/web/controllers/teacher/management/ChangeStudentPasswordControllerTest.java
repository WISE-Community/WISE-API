/**
 * Copyright (c) 2021 Regents of the University of California (Regents). Created
 * by TELS, Graduate School of Education, University of California at Berkeley.
 *
 * This software is distributed under the GNU Lesser General Public License, v2.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWAREAND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.presentation.web.controllers.teacher.management;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.password.impl.PasswordServiceImpl;
import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(EasyMockRunner.class)
public class ChangeStudentPasswordControllerTest extends APIControllerTest {

  @TestSubject
  private ChangeStudentPasswordController controller = new ChangeStudentPasswordController();

  String STUDENT_PASSWORD_INVALID = PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT;
  String STUDENT_PASSWORD_VALID = PasswordServiceImpl.VALID_PASSWORD;
  String TEACHER_PASSWORD_CORRECT = "correctTeacherPassword1";
  String TEACHER_PASSWORD_INCORRECT = "incorrectTeacherPassword1";

  @Before
  public void setUp() {
    super.setUp();
    ReflectionTestUtils.setField(controller, "passwordService", new PasswordServiceImpl());
  }

  @Test
  public void changeStudentPassword_NoWritePermission_ThrowAccessDenied() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(false);
    replayServices();
    try {
      controller.changeStudentPassword(teacherAuth, runId1, student1Id, "a", "b");
      fail("Expected AccessDeniedException to be thrown");
    } catch (AccessDeniedException e) {
    }
    verifyServices();
  }

  private void setupChangeStudentPasswordExpect() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
  }

  private void assertResponseValues(ResponseEntity<Map<String, Object>> response,
      HttpStatus expectedStatus, String expectedMessageCode) {
    assertEquals(expectedStatus, response.getStatusCode());
    assertEquals(expectedMessageCode, response.getBody().get("messageCode"));
  }

  private void replayServices() {
    replay(runService, userService);
  }

  private void verifyServices() {
    verify(runService, userService);
  }

  @Test
  public void changeStudentPassword_IncorrectTeacherPassword_ThrowInvalidPassword()
      throws Exception {
    setupChangeStudentPasswordExpect();
    expect(userService.isPasswordCorrect(teacher1, TEACHER_PASSWORD_INCORRECT)).andReturn(false);
    replayServices();
    ResponseEntity<Map<String, Object>> response = controller.changeStudentPassword(teacherAuth,
        runId1, student1Id, TEACHER_PASSWORD_INCORRECT, STUDENT_PASSWORD_VALID);
    assertResponseValues(response, HttpStatus.BAD_REQUEST, "incorrectPassword");
    verifyServices();
  }

  @Test
  public void changeStudentPassword_InvalidPassword_ReturnError() throws Exception {
    setupChangeStudentPasswordExpect();
    expect(userService.isPasswordCorrect(teacher1, TEACHER_PASSWORD_CORRECT)).andReturn(true);
    replayServices();
    ResponseEntity<Map<String, Object>> response = controller.changeStudentPassword(teacherAuth,
        runId1, student1Id, TEACHER_PASSWORD_CORRECT, STUDENT_PASSWORD_INVALID);
    assertResponseValues(response, HttpStatus.BAD_REQUEST, "invalidPassword");
    verifyServices();
  }

  @Test
  public void changeStudentPassword_validTeacherPassword_ChangeStudentPassword() throws Exception {
    setupChangeStudentPasswordExpect();
    expect(userService.isPasswordCorrect(teacher1, TEACHER_PASSWORD_CORRECT)).andReturn(true);
    expect(userService.retrieveById(student1Id)).andReturn(student1);
    expect(userService.updateUserPassword(student1, STUDENT_PASSWORD_VALID)).andReturn(student1);
    replayServices();
    ResponseEntity<Map<String, Object>> response = controller.changeStudentPassword(teacherAuth,
        runId1, student1Id, TEACHER_PASSWORD_CORRECT, STUDENT_PASSWORD_VALID);
    assertResponseValues(response, HttpStatus.OK, "passwordUpdated");
    verifyServices();
  }
}
