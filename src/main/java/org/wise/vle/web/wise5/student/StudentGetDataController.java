package org.wise.vle.web.wise5.student;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.portal.service.workgroup.WorkgroupService;

@RestController
@Secured("ROLE_STUDENT")
public class StudentGetDataController {

  @Autowired
  private RunService runService;

  @Autowired
  private UserService userService;

  @Autowired
  private VLEService vleService;

  @Autowired
  private WorkgroupService workgroupService;

  @GetMapping("/api/student/data")
  public HashMap<String, Object> getStudentData(Authentication authentication,
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
      @RequestParam(value = "onlyGetLatest", required = false) Boolean onlyGetLatest)
      throws ObjectNotFoundException {
    HashMap<String, Object> data = new HashMap<String, Object>();
    User user = userService.retrieveUser((StudentUserDetails) authentication.getPrincipal());
    Run run = runService.retrieveById(Long.valueOf(runId));
    if (getStudentWork && isMemberOfWorkgroupId(user, run, workgroupId)) {
      data.put("studentWorkList", vleService.getStudentWorkList(id, runId, periodId, workgroupId,
          isAutoSave, isSubmit, nodeId, componentId, componentType, components, onlyGetLatest));
    }
    if (getEvents && isMemberOfWorkgroupId(user, run, workgroupId)) {
      data.put("events", vleService.getEvents(id, runId, periodId, workgroupId, nodeId, componentId,
          componentType, context, category, event, components));
    }
    if (getAnnotations && isAllowedToGetAnnotations(user, run, fromWorkgroupId, toWorkgroupId)) {
      data.put("annotations",
          vleService.getAnnotations(id, runId, periodId, fromWorkgroupId, toWorkgroupId, nodeId,
              componentId, studentWorkId, localNotebookItemId, notebookItemId, annotationType));
    }
    return data;
  }

  private boolean isMemberOfWorkgroupId(User user, Run run, Integer workgroupId)
      throws ObjectNotFoundException {
    return workgroupId != null && workgroupService.isUserInWorkgroupForRun(user, run,
        workgroupService.retrieveById(Long.valueOf(workgroupId)));
  }

  private boolean isAllowedToGetAnnotations(User user, Run run, Integer fromWorkgroupId,
      Integer toWorkgroupId) throws ObjectNotFoundException {
    return isMemberOfWorkgroupId(user, run, fromWorkgroupId)
        || isMemberOfWorkgroupId(user, run, toWorkgroupId);
  }
}
