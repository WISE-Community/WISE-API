/**
 * Copyright (c) 2008-2015 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.domain.general.contactwise.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.MutableUserDetails;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.general.contactwise.IssueType;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.user.UserService;

/**
 * @author Hiroki Terashima
 * @author Geoffrey Kwan
 */
@Getter
@Setter
public class ContactWISEForm implements Serializable {

  private static final long serialVersionUID = 1L;

  protected IssueType issuetype;

  protected String name;

  protected String email;

  private Long teacherId;

  private String teacherName;

  protected String summary;

  protected String description;

  private Boolean isStudent = false;

  protected String usersystem;

  private String projectName;

  private Long projectId;

  private Long runId;

  private String operatingSystemName;

  private String operatingSystemVersion;

  private String browserName;

  private String browserVersion;

  public String getMailSubject() {
    return "[Contact WISE] " + issuetype + ": " + summary;
  }

  public String getMailMessage() {
    StringBuffer message = new StringBuffer();

    if (getIsStudent()) {
      // a student is submitting this contact form and we are cc'ing their teacher
      message.append("Dear " + getTeacherName() + ",");
      message.append("\n\n");
      message.append("One of your students has submitted a WISE trouble ticket.\n\n");
    }

    message.append("Contact SCORE Project Request\n");
    message.append("=================\n");
    message.append("Name: " + name + "\n");

    /*
     * do not display the Email line if email is null or blank.
     * this variable will be null if the user is a student.
     */
    if (email != null && !email.equals("")) {
      message.append("Email: " + email + "\n");
    }

    message.append("Project Name: " + projectName + "\n");
    message.append("Project ID: " + projectId + "\n");

    // display the run id if it is not null
    if (runId != null) {
      message.append("Run ID: " + runId + "\n");
    }

    message.append("Issue Type: " + issuetype + "\n");
    message.append("Summary: " + summary + "\n");
    message.append("Description: " + description + "\n");

    String operatingSystem = "";

    if (this.operatingSystemName != null) {
      operatingSystem = this.operatingSystemName;
    }

    if (this.operatingSystemVersion != null) {
      operatingSystem += " " + this.operatingSystemVersion;
    }

    if (operatingSystem != null && !operatingSystem.equals("")) {
      message.append("Operating System: " + operatingSystem + "\n");
    }

    String browser = "";

    if (this.browserName != null) {
      browser = this.browserName;
    }

    if (this.browserVersion != null) {
      browser += " " + this.browserVersion;
    }

    if (browser != null && !browser.equals("")) {
      message.append("Browser: " + browser + "\n");
    }

    message.append("User System: " + usersystem + "\n");

    if (getIsStudent()) {
      // a student is submitting this contact form and we are cc'ing their teacher
      message.append("\nWe recommend that you follow up with your student if necessary. If you need further assistance, you can 'Reply to all' on this email to contact us.");
    }

    return message.toString();
  }

  public void setIsStudent(Boolean isStudent) {
    this.isStudent = isStudent;
  }

  public void setIsStudent(User user) {
    if (user != null && user.getUserDetails() instanceof StudentUserDetails) {
      isStudent = true;
    }
  }

  public Boolean getIsStudent() {
    return isStudent;
  }
}
