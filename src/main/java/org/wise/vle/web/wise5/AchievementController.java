package org.wise.vle.web.wise5;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.portal.service.workgroup.WorkgroupService;
import org.wise.portal.spring.data.redis.MessagePublisher;
import org.wise.vle.domain.achievement.Achievement;

@RestController
@RequestMapping("/api/achievement")
public class AchievementController {

  @Autowired
  private MessagePublisher redisPublisher;

  @Autowired
  private UserService userService;

  @Autowired
  private VLEService vleService;

  @Autowired
  private WorkgroupService workgroupService;

  @GetMapping("/{runId}")
  public List<Achievement> getWISE5StudentAchievements(@PathVariable("runId") RunImpl run,
      @RequestParam(value = "id", required = false) Integer id,
      @RequestParam(value = "workgroupId", required = false) WorkgroupImpl workgroup,
      @RequestParam(value = "achievementId", required = false) String achievementId,
      @RequestParam(value = "type", required = false) String type,
      Authentication auth) throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (!isAssociatedWithRun(run, user, workgroup)) {
      throw new AccessDeniedException("Not associated with run");
    }
    return vleService.getAchievements(id, run, workgroup, achievementId, type);
  }

  private Boolean isAssociatedWithRun(Run run, User user, Workgroup workgroup) {
    return isStudentAssociatedWithRun(run, user, workgroup)
        || isTeacherAssociatedWithRun(run, user);
  }

  private Boolean isStudentAssociatedWithRun(Run run, User user, Workgroup workgroup) {
    return user.isStudent() && run.isStudentAssociatedToThisRun(user)
        && workgroupService.isUserInWorkgroupForRun(user, run, workgroup);
  }

  private Boolean isTeacherAssociatedWithRun(Run run, User user) {
    return (user.isTeacher() && run.isTeacherAssociatedToThisRun(user)) || user.isAdmin();
  }

  @PostMapping("/{runId}")
  public Achievement saveAchievement(@PathVariable("runId") RunImpl run,
      @RequestParam(value = "id", required = false) Integer id,
      @RequestParam(value = "workgroupId", required = true) WorkgroupImpl workgroup,
      @RequestParam(value = "achievementId", required = true) String achievementId,
      @RequestParam(value = "type", required = true) String type,
      @RequestParam(value = "data", required = true) String data,
      Authentication auth) throws JSONException, ObjectNotFoundException, IOException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (isAllowedToSave(run, user, workgroup)) {
      Achievement achievement = vleService.saveAchievement(id, run, workgroup, achievementId,
          type, data);
      achievement.convertToClientAchievement();
      broadcastAchievementToTeacher(achievement);
      return achievement;
    }
    throw new AccessDeniedException("Not allowed to save achievement");
  }

  private boolean isAllowedToSave(Run run, User user, Workgroup workgroup) {
    if (user.isStudent() && run.isStudentAssociatedToThisRun(user) &&
        workgroupService.isUserInWorkgroupForRun(user, run, workgroup)) {
      return true;
    } else if (user.isTeacher() &&
        (run.getOwner().equals(user) || run.getSharedowners().contains(user))) {
      return true;
    } else {
      return false;
    }
  }

  public void broadcastAchievementToTeacher(Achievement achievement) throws JSONException {
    JSONObject message = new JSONObject();
    message.put("type", "achievementToTeacher");
    message.put("topic", String.format("/topic/teacher/%s", achievement.getRunId()));
    message.put("achievement", achievement.toJSON());
    redisPublisher.publish(message.toString());
  }
}
