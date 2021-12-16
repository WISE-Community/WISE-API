package org.wise.vle.web.wise5;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.portal.spring.data.redis.MessagePublisher;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.notification.Notification;
import org.wise.vle.domain.work.Event;
import org.wise.vle.domain.work.StudentWork;

/**
 * Controller for handling GET and POST of WISE5 Teacher related data like annotations.
 */
@Secured("ROLE_TEACHER")
@Controller("wise5TeacherDataController")
public class TeacherDataController {

  @Autowired
  private VLEService vleService;

  @Autowired
  private ProjectService projectService;

  @Autowired
  private RunService runService;

  @Autowired
  private MessagePublisher redisPublisher;

  @GetMapping("/api/teacher/data")
  @ResponseBody
  protected HashMap<String, Object> getWISE5TeacherData(
      @RequestParam(value = "getStudentWork", defaultValue = "false") boolean getStudentWork,
      @RequestParam(value = "getEvents", defaultValue = "false") boolean getEvents,
      @RequestParam(value = "getAnnotations", defaultValue = "false") boolean getAnnotations,
      @RequestParam(value = "id", required = false) Integer id,
      @RequestParam(value = "runId", required = false) Integer runId,
      @RequestParam(value = "periodId", required = false) Integer periodId,
      @RequestParam(value = "workgroupId", required = false) Integer workgroupId,
      @RequestParam(value = "isAutoSave", required = false) Boolean isAutoSave,
      @RequestParam(value = "isSubmit", required = false) Boolean isSubmit,
      @RequestParam(value = "nodeId", required = false) String nodeId,
      @RequestParam(value = "componentId", required = false) String componentId,
      @RequestParam(value = "componentType", required = false) String componentType,
      @RequestParam(value = "context", required = false) String context,
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "event", required = false) String event,
      @RequestParam(value = "fromWorkgroupId", required = false) Integer fromWorkgroupId,
      @RequestParam(value = "toWorkgroupId", required = false) Integer toWorkgroupId,
      @RequestParam(value = "studentWorkId", required = false) Integer studentWorkId,
      @RequestParam(value = "localNotebookItemId", required = false) String localNotebookItemId,
      @RequestParam(value = "notebookItemId", required = false) Integer notebookItemId,
      @RequestParam(value = "annotationType", required = false) String annotationType,
      @RequestParam(value = "components", required = false) List<JSONObject> components,
      @RequestParam(value = "onlyGetLatest", required = false) Boolean onlyGetLatest) {
    HashMap<String, Object> data = new HashMap<String, Object>();
    try {
      User signedInUser = ControllerUtil.getSignedInUser();
      Run run = runService.retrieveById(new Long(runId));
      User owner = run.getOwner();
      Set<User> sharedOwners = run.getSharedowners();
      if (owner.equals(signedInUser) || sharedOwners.contains(signedInUser)
          || signedInUser.isAdmin()) {
        if (getStudentWork) {
          List<StudentWork> studentWorkList = vleService.getStudentWorkList(id, runId, periodId,
              workgroupId, isAutoSave, isSubmit, nodeId, componentId, componentType, components,
              onlyGetLatest);
          data.put("studentWorkList", studentWorkList);
        }
        if (getEvents) {
          List<Event> events = vleService.getEvents(id, runId, periodId, workgroupId, nodeId,
              componentId, componentType, context, category, event, components);
          data.put("events", events);
        }
        if (getAnnotations) {
          List<Annotation> annotations = vleService.getAnnotations(id, runId, periodId,
              fromWorkgroupId, toWorkgroupId, nodeId, componentId, studentWorkId,
              localNotebookItemId, notebookItemId, annotationType);
          data.put("annotations", annotations);
        }
      }
    } catch (ObjectNotFoundException onfe) {
      onfe.printStackTrace();
    }
    return data;
  }

  @PostMapping("/api/teacher/data")
  public void postWISETeacherData(HttpServletResponse response,
      @RequestParam(value = "workgroupId", required = false) Integer workgroupId,
      @RequestParam(value = "projectId", required = false) Integer projectId,
      @RequestParam(value = "runId", required = false) Integer runId,
      @RequestParam(value = "annotations", required = false) String annotations,
      @RequestParam(value = "events", required = false) String events) {
    JSONObject result = new JSONObject();
    try {
      User signedInUser = ControllerUtil.getSignedInUser();
      Project project = null;
      Run run = null;
      User owner = null;
      Integer userId = null;
      Set<User> sharedOwners = null;
      if (runId != null) {
        run = runService.retrieveById(new Long(runId));
        if (run != null) {
          owner = run.getOwner();
          sharedOwners = run.getSharedowners();
        }
      }
      if (projectId != null) {
        project = projectService.getById(new Long(projectId));
        if (project != null) {
          owner = project.getOwner();
          sharedOwners = project.getSharedowners();
        }
      }
      if (signedInUser != null) {
        Long userIdLong = signedInUser.getId();
        if (userIdLong != null) {
          userId = userIdLong.intValue();
        }
      }
      /*
       * the signed in user is an owner of the project or run or we are saving a teacher event that
       * isn't associated with a project or run
       */
      if ((owner != null && owner.equals(signedInUser))
          || (sharedOwners != null && sharedOwners.contains(signedInUser))
          || (runId == null && projectId == null && events != null)) {
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
                    userId);

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
    } catch (ObjectNotFoundException e) {
      e.printStackTrace();
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
   * @param annotation
   *                     Annotation to create the notification for
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
