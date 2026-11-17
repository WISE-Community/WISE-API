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
package org.wise.portal.presentation.web.controllers.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.wise.portal.domain.AccountQuestion;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.project.impl.Projectcode;
import org.wise.portal.domain.user.BatchCreateUserAccountsUpload;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.student.StudentService;
import org.wise.portal.service.user.UserService;

/**
 * Admin tool for batch creating user accounts. Admin uploads a csv file
 * containing a row for each new user to be added to WISE.
 *
 * Currently only supports student accounts
 *
 * @author Hiroki Terashima
 */
@Secured("ROLE_ADMINISTRATOR")
@Controller
@RequestMapping("/admin/account/batchcreateuseraccounts.html")
public class BatchCreateUserAccountsController {

  private static final String CSV_FORMAT_ERROR =
      "This CSV file does not match the updated format. Please download and use the updated template.";

  @Autowired
  private UserService userService;

  @Autowired
  private StudentService studentService;

  @PostMapping
  protected ModelAndView onSubmit(
      @ModelAttribute("csvFile") BatchCreateUserAccountsUpload csvUpload,
      BindingResult result) throws Exception {
    List<String[]> studentRows = new ArrayList<String[]>();
    Gender[] genderArray = {Gender.MALE,Gender.FEMALE,Gender.UNSPECIFIED};
    AccountQuestion[] accountQuestionArray =
        { AccountQuestion.QUESTION_FOUR, AccountQuestion.QUESTION_TWO,
        AccountQuestion.QUESTION_THREE };
    if (!readStudentRows(csvUpload.getFile(), studentRows, genderArray, accountQuestionArray)) {
      return csvFormatError();
    }
    ArrayList<String> newUsernames =
        createStudentAccounts(studentRows, genderArray, accountQuestionArray);
    ModelAndView modelAndView = new ModelAndView("admin/account/batchcreateuseraccounts");
    modelAndView.addObject("msg", "Batch create user accounts complete!");
    modelAndView.addObject("newUsernames", newUsernames);
    return modelAndView;
  }

  private boolean readStudentRows(MultipartFile csvFile, List<String[]> studentRows,
      Gender[] genderArray, AccountQuestion[] accountQuestionArray) throws IOException {
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (!line.isBlank()) {
          String[] userInfo = line.split(",", -1);
          boolean headerRow = isHeaderRow(userInfo);
          boolean invalidRow = headerRow
              ? line.toLowerCase(Locale.ROOT).contains("birthday")
              : isInvalidRow(genderArray, accountQuestionArray, userInfo);
          if (invalidRow) {
            return false;
          } else if (!headerRow) {
            studentRows.add(userInfo);
          }
        }
      }
    }
    return true;
  }

  private boolean isInvalidRow(Gender[] genderArray, AccountQuestion[] accountQuestionArray,
      String[] userInfo) {
    return userInfo.length != 8 || !hasValidAccountIndexes(userInfo, genderArray,
        accountQuestionArray);
  }

  private boolean isHeaderRow(String[] userInfo) {
    return userInfo.length >= 2 && "Firstname".equals(userInfo[0])
        && "Lastname".equals(userInfo[1]);
  }

  private ArrayList<String> createStudentAccounts(List<String[]> studentRows,
      Gender[] genderArray, AccountQuestion[] accountQuestionArray) throws Exception {
    ArrayList<String> newUsernames = new ArrayList<String>();
    for (String[] userInfo : studentRows) {
      String firstname = userInfo[0];
      String lastname = userInfo[1];
      Gender gender = genderArray[Integer.valueOf(userInfo[2])];
      String password = userInfo[3];
      AccountQuestion accountQuestion = accountQuestionArray[Integer.valueOf(userInfo[4])];
      String accountAnswer = userInfo[5];
      StudentUserDetails studentUserDetails = new StudentUserDetails();
      studentUserDetails.setFirstname(firstname);
      studentUserDetails.setLastname(lastname);
      studentUserDetails.setGender(gender);
      studentUserDetails.setPassword(password);
      studentUserDetails.setAccountQuestion(accountQuestion.toString());
      studentUserDetails.setAccountAnswer(accountAnswer);
      studentUserDetails.setSignupdate(Calendar.getInstance().getTime());
      User user = userService.createUser(studentUserDetails);
      String accessCode = userInfo[6];
      String period = userInfo[7];
      Projectcode projectcode = new Projectcode(accessCode, period);
      studentService.addStudentToRun(user, projectcode);
      newUsernames.add(studentUserDetails.getUsername());
    }
    return newUsernames;
  }

  private boolean hasValidAccountIndexes(String[] userInfo, Gender[] genderArray,
      AccountQuestion[] accountQuestionArray) {
    try {
      int genderIndex = Integer.parseInt(userInfo[2]);
      int accountQuestionIndex = Integer.parseInt(userInfo[4]);
      return genderIndex >= 0 && genderIndex < genderArray.length
          && accountQuestionIndex >= 0 && accountQuestionIndex < accountQuestionArray.length;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private ModelAndView csvFormatError() {
    ModelAndView modelAndView = new ModelAndView("admin/account/batchcreateuseraccounts");
    modelAndView.addObject("error", CSV_FORMAT_ERROR);
    return modelAndView;
  }

  @GetMapping
  public ModelAndView initializeForm(ModelMap model) {
    ModelAndView mav = new ModelAndView();
    mav.addObject("csvFile", new BatchCreateUserAccountsUpload());
    return mav;
  }
}
