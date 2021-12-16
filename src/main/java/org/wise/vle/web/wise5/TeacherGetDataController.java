package org.wise.vle.web.wise5;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.service.vle.wise5.VLEService;

@Secured("ROLE_TEACHER")
@RestController
public class TeacherGetDataController {

  @Autowired
  private VLEService vleService;

  @GetMapping("/api/teacher/data")
  protected HashMap<String, Object> getData(
      @RequestParam("runId") RunImpl run,
      @RequestParam(value = "getStudentWork", defaultValue = "false") boolean getStudentWork,
      @RequestParam(value = "getEvents", defaultValue = "false") boolean getEvents,
      @RequestParam(value = "getAnnotations", defaultValue = "false") boolean getAnnotations,
      @RequestParam(value = "id", required = false) Integer id,
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
    if (canGetData(run)) {
      HashMap<String, Object> data = new HashMap<String, Object>();
      int runId = run.getId().intValue();
      if (getStudentWork) {
        data.put("studentWorkList", vleService.getStudentWorkList(id, runId, periodId, workgroupId,
            isAutoSave, isSubmit, nodeId, componentId, componentType, components, onlyGetLatest));
      }
      if (getEvents) {
        data.put("events", vleService.getEvents(id, runId, periodId, workgroupId, nodeId,
            componentId, componentType, context, category, event, components));
      }
      if (getAnnotations) {
        data.put("annotations", vleService.getAnnotations(id, runId, periodId, fromWorkgroupId,
            toWorkgroupId, nodeId, componentId, studentWorkId, localNotebookItemId, notebookItemId,
            annotationType));
      }
      return data;
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  private boolean canGetData(Run run) {
    User signedInUser = ControllerUtil.getSignedInUser();
    return run.getOwner().equals(signedInUser) || run.getSharedowners().contains(signedInUser)
        || signedInUser.isAdmin();
  }
}
