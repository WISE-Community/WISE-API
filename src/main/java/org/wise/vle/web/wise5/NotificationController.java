package org.wise.vle.web.wise5;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.notification.NotificationService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.portal.spring.data.redis.MessagePublisher;
import org.wise.vle.domain.notification.Notification;

/**
 * WISE Notification API
 * @author Hiroki Terashima
 */
@RestController
@RequestMapping("/api")
public class NotificationController {

  @Autowired
  private VLEService vleService;

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private MessagePublisher redisPublisher;

  public void broadcastNotification(Notification notification) throws JSONException {
    notification.convertToClientNotification();
    JSONObject message = new JSONObject();
    message.put("type", "notification");
    message.put("topic", String.format("/topic/workgroup/%s", notification.getToWorkgroupId()));
    message.put("notification", notification.toJSON());
    redisPublisher.publish(message.toString());
  }

  @GetMapping("/notification/{runId}")
  protected List<Notification> getNotifications(
      Authentication auth,
      @PathVariable("runId") RunImpl run,
      @RequestParam(value = "id", required = false) Integer id,
      @RequestParam(value = "periodId", required = false) Integer periodId,
      @RequestParam(value = "toWorkgroupId", required = false) WorkgroupImpl toWorkgroup,
      @RequestParam(value = "groupId", required = false) String groupId,
      @RequestParam(value = "nodeId", required = false) String nodeId,
      @RequestParam(value = "componentId", required = false) String componentId)
      throws ObjectNotFoundException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (toWorkgroup != null) {
      if (isStudentAndNotAllowedToSaveNotification(user, run, toWorkgroup)) {
        return new ArrayList<Notification>();
      }
    } else if (!user.isAdmin() && !runService.hasRunPermission(run, user, BasePermission.READ)) {
      return new ArrayList<Notification>();
    }
    return vleService.getNotifications(id, run, periodId, toWorkgroup, groupId, nodeId,
        componentId);
  }

  @PostMapping("/notification/{runId}")
  protected Notification saveNotification(@PathVariable("runId") RunImpl run,
      @RequestBody Notification notification, Authentication authentication) throws Exception {
    User user = userService.retrieveUserByUsername(authentication.getName());
    Workgroup fromWorkgroup = notification.getFromWorkgroup();
    Workgroup toWorkgroup = notification.getToWorkgroup();
    if (user.isAdmin() || runService.hasRunPermission(run, user, BasePermission.READ)) {
    } else if (notification.getId() != null) {
      if (!toWorkgroup.getMembers().contains(user)) {
        return null;
      }
    } else if (fromWorkgroup != null) {
      if (isStudentAndNotAllowedToSaveNotification(user, run, fromWorkgroup)) {
        return null;
      }
    } else if (toWorkgroup != null) {
      if (fromWorkgroup == null) {
        if ("CRaterResult".equals(notification.getType())) {
        } else {
          return null;
        }
      }
    }
    Calendar now = Calendar.getInstance();
    notification.setServerSaveTime(new Timestamp(now.getTimeInMillis()));
    Notification savedNotification = notificationService.saveNotification(notification);
    broadcastNotification(savedNotification);
    return notification;
  }

  @PostMapping("/notification/{runId}/period/{periodId}")
  protected void notifyClassmatesInPeriod(@PathVariable Long runId,
      @PathVariable Long periodId, @RequestBody Notification notification,
      Authentication authentication) throws Exception {
    User user = userService.retrieveUserByUsername(authentication.getName());
    Run run = runService.retrieveById(runId);
    if (run.isStudentAssociatedToThisRun(user)) {
      List<Workgroup> workgroups = runService.getWorkgroups(runId, periodId);
      removeOwnAndTeacherWorkgroup(workgroups, notification.getFromWorkgroup());
      notifyClassmates(notification, workgroups);
    }
  }

  @PostMapping("/notification/{runId}/all-periods")
  protected void notifyClassmatesInAllPeriods(@PathVariable Long runId,
      @RequestBody Notification notification, Authentication authentication) throws Exception {
    User user = userService.retrieveUserByUsername(authentication.getName());
    Run run = runService.retrieveById(runId);
    if (run.isStudentAssociatedToThisRun(user)) {
      List<Workgroup> workgroups = runService.getWorkgroups(runId);
      removeOwnAndTeacherWorkgroup(workgroups, notification.getFromWorkgroup());
      notifyClassmates(notification, workgroups);
    }
  }

  private void removeOwnAndTeacherWorkgroup(List<Workgroup> workgroups, Workgroup ownWorkgroup) {
    workgroups.removeIf(workgroup -> workgroup.getId().equals(ownWorkgroup.getId()) ||
        workgroup.isTeacherWorkgroup());
  }

  private void notifyClassmates(Notification notification, List<Workgroup> workgroups)
      throws JSONException {
    for (Workgroup workgroup : workgroups) {
      Notification newNotification = new Notification();
      BeanUtils.copyProperties(notification, newNotification, "id", "toWorkgroupId");
      newNotification.setToWorkgroup(workgroup);
      newNotification.setPeriod(workgroup.getPeriod());
      Calendar now = Calendar.getInstance();
      newNotification.setServerSaveTime(new Timestamp(now.getTimeInMillis()));
      Notification savedNotification = notificationService.saveNotification(newNotification);
      broadcastNotification(savedNotification);
    }
  }

  private boolean isStudentAndNotAllowedToSaveNotification(User user, Run run,
      Workgroup fromWorkgroup) {
    return user.getUserDetails() instanceof StudentUserDetails &&
      (!run.isStudentAssociatedToThisRun(user) ||
        !fromWorkgroup.getMembers().contains(user));
  }

  @PostMapping("/notification/{runId}/dismiss")
  protected Notification dismissNotification(@PathVariable("runId") RunImpl run,
      @RequestBody Notification notification, Authentication authentication)
      throws IOException, ObjectNotFoundException, JSONException {
    User user = userService.retrieveUserByUsername(authentication.getName());
    if (canDismissNotification(user, notification, run)) {
      Timestamp timeDismissed = notification.getTimeDismissed();
      notification = vleService.dismissNotification(notification, timeDismissed);
      if ("CRaterResult".equals(notification.getType())) {
        String groupId = notification.getGroupId();
        List<Notification> notificationsInGroup = vleService.getNotificationsByGroupId(groupId);
        for (Notification notificationInGroup : notificationsInGroup) {
          if (notificationInGroup.getId().equals(notification.getId())) {
            continue;
          }
          vleService.dismissNotification(notificationInGroup, timeDismissed);
          broadcastNotification(notificationInGroup);
        }
      }
      return notification;
    }
    throw new AccessDeniedException("Access denied for notification");
  }

  private boolean canDismissNotification(User user, Notification notification, Run run) {
    return user.isAdmin() ||
        runService.hasRunPermission(run, user, BasePermission.READ) ||
        notification.getToWorkgroup().getMembers().contains(user);
  }
}
