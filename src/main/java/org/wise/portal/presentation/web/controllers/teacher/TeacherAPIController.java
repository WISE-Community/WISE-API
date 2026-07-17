package org.wise.portal.presentation.web.controllers.teacher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.PeriodNotFoundException;
import org.wise.portal.domain.authentication.Schoollevel;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.user.UserAPIController;
import org.wise.portal.presentation.web.response.SimpleResponse;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.usertags.UserTagsService;

/**
 * Teacher REST API
 *
 * @author Jonathan Lim-Breitbart
 * @author Geoffrey Kwan
 * @author Hiroki Terashima
 */
@RestController
@RequestMapping("/api/teacher")
@Secured({ "ROLE_TEACHER" })
public class TeacherAPIController extends UserAPIController {

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private UserTagsService userTagsService;

  @Value("${google.clientId:}")
  private String googleClientId;

  @Value("${google.clientSecret:}")
  private String googleClientSecret;

  @GetMapping("/runs")
  List<HashMap<String, Object>> getRuns(Authentication auth,
      @RequestParam(required = false) Integer max) {
    User user = userService.retrieveUserByUsername(auth.getName());
    List<Run> runs = runService.getRunListByOwner(user);
    runs.addAll(runService.getRunListBySharedOwner(user));
    runs.sort((a, b) -> b.getStarttime().compareTo(a.getStarttime()));
    if (max != null && max > 0) {
      runs = runs.subList(0, Math.min(max, runs.size()));
    }
    return getRunsList(user, runs);
  }

  private List<HashMap<String, Object>> getRunsList(User user, List<Run> runs) {
    List<HashMap<String, Object>> runsList = new ArrayList<HashMap<String, Object>>();
    for (Run run : runs) {
      HashMap<String, Object> runMap = getRunMap(user, run);
      ((HashMap<String, Object>) runMap.get("project")).put("tags",
          userTagsService.getTagsList(user, run.getProject()));
      runsList.add(runMap);
    }
    return runsList;
  }

  @GetMapping("/run/{runId}")
  HashMap<String, Object> getRun(Authentication auth, @PathVariable Long runId)
      throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(auth.getName());
    Run run = runService.retrieveById(runId);
    return getRunMap(user, run);
  }

  @Override
  protected HashMap<String, Object> getRunMap(User user, Run run) {
    HashMap<String, Object> map = super.getRunMap(user, run);
    map.put("sharedOwners", getRunSharedOwnersList(run));
    List<String> periods = new ArrayList<String>();
    for (Group period : run.getPeriods()) {
      periods.add(period.getName());
    }
    map.put("periods", periods);
    map.put("lastRun", run.getLastRun());
    return map;
  }

  @GetMapping("/projectlastrun/{projectId}")
  HashMap<String, Object> getProjectLastRun(Authentication auth, @PathVariable Long projectId) {
    User user = userService.retrieveUserByUsername(auth.getName());
    List<Run> runsOfProject = runService.getProjectRuns(projectId);
    if (runsOfProject.size() > 0) {
      Run run = runsOfProject.get(0);
      if (run.getLastRun() != null) {
        return getRunMap(user, run);
      }
    }
    return null;
  }

  @GetMapping("/usernames")
  List<String> getAllTeacherUsernames() {
    return userDetailsService.retrieveAllTeacherUsernames();
  }

  private List<HashMap<String, Object>> getRunSharedOwnersList(Run run) {
    List<HashMap<String, Object>> sharedOwners = new ArrayList<HashMap<String, Object>>();
    for (User sharedOwner : run.getSharedowners()) {
      TeacherUserDetails ud = (TeacherUserDetails) sharedOwner.getUserDetails();
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("id", sharedOwner.getId());
      map.put("username", ud.getUsername());
      map.put("firstName", ud.getFirstname());
      map.put("lastName", ud.getLastname());
      map.put("permissions", getSharedOwnerPermissionsList(run, sharedOwner));
      sharedOwners.add(map);
    }
    return sharedOwners;
  }

  private List<Integer> getSharedOwnerPermissionsList(Run run, User user) {
    List<Integer> permissions = new ArrayList<Integer>();
    List<Permission> sharedTeacherPermissions = runService.getSharedTeacherPermissions(run, user);
    for (Permission permission : sharedTeacherPermissions) {
      permissions.add(permission.getMask());
    }
    return permissions;
  }

  @PostMapping("/run/create")
  HashMap<String, Object> createRun(Authentication auth, HttpServletRequest request,
      @RequestParam("projectId") Long projectId, @RequestParam("periods") String periods,
      @RequestParam boolean isSurvey,
      @RequestParam("maxStudentsPerTeam") Integer maxStudentsPerTeam,
      @RequestParam("startDate") Long startDate,
      @RequestParam(value = "endDate", required = false) Long endDate,
      @RequestParam(value = "isLockedAfterEndDate", defaultValue = "false") Boolean isLockedAfterEndDate)
      throws Exception {
    User user = userService.retrieveUserByUsername(auth.getName());
    Locale locale = request.getLocale();
    Set<String> periodNames = createPeriodNamesSet(periods);
    Run run = runService.createRun(projectId, user, periodNames, isSurvey, maxStudentsPerTeam,
        startDate, endDate, isLockedAfterEndDate, locale);
    return getRunMap(user, run);
  }

  private Set<String> createPeriodNamesSet(String periodsString) {
    Set<String> periods = new TreeSet<String>();
    String[] periodsSplit = periodsString.split(",");
    for (String period : periodsSplit) {
      periods.add(period.trim());
    }
    return periods;
  }

  @PostMapping("/profile/update")
  SimpleResponse updateProfile(Authentication auth, @RequestParam("displayName") String displayName,
      @RequestParam("email") String email, @RequestParam("city") String city,
      @RequestParam("state") String state, @RequestParam("country") String country,
      @RequestParam("schoolName") String schoolName,
      @RequestParam("schoolLevel") String schoolLevel, @RequestParam("language") String language) {
    User user = userService.retrieveUserByUsername(auth.getName());
    TeacherUserDetails tud = (TeacherUserDetails) user.getUserDetails();
    tud.setEmailAddress(email);
    tud.setDisplayname(displayName);
    tud.setCity(city);
    tud.setState(state);
    tud.setCountry(country);
    tud.setSchoolname(schoolName);
    tud.setSchoollevel(Schoollevel.valueOf(schoolLevel));
    tud.setLanguage(language);
    userService.updateUser(user);
    return new SimpleResponse("success", "profileUpdated");
  }

  @PostMapping("/run/add/period")
  HashMap<String, Object> addPeriodToRun(Authentication auth, @RequestParam("runId") Long runId,
      @RequestParam("periodName") String periodName) throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(auth.getName());
    Run run = runService.retrieveById(runId);
    HashMap<String, Object> response = new HashMap<String, Object>();
    if (run.isTeacherAssociatedToThisRun(user)) {
      try {
        if (run.getPeriodByName(periodName) != null) {
          response.put("status", "error");
          response.put("messageCode", "periodNameAlreadyExists");
        }
      } catch (PeriodNotFoundException e) {
        runService.addPeriodToRun(runId, periodName);
        response.put("status", "success");
      }
      response.put("run", getRunMap(user, run));
    } else {
      response.put("status", "error");
      response.put("messageCode", "noPermissionToAddPeriod");
    }
    return response;
  }

  @PostMapping("/run/delete/period")
  HashMap<String, Object> deletePeriodFromRun(Authentication auth,
      @RequestParam("runId") Long runId, @RequestParam("periodName") String periodName)
      throws ObjectNotFoundException, PeriodNotFoundException {
    User user = userService.retrieveUserByUsername(auth.getName());
    Run run = runService.retrieveById(runId);
    HashMap<String, Object> response = new HashMap<String, Object>();
    if (run.isTeacherAssociatedToThisRun(user)) {
      Group period = run.getPeriodByName(periodName);
      if (period.getMembers().size() == 0) {
        runService.deletePeriodFromRun(runId, periodName);
        response.put("status", "success");
      } else {
        response.put("status", "error");
        response.put("messageCode", "notAllowedToDeletePeriodWithStudents");
      }
      response.put("run", getRunMap(user, run));
    } else {
      response.put("status", "error");
      response.put("messageCode", "noPermissionToDeletePeriod");
    }
    return response;
  }

  @PostMapping("/run/update/studentsperteam")
  HashMap<String, Object> editRunStudentsPerTeam(Authentication auth,
      @RequestParam("runId") Long runId, @RequestParam("maxStudentsPerTeam") Integer newMax)
      throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(auth.getName());
    Run run = runService.retrieveById(runId);
    HashMap<String, Object> response = new HashMap<String, Object>();
    if (run.isTeacherAssociatedToThisRun(user)) {
      if (newMax != 1 || runService.canDecreaseMaxStudentsPerTeam(run.getId())) {
        runService.setMaxWorkgroupSize(runId, newMax);
        response.put("status", "success");
      } else {
        response.put("status", "error");
        response.put("messageCode", "notAllowedToDecreaseMaxStudentsPerTeam");
      }
      response.put("run", getRunMap(user, run));
    } else {
      response.put("status", "error");
      response.put("messageCode", "noPermissionToChangeMaxStudentsPerTeam");
    }
    return response;
  }

  @PostMapping("/run/update/starttime")
  HashMap<String, Object> editRunStartTime(Authentication authentication,
      @RequestParam("runId") Long runId, @RequestParam("startTime") Long startTime)
      throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(authentication.getName());
    Run run = runService.retrieveById(runId);
    HashMap<String, Object> response = new HashMap<String, Object>();
    if (run.isTeacherAssociatedToThisRun(user)) {
      Long endTime = run.getEndTimeMilliseconds();
      if (endTime == null || startTime < endTime) {
        runService.setStartTime(runId, startTime);
        response.put("status", "success");
      } else {
        response.put("status", "error");
        response.put("messageCode", "startDateAfterEndDate");
      }
      response.put("run", getRunMap(user, run));
    } else {
      response.put("status", "error");
      response.put("messageCode", "noPermissionToChangeDate");
    }
    return response;
  }

  @PostMapping("/run/update/endtime")
  HashMap<String, Object> editRunEndTime(Authentication authentication,
      @RequestParam("runId") Long runId,
      @RequestParam(value = "endTime", required = false) Long endTime)
      throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(authentication.getName());
    Run run = runService.retrieveById(runId);
    HashMap<String, Object> response = new HashMap<String, Object>();
    if (run.isTeacherAssociatedToThisRun(user)) {
      if (endTime == null || run.getStartTimeMilliseconds() < endTime) {
        runService.setEndTime(runId, endTime);
        response.put("status", "success");
      } else {
        response.put("status", "error");
        response.put("messageCode", "endDateBeforeStartDate");
      }
      response.put("run", getRunMap(user, run));
    } else {
      response.put("status", "error");
      response.put("messageCode", "noPermissionToChangeDate");
    }
    return response;
  }

  @PostMapping("/run/update/islockedafterenddate")
  HashMap<String, Object> editRunIsLockedAfterEndDate(Authentication authentication,
      @RequestParam("runId") Long runId,
      @RequestParam("isLockedAfterEndDate") Boolean isLockedAfterEndDate)
      throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(authentication.getName());
    Run run = runService.retrieveById(runId);
    HashMap<String, Object> response = new HashMap<String, Object>();
    if (run.isTeacherAssociatedToThisRun(user)) {
      runService.setIsLockedAfterEndDate(runId, isLockedAfterEndDate);
      response.put("status", "success");
      response.put("run", getRunMap(user, run));
    } else {
      response.put("status", "error");
      response.put("messageCode", "noPermissionToChangeIsLockedAfterEndDate");
    }
    return response;
  }
}
