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

import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.presentation.web.exception.InvalidPasswordException;
import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

@RunWith(EasyMockRunner.class)
public class ChangeStudentPasswordControllerTest extends APIControllerTest {

  @TestSubject
  private ChangeStudentPasswordController controller = new ChangeStudentPasswordController();

  @Test
	public void changeStudentPassword_NoWritePermission_ThrowAccessDenied() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(false);
    replay(runService);
    try {
      controller.changeStudentPassword(teacherAuth, runId1, student1Id, "a", "b");
      fail("Expected AccessDeniedException to be thrown");
    } catch (AccessDeniedException e) {}
		verify(runService);
	}

  @Test
  public void changeStudentPassword_InvalidTeacherPassword_ThrowInvalidPassword() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(userService.isPasswordCorrect(teacher1, "badTeacherPass")).andReturn(false);
    replay(runService, userService);
    try {
      controller.changeStudentPassword(teacherAuth, runId1, student1Id, "badTeacherPass", "newStudentPass");
      fail("Expected InvalidPassowrdException to be thrown");
    } catch (InvalidPasswordException e) {}
		verify(runService, userService);
  }

  @Test
  public void changeStudentPassword_validTeacherPassword_ChangeStudentPassword() throws Exception {
    expect(runService.retrieveById(runId1)).andReturn(run1);
    expect(runService.hasWritePermission(teacherAuth, run1)).andReturn(true);
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(userService.isPasswordCorrect(teacher1, "teacherPass")).andReturn(true);
    expect(userService.retrieveById(student1Id)).andReturn(student1);
    expect(userService.updateUserPassword(student1, "newStudentPass")).andReturn(student1);
    replay(runService, userService);
    controller.changeStudentPassword(teacherAuth, runId1, student1Id, "teacherPass", "newStudentPass");
		verify(runService, userService);
  }
}
