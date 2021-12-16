package org.wise.vle.web.wise5;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.portal.spring.data.redis.MessagePublisher;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.notification.Notification;
import org.wise.vle.domain.work.Event;

@Secured("ROLE_TEACHER")
@Controller
public class TeacherPostDataController {

  @Autowired
  private VLEService vleService;

  @Autowired
  private MessagePublisher redisPublisher;

  @PostMapping("/api/teacher/data")
  public void postData(HttpServletResponse response,
      @RequestParam(value = "projectId", required = false) ProjectImpl project,
      @RequestParam(value = "runId", required = false) RunImpl run,
      @RequestParam(value = "annotations", required = false) String annotations,
      @RequestParam(value = "events", required = false) String events) {
    JSONObject result = new JSONObject();
    try {
      User signedInUser = ControllerUtil.getSignedInUser();
      User owner = null;
      Set<User> sharedOwners = null;
      if (run != null) {
        owner = run.getOwner();
        sharedOwners = run.getSharedowners();
      }
      if (project != null) {
        owner = project.getOwner();
        sharedOwners = project.getSharedowners();
      }
      /*
       * the signed in user is an owner of the project or run or we are saving a teacher event that
       * isn't associated with a project or run
       */
      if ((owner != null && owner.equals(signedInUser))
          || (sharedOwners != null && sharedOwners.contains(signedInUser))
          || (run == null && project == null && events != null)) {
        if (annotations != null) {
          JSONArray annotationsJSONArray = new JSONArray(annotations);
          if (annotationsJSONArray != null) {
            JSONArray annotationsResultJSONArray = new JSONArray();
            for (int a = 0; a < annotationsJSONArray.length(); a++) {
              try {
                JSONObject annotationJSONObject = annotationsJSONArray.getJSONObject(a);
                String requestToken = annotationJSONObject.getString("requestToken");

                Annotation annotation = vleService.saveAnnotation(
                    annotationJSONObject.isNull("id") ? null : annotationJSONObject.getInt("id"),
                    annotationJSONObject.isNull("runId") ? null
                      : annotationJSONObject.getInt("runId"),
                    annotationJSONObject.isNull("periodId") ? null
                      : annotationJSONObject.getInt("periodId"),
                    annotationJSONObject.isNull("fromWorkgroupId") ? null
                      : annotationJSONObject.getInt("fromWorkgroupId"),
                    annotationJSONObject.isNull("toWorkgroupId") ? null
                      : annotationJSONObject.getInt("toWorkgroupId"),
                    annotationJSONObject.isNull("nodeId") ? null
                      : annotationJSONObject.getString("nodeId"),
                    annotationJSONObject.isNull("componentId") ? null
                      : annotationJSONObject.getString("componentId"),
                    annotationJSONObject.isNull("studentWorkId") ? null
                      : annotationJSONObject.getInt("studentWorkId"),
                    annotationJSONObject.isNull("localNotebookItemId") ? null
                      : annotationJSONObject.getString("localNotebookItemId"),
                    annotationJSONObject.isNull("notebookItemId") ? null
                      : annotationJSONObject.getInt("notebookItemId"),
                    annotationJSONObject.isNull("type") ? null
                      : annotationJSONObject.getString("type"),
                    annotationJSONObject.isNull("data") ? null
                      : annotationJSONObject.getString("data"),
                    annotationJSONObject.isNull("clientSaveTime") ? null
                      : annotationJSONObject.getString("clientSaveTime"));

                // before returning saved Annotation, strip all fields except
                // id, responseToken, and serverSaveTime to minimize response
                // size
                JSONObject savedAnnotationJSONObject = new JSONObject();
                savedAnnotationJSONObject.put("id", annotation.getId());
                savedAnnotationJSONObject.put("requestToken", requestToken);
                savedAnnotationJSONObject.put("serverSaveTime",
                    annotation.getServerSaveTime().getTime());
                annotationsResultJSONArray.put(savedAnnotationJSONObject);
                this.sendAnnotationNotificationToStudent(annotation);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
            result.put("annotations", annotationsResultJSONArray);
          }
        } else if (events != null) {
          JSONArray eventsJSONArray = new JSONArray(events);
          if (eventsJSONArray != null) {
            JSONArray eventsResultJSONArray = new JSONArray();
            for (int e = 0; e < eventsJSONArray.length(); e++) {
              try {
                JSONObject eventJSONObject = eventsJSONArray.getJSONObject(e);

                Event event = vleService.saveEvent(
                    eventJSONObject.isNull("id") ? null : eventJSONObject.getInt("id"),
                    eventJSONObject.isNull("runId") ? null : eventJSONObject.getInt("runId"),
                    eventJSONObject.isNull("periodId") ? null : eventJSONObject.getInt("periodId"),
                    eventJSONObject.isNull("workgroupId") ? null
                      : eventJSONObject.getInt("workgroupId"),
                    eventJSONObject.isNull("nodeId") ? null : eventJSONObject.getString("nodeId"),
                    eventJSONObject.isNull("componentId") ? null
                      : eventJSONObject.getString("componentId"),
                    eventJSONObject.isNull("componentType") ? null
                      : eventJSONObject.getString("componentType"),
                    eventJSONObject.isNull("context") ? null : eventJSONObject.getString("context"),
                    eventJSONObject.isNull("category") ? null
                      : eventJSONObject.getString("category"),
                    eventJSONObject.isNull("event") ? null : eventJSONObject.getString("event"),
                    eventJSONObject.isNull("data") ? null : eventJSONObject.getString("data"),
                    eventJSONObject.isNull("clientSaveTime") ? null
                      : eventJSONObject.getString("clientSaveTime"),
                    eventJSONObject.isNull("projectId") ? null
                      : eventJSONObject.getInt("projectId"),
                    signedInUser.getId().intValue());

                // before returning saved Event, strip all fields except id,
                // responseToken, and serverSaveTime to minimize response size
                JSONObject savedEventJSONObject = new JSONObject();
                savedEventJSONObject.put("id", event.getId());
                savedEventJSONObject.put("serverSaveTime", event.getServerSaveTime().getTime());
                eventsResultJSONArray.put(savedEventJSONObject);
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
            result.put("events", eventsResultJSONArray);
          }
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    try {
      PrintWriter writer = response.getWriter();
      writer.write(result.toString());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void sendAnnotationNotificationToStudent(Annotation annotation) {
    try {
      Notification notification = this.createNotificationForAnnotation(annotation);
      Long toWorkgroupId = notification.getToWorkgroup().getId();
      broadcastAnnotationToStudent(toWorkgroupId, annotation);
      broadcastNotificationToStudent(toWorkgroupId, notification);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Saves and returns a notification for the specified annotation
   *
   * @param annotation  Annotation to create the notification for
   * @return Notification notification for the specified annotation
   */
  private Notification createNotificationForAnnotation(Annotation annotation) {
    Integer notificationId = null;
    Integer runId = annotation.getRun().getId().intValue();
    Integer periodId = annotation.getPeriod().getId().intValue();
    Integer fromWorkgroupId = annotation.getFromWorkgroup().getId().intValue();
    Integer toWorkgroupId = annotation.getToWorkgroup().getId().intValue();
    String groupId = null;
    String nodeId = annotation.getNodeId();
    String componentId = annotation.getComponentId();
    String componentType = null;
    String type = "teacherToStudent";
    String message = "You have new feedback from your teacher!";
    String data = null;
    try {
      JSONObject dataJSONObject = new JSONObject();
      dataJSONObject.put("annotationId", annotation.getId());
      data = dataJSONObject.toString();
    } catch (JSONException je) {

    }
    Calendar now = Calendar.getInstance();
    String timeGenerated = String.valueOf(now.getTimeInMillis());
    String timeDismissed = null;

    Notification notification = vleService.saveNotification(notificationId, runId, periodId,
        fromWorkgroupId, toWorkgroupId, groupId, nodeId, componentId, componentType, type, message,
        data, timeGenerated, timeDismissed);
    return notification;
  }

  public void broadcastAnnotationToStudent(Long toWorkgroupId, Annotation annotation)
      throws JSONException {
    annotation.convertToClientAnnotation();
    JSONObject message = new JSONObject();
    message.put("type", "annotationToStudent");
    message.put("topic", String.format("/topic/workgroup/%s", toWorkgroupId));
    message.put("annotation", annotation.toJSON());
    redisPublisher.publish(message.toString());
  }

  public void broadcastNotificationToStudent(Long toWorkgroupId, Notification notification)
      throws JSONException {
    notification.convertToClientNotification();
    JSONObject message = new JSONObject();
    message.put("type", "notification");
    message.put("topic", String.format("/topic/workgroup/%s", toWorkgroupId));
    message.put("notification", notification.toJSON());
    redisPublisher.publish(message.toString());
  }
}
