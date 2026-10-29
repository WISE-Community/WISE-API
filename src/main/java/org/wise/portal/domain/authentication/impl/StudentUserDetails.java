/**
 * Copyright (c) 2008-2017 Regents of the University of California (Regents).
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
package org.wise.portal.domain.authentication.impl;

import java.io.Serial;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.MutableUserDetails;

/**
 * Implementation class of <code>MutableUserDetails</code> that uses an EJB3 compliant object
 * persistence mechanism.
 *
 * UserDetails for a student in WISE
 *
 * @author Hiroki Terashima
 */
@Entity
@Table(name = StudentUserDetails.DATA_STORE_NAME)
public class StudentUserDetails extends PersistentUserDetails implements MutableUserDetails {

  @Transient
  public static final String DATA_STORE_NAME = "student_user_details";

  @Transient
  public static final String COLUMN_NAME_FIRSTNAME = "firstname";

  @Transient
  public static final String COLUMN_NAME_LASTNAME = "lastname";

  @Transient
  private static final String COLUMN_NAME_SIGNUPDATE = "signupdate";

  @Transient
  public static final String COLUMN_NAME_GENDER = "gender";

  @Transient
  public static final String COLUMN_NAME_BIRTHDAY = "birthday";

  @Transient
  public static final String COLUMN_NAME_NUMBEROFLOGINS = "numberoflogins";

  @Transient
  public static final String COLUMN_NAME_LASTLOGINTIME = "lastlogintime";

  @Transient
  public static final String COLUMN_NAME_ACCOUNTQUESTION = "accountquestion";

  @Transient
  public static final String COLUMN_NAME_ACCOUNTANSWER = "accountanswer";

  @Serial
  @Transient
  private static final long serialVersionUID = 1L;

  @Column(name = StudentUserDetails.COLUMN_NAME_FIRSTNAME, nullable = false)
  @Getter
  @Setter
  private String firstname;

  @Column(name = StudentUserDetails.COLUMN_NAME_LASTNAME, nullable = false)
  @Getter
  @Setter
  private String lastname;

  @Column(name = StudentUserDetails.COLUMN_NAME_SIGNUPDATE, nullable = false)
  @Getter
  @Setter
  private Date signupdate;

  @Column(name = StudentUserDetails.COLUMN_NAME_GENDER, nullable = false)
  @Getter
  @Setter
  private Gender gender;

  @Column(name = StudentUserDetails.COLUMN_NAME_BIRTHDAY, nullable = true)
  @Getter
  @Setter
  private Date birthday;

  @Column(name = StudentUserDetails.COLUMN_NAME_NUMBEROFLOGINS, nullable = false)
  @Getter
  @Setter
  private Integer numberOfLogins;

  @Column(name = StudentUserDetails.COLUMN_NAME_LASTLOGINTIME)
  @Setter
  private Date lastLoginTime;

  @Column(name = StudentUserDetails.COLUMN_NAME_ACCOUNTQUESTION, nullable = true)
  @Getter
  @Setter
  private String accountQuestion;

  @Column(name = StudentUserDetails.COLUMN_NAME_ACCOUNTANSWER, nullable = true)
  @Getter
  @Setter
  private String accountAnswer;

  public String getCoreUsername() {
    String firstname = getFirstname().replaceAll("[\\s-]+", "");
    String lastnameInitial = getLastname().substring(0, 1);
    return firstname + lastnameInitial;
  }

  /**
   * Get the next numeric username suffix for students.
   * 
   * @param currentUsernameSuffix the current suffix
   * @return the next suffix, starting with "2" after the unsuffixed username
   */
  public String getNextUsernameSuffix(String currentUsernameSuffix) {
    if (currentUsernameSuffix == null) {
      return "";
    } else if (currentUsernameSuffix.isEmpty()) {
      return "2";
    } else {
      return String.valueOf(Integer.parseInt(currentUsernameSuffix) + 1);
    }
  }

  public Date getLastLoginTime() {
    if (lastLoginTime != null) {
      return lastLoginTime;
    } else {
      return Calendar.getInstance().getTime();
    }
  }

  public void incrementNumberOfLogins() {
    this.numberOfLogins++;
  }

  public HashMap<String, Object> getInfo() {
    HashMap<String, Object> infoMap = new HashMap<String, Object>();
    infoMap.put("ID", this.getId());
    infoMap.put("First Name", this.getFirstname());
    infoMap.put("Last Name", this.getLastname());
    infoMap.put("Username", this.getUsername());
    infoMap.put("Sign Up Date", this.getSignupdate());
    infoMap.put("Gender", this.getGender().toString());
    infoMap.put("Number of Logins", this.getNumberOfLogins().toString());
    infoMap.put("Last Login", this.getLastLoginTime());
    infoMap.put("Language", this.getLanguage());
    return infoMap;
  }
}
