/**
 * Copyright (c) 2007-2021 Regents of the University of California (Regents).
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
package org.wise.portal.presentation.web.controllers.teacher.management;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.wise.portal.domain.authentication.MutableUserDetails;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.service.acl.AclService;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.workgroup.WorkgroupService;

/**
 * Controller for managing students in the run, like displaying students, exporting student names,
 * and updating workgroup memberships
 *
 * @author Patrick Lawler
 * @author Hiroki Terashima
 */
@Controller
@RequestMapping("/teacher/management")
public class ManageStudentsController {

  @Autowired
  private RunService runService;

  @Autowired
  private WorkgroupService workgroupService;

  @Autowired
  private AclService<Run> aclService;


  private boolean userCanViewRun(User user, Run run) {
    return user.isAdmin()
        || user.getUserDetails().hasGrantedAuthority(UserDetailsService.RESEARCHER_ROLE)
        || aclService.hasPermission(run, BasePermission.ADMINISTRATION, user)
        || aclService.hasPermission(run, BasePermission.READ, user);
  }

  /**
   * Get the students in the specified run and returns them in the model
   * @param runId id of the Run
   * @return modelAndView containing information needed to get student list
   * @throws Exception
   */
  @GetMapping("/studentlist")
  protected ModelAndView getStudentList(@RequestParam("runId") Long runId) throws Exception {
    Run run = runService.retrieveById(runId);
    if (userCanViewRun(ControllerUtil.getSignedInUser(), run)) {
      Set<Group> periods = run.getPeriods();
      Set<Group> requestedPeriods = new TreeSet<Group>();
      for (Group period : periods) {
        // TODO in future: filter by period...for now, include all periods
        requestedPeriods.add(period);
      }
      ModelAndView modelAndView = new ModelAndView();
      modelAndView.addObject("run", run);
      modelAndView.addObject("periods", requestedPeriods);
      return modelAndView;
    } else {
      return new ModelAndView("errors/accessdenied");
    }
  }

  /**
   * Handles request to export a list of students in the run
   *
   * @param runId id of the Run
   * @param response response to write the export into
   * @throws Exception
   */
  @GetMapping("/studentListExport")
  protected void exportStudentList(@RequestParam("runId") Long runId,
      HttpServletResponse response) throws Exception {
    Run run = runService.retrieveById(runId);
    Project project = run.getProject();
    User owner = run.getOwner();
    List<Workgroup> teacherWorkgroups = workgroupService.getWorkgroupListByRunAndUser(run, owner);
    // there should only be one workgroup for the owner
    Workgroup teacherWorkgroup = teacherWorkgroups.get(0);
    String teacherUsername = teacherWorkgroup.generateWorkgroupName();

    // get the meta data for the project
    Long projectId = (Long) project.getId();
    Long parentProjectId = project.getParentProjectId();
    String parentProjectIdStr = "N/A";
    if (parentProjectId != null) {
      parentProjectIdStr = parentProjectId.toString();
    }
    String projectName = project.getName();
    String runName = run.getName();
    Date startTime = run.getStarttime();
    Date endTime = run.getEndtime();

    int rowCounter = 0;
    int columnCounter = 0;

    int maxColumn = 0;
    HSSFWorkbook wb = new HSSFWorkbook();
    HSSFSheet mainSheet = wb.createSheet();

    columnCounter = 0;
    HSSFRow metaDataHeaderRow = mainSheet.createRow(rowCounter++);
    metaDataHeaderRow.createCell(columnCounter++).setCellValue("Teacher Login");
    metaDataHeaderRow.createCell(columnCounter++).setCellValue("Project Id");
    metaDataHeaderRow.createCell(columnCounter++).setCellValue("Parent Project Id");
    metaDataHeaderRow.createCell(columnCounter++).setCellValue("Project Name");
    metaDataHeaderRow.createCell(columnCounter++).setCellValue("Run Id");
    metaDataHeaderRow.createCell(columnCounter++).setCellValue("Run Name");
    metaDataHeaderRow.createCell(columnCounter++).setCellValue("Start Date");
    metaDataHeaderRow.createCell(columnCounter++).setCellValue("End Date");

    if (columnCounter > maxColumn) {
      maxColumn = columnCounter;
    }

    columnCounter = 0;
    HSSFRow metaDataRow = mainSheet.createRow(rowCounter++);
    metaDataRow.createCell(columnCounter++).setCellValue(teacherUsername);
    metaDataRow.createCell(columnCounter++).setCellValue(projectId);
    metaDataRow.createCell(columnCounter++).setCellValue(parentProjectIdStr);
    metaDataRow.createCell(columnCounter++).setCellValue(projectName);
    metaDataRow.createCell(columnCounter++).setCellValue(runId);
    metaDataRow.createCell(columnCounter++).setCellValue(runName);
    metaDataRow.createCell(columnCounter++).setCellValue(timestampToFormattedString(startTime));
    metaDataRow.createCell(columnCounter++).setCellValue(timestampToFormattedString(endTime));

    if (columnCounter > maxColumn) {
      maxColumn = columnCounter;
    }

    rowCounter++;

    columnCounter = 0;
    HSSFRow studentHeaderRow = mainSheet.createRow(rowCounter++);
    studentHeaderRow.createCell(columnCounter++).setCellValue("Period");
    studentHeaderRow.createCell(columnCounter++).setCellValue("Workgroup Id");
    studentHeaderRow.createCell(columnCounter++).setCellValue("Wise Id");
    studentHeaderRow.createCell(columnCounter++).setCellValue("Student Username");
    studentHeaderRow.createCell(columnCounter++).setCellValue("Student Name");

    Set<Group> periods = run.getPeriods();
    Iterator<Group> periodsIterator = periods.iterator();
    while(periodsIterator.hasNext()) {
      Group group = periodsIterator.next();

      String periodName = group.getName();
      Set<User> periodMembers = group.getMembers();
      Iterator<User> periodMembersIterator = periodMembers.iterator();
      while(periodMembersIterator.hasNext()) {
        User user = periodMembersIterator.next();
        List<Workgroup> workgroupListByRunAndUser = workgroupService.getWorkgroupListByRunAndUser(run, user);
        Long workgroupId = null;
        if (workgroupListByRunAndUser.size() > 0) {
          Workgroup workgroup = workgroupListByRunAndUser.get(0);
          workgroupId = workgroup.getId();
        }
        Long wiseId = user.getId();
        MutableUserDetails userDetails = (MutableUserDetails) user.getUserDetails();

        String username = "";
        String firstName = "";
        String lastName = "";
        String fullName = "";

        if (userDetails != null) {
          username = userDetails.getUsername();
          firstName = userDetails.getFirstname();
          lastName = userDetails.getLastname();
          fullName = firstName + " " + lastName;
        }

        columnCounter = 0;
        HSSFRow studentDataRow = mainSheet.createRow(rowCounter++);
        if (periodName != null && !periodName.equals("")) {
          try {
            studentDataRow.createCell(columnCounter).setCellValue(Long.parseLong(periodName));
          } catch(NumberFormatException e) {
            e.printStackTrace();
            studentDataRow.createCell(columnCounter).setCellValue(periodName);
          }
        }

        columnCounter++;
        if (workgroupId == null) {
          studentDataRow.createCell(columnCounter++).setCellValue("N/A");
        } else {
          studentDataRow.createCell(columnCounter++).setCellValue(workgroupId);
        }
        studentDataRow.createCell(columnCounter++).setCellValue(wiseId);
        studentDataRow.createCell(columnCounter++).setCellValue(username);
        studentDataRow.createCell(columnCounter++).setCellValue(fullName);

        if (columnCounter > maxColumn) {
          maxColumn = columnCounter;
        }
      }
    }
    response.setContentType("application/vnd.ms-excel");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + projectName + "-" + runId + "-student-names.xls\"");
    ServletOutputStream outputStream = response.getOutputStream();
    if (wb != null) {
      wb.write(outputStream);
    }
  }

  /**
   * Get the timestamp as a string
   * @param date the date object
   * @return the timstamp as a string
   * e.g.
   * Mar 9, 2011 8:50:47 PM
   */
  private String timestampToFormattedString(Date date) {
    String timestampString = "";
    if (date != null) {
      DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();
      long time = date.getTime();
      Date timestampDate = new Date(time);
      timestampString = dateTimeInstance.format(timestampDate);
    }
    return timestampString;
  }
}
