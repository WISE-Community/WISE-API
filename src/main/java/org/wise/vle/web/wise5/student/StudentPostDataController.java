package org.wise.vle.web.wise5.student;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.portal.service.work.BroadcastStudentWorkService;
import org.wise.portal.service.workgroup.WorkgroupService;
import org.wise.portal.spring.data.redis.MessagePublisher;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.Event;
import org.wise.vle.domain.work.StudentWork;

import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@Secured("ROLE_STUDENT")
public class StudentPostDataController {

  @Autowired
  private ProjectService projectService;

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Autowired
  private VLEService vleService;

  @Autowired
  private WorkgroupService workgroupService;

  @Autowired
  private MessagePublisher redisPublisher;

  @Autowired
  private BroadcastStudentWorkService broadcastStudentWorkService;

  @PostMapping("/api/student/data")
  public void postStudentData(HttpServletResponse response, @RequestBody ObjectNode postedParams,
      Authentication auth) throws JSONException {
    User user = userService.retrieveUserByUsername(auth.getName());
    Integer runId = postedParams.get("runId").asInt();
    String studentWorkList = postedParams.get("studentWorkList").asText();
    String events = postedParams.get("events").asText();
    String annotations = postedParams.get("annotations").asText();
    JSONObject result = new JSONObject();
    try {
      Run run = runService.retrieveById(new Long(runId));
      if (run.isActive() && run.isStudentAssociatedToThisRun(user)) {
        List<Workgroup> workgroups = workgroupService.getWorkgroupListByRunAndUser(run, user);
        if (workgroups.size() == 0) {
          return;
        }
        Workgroup workgroup = workgroups.get(0);
        // maps nodeId_componentId to StudentWork.
        HashMap<String, StudentWork> savedStudentWorkList = new HashMap<>();
        // Used later for handling simultaneous POST of CRater annotation
        // handle POST'ed studentWork
        JSONArray studentWorkJSONArray = new JSONArray(studentWorkList);
        if (studentWorkJSONArray != null) {
          JSONArray studentWorkResultJSONArray = new JSONArray();
          for (int c = 0; c < studentWorkJSONArray.length(); c++) {
            try {
              JSONObject studentWorkJSONObject = studentWorkJSONArray.getJSONObject(c);
              if (!canSaveStudentWorkOrEvent(studentWorkJSONObject, workgroup)) {
                continue;
              }
              StudentWork studentWork = vleService.saveStudentWork(
                  studentWorkJSONObject.isNull("id") ? null : studentWorkJSONObject.getInt("id"),
                  studentWorkJSONObject.isNull("runId") ? null
                    : studentWorkJSONObject.getInt("runId"),
                  studentWorkJSONObject.isNull("periodId") ? null
                    : studentWorkJSONObject.getInt("periodId"),
                  studentWorkJSONObject.isNull("workgroupId") ? null
                    : studentWorkJSONObject.getInt("workgroupId"),
                  studentWorkJSONObject.isNull("peerGroupId") ? null
                    : studentWorkJSONObject.getLong("peerGroupId"),
                  studentWorkJSONObject.isNull("isAutoSave") ? null
                    : studentWorkJSONObject.getBoolean("isAutoSave"),
                  studentWorkJSONObject.isNull("isSubmit") ? null
                    : studentWorkJSONObject.getBoolean("isSubmit"),
                  studentWorkJSONObject.isNull("nodeId") ? null
                    : studentWorkJSONObject.getString("nodeId"),
                  studentWorkJSONObject.isNull("componentId") ? null
                    : studentWorkJSONObject.getString("componentId"),
                  studentWorkJSONObject.isNull("componentType") ? null
                    : studentWorkJSONObject.getString("componentType"),
                  studentWorkJSONObject.isNull("studentData") ? null
                    : studentWorkJSONObject.getString("studentData"),
                  studentWorkJSONObject.isNull("clientSaveTime") ? null
                    : studentWorkJSONObject.getString("clientSaveTime"));

              if (studentWork.getNodeId() != null && studentWork.getComponentId() != null) {
                // the student work was a component state, so save it for later when we might need
                // it to add annotations
                savedStudentWorkList
                    .put(studentWork.getNodeId() + "_" + studentWork.getComponentId(), studentWork);
              }

              // before returning saved StudentWork, strip all fields except id, responseToken, and
              // serverSaveTime to minimize response size
              JSONObject savedStudentWorkJSONObject = new JSONObject();
              savedStudentWorkJSONObject.put("id", studentWork.getId());
              savedStudentWorkJSONObject.put("requestToken",
                  studentWorkJSONObject.getString("requestToken"));
              savedStudentWorkJSONObject.put("serverSaveTime",
                  studentWork.getServerSaveTime().getTime());
              studentWorkResultJSONArray.put(savedStudentWorkJSONObject);

              studentWork.convertToClientStudentWork();
              broadcastStudentWorkService.broadcastToTeacher(studentWork);
              if (studentWork.getComponentType().equals("Discussion")) {
                broadcastStudentWorkService.broadcastToClassroom(studentWork);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          result.put("studentWorkList", studentWorkResultJSONArray);
        }
        JSONArray eventsJSONArray = new JSONArray(events);
        if (eventsJSONArray != null) {
          JSONArray eventsResultJSONArray = new JSONArray();
          for (int e = 0; e < eventsJSONArray.length(); e++) {
            try {
              JSONObject eventJSONObject = eventsJSONArray.getJSONObject(e);
              if (!canSaveStudentWorkOrEvent(eventJSONObject, workgroup)) {
                continue;
              }
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
                  eventJSONObject.isNull("category") ? null : eventJSONObject.getString("category"),
                  eventJSONObject.isNull("event") ? null : eventJSONObject.getString("event"),
                  eventJSONObject.isNull("data") ? null : eventJSONObject.getString("data"),
                  eventJSONObject.isNull("clientSaveTime") ? null
                    : eventJSONObject.getString("clientSaveTime"),
                  eventJSONObject.isNull("projectId") ? null : eventJSONObject.getInt("projectId"),
                  null);

              // before returning saved Event, strip all fields except id, responseToken, and
              // serverSaveTime to minimize response size
              JSONObject savedEventJSONObject = new JSONObject();
              savedEventJSONObject.put("id", event.getId());
              savedEventJSONObject.put("requestToken", eventJSONObject.getString("requestToken"));
              savedEventJSONObject.put("serverSaveTime", event.getServerSaveTime().getTime());
              eventsResultJSONArray.put(savedEventJSONObject);
            } catch (Exception exception) {
              exception.printStackTrace();
            }
          }
          result.put("events", eventsResultJSONArray);
        }
        JSONArray annotationsJSONArray = new JSONArray(annotations);
        if (annotationsJSONArray != null) {
          JSONArray annotationsResultJSONArray = new JSONArray();
          for (int a = 0; a < annotationsJSONArray.length(); a++) {
            try {
              JSONObject annotationJSONObject = annotationsJSONArray.getJSONObject(a);
              if (!canSaveAnnotation(annotationJSONObject, workgroup)) {
                continue;
              }
              Annotation annotation;
              // check to see if this Annotation was posted along with a StudentWork (e.g. CRater)
              if (annotationJSONObject.isNull("studentWorkId")
                  && !annotationJSONObject.isNull("nodeId")
                  && !annotationJSONObject.isNull("componentId")
                  && savedStudentWorkList.containsKey(annotationJSONObject.getString("nodeId") + "_"
                      + annotationJSONObject.getString("componentId"))) {
                // this is an annotation for a StudentWork that we just saved.
                String localNotebookItemId = null; // since this is an annotation on student work,
                                                   // notebook item should be null.
                Integer notebookItemId = null; // since this is an annotation on student work,
                                               // notebook item should be null.
                StudentWork savedStudentWork = savedStudentWorkList
                    .get(annotationJSONObject.getString("nodeId") + "_"
                        + annotationJSONObject.getString("componentId"));
                Integer savedStudentWorkId = savedStudentWork.getId();
                annotation = vleService.saveAnnotation(
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
                    savedStudentWorkId, localNotebookItemId, notebookItemId,
                    annotationJSONObject.isNull("type") ? null
                      : annotationJSONObject.getString("type"),
                    annotationJSONObject.isNull("data") ? null
                      : annotationJSONObject.getString("data"),
                    annotationJSONObject.isNull("clientSaveTime") ? null
                      : annotationJSONObject.getString("clientSaveTime"));

                // send this annotation immediately to the teacher so the Classroom Monitor can be
                // updated
                annotation.convertToClientAnnotation();
                broadcastAnnotationToTeacher(annotation);
              } else {
                annotation = vleService.saveAnnotation(
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
              }
              annotation.convertToClientAnnotation();
              broadcastAnnotationToTeacher(annotation);

              // before returning saved Annotation, strip all fields except id, responseToken, and
              // serverSaveTime to minimize response size
              JSONObject savedAnnotationJSONObject = new JSONObject();
              savedAnnotationJSONObject.put("id", annotation.getId());
              savedAnnotationJSONObject.put("requestToken",
                  annotationJSONObject.getString("requestToken"));
              savedAnnotationJSONObject.put("serverSaveTime",
                  annotation.getServerSaveTime().getTime());
              annotationsResultJSONArray.put(savedAnnotationJSONObject);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          result.put("annotations", annotationsResultJSONArray);
        }
      }
    } catch (ObjectNotFoundException e) {
      e.printStackTrace();
      return;
    }

    try {
      PrintWriter writer = response.getWriter();
      writer.write(result.toString());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean canSaveStudentWorkOrEvent(JSONObject jsonObject, Workgroup workgroup) {
    return isMatchingRunAndPeriod(jsonObject, workgroup)
        && isMatchingWorkgroup(jsonObject, workgroup);
  }

  private boolean isMatchingRunAndPeriod(JSONObject jsonObject, Workgroup workgroup) {
    try {
      return jsonObject.getInt("runId") == workgroup.getRun().getId()
          && jsonObject.getLong("periodId") == workgroup.getPeriod().getId();
    } catch (JSONException e) {
    }
    return false;
  }

  private boolean isMatchingWorkgroup(JSONObject jsonObject, Workgroup workgroup) {
    try {
      return jsonObject.getLong("workgroupId") == workgroup.getId();
    } catch (JSONException e) {
    }
    return false;
  }

  private boolean canSaveAnnotation(JSONObject annotation, Workgroup workgroup) {
    return isMatchingRunAndPeriod(annotation, workgroup)
        && (isValidAutoGradedAnnotation(annotation, workgroup)
            || isValidFromWorkgroupId(annotation, workgroup))
        && isToWorkgroupInSameRun(annotation, workgroup);
  }

  private boolean isValidAutoGradedAnnotation(JSONObject annotation, Workgroup workgroup) {
    try {
      if (annotation.get("type").equals("autoComment")
          || annotation.get("type").equals("autoScore")) {
        ProjectComponent component = projectService.getProjectComponent(
            workgroup.getRun().getProject(), annotation.getString("nodeId"),
            annotation.getString("componentId"));
        return component != null && component.getBoolean("enableCRater")
            && annotation.getLong("toWorkgroupId") == workgroup.getId();
      }
    } catch (JSONException | IOException e) {
    }
    return false;
  }

  private boolean isValidFromWorkgroupId(JSONObject annotation, Workgroup workgroup) {
    try {
      return annotation.getLong("fromWorkgroupId") == workgroup.getId();
    } catch (JSONException e) {
    }
    return false;
  }

  private boolean isToWorkgroupInSameRun(JSONObject annotation, Workgroup workgroup) {
    try {
      Workgroup toWorkgroup = workgroupService.retrieveById(annotation.getLong("toWorkgroupId"));
      return toWorkgroup.getRun().equals(workgroup.getRun());
    } catch (ObjectNotFoundException | JSONException e) {
    }
    return false;
  }

  private void broadcastAnnotationToTeacher(Annotation annotation) throws JSONException {
    JSONObject message = new JSONObject();
    message.put("type", "annotationToTeacher");
    message.put("topic", String.format("/topic/teacher/%s", annotation.getRunId()));
    message.put("annotation", annotation.toJSON());
    redisPublisher.publish(message.toString());
  }
}
