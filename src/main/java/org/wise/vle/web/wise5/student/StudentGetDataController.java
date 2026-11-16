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
      @RequestParam(defaultValue = "false") boolean getStudentWork,
      @RequestParam(defaultValue = "false") boolean getEvents,
      @RequestParam(defaultValue = "false") boolean getAnnotations,
      @RequestParam(required = false) Integer id,
      @RequestParam(required = false) Integer runId,
      @RequestParam(required = false) Integer periodId,
      @RequestParam(required = false) Integer workgroupId,
      @RequestParam(required = false) Boolean isAutoSave,
      @RequestParam(required = false) Boolean isSubmit,
      @RequestParam(required = false) String nodeId,
      @RequestParam(required = false) String componentId,
      @RequestParam(required = false) String componentType,
      @RequestParam(required = false) String context,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String event,
      @RequestParam(required = false) Integer fromWorkgroupId,
      @RequestParam(required = false) Integer toWorkgroupId,
      @RequestParam(required = false) Integer studentWorkId,
      @RequestParam(required = false) String localNotebookItemId,
      @RequestParam(required = false) Integer notebookItemId,
      @RequestParam(required = false) String annotationType,
      @RequestParam(required = false) List<JSONObject> components,
      @RequestParam(required = false) Boolean onlyGetLatest)
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
