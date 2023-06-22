package org.wise.portal.presentation.web.controllers.archive;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.presentation.web.response.ResponseEntityGenerator;
import org.wise.portal.service.tags.TagsService;

@RestController
@RequestMapping(value = "/api")
public class ArchiveController {

  @Autowired
  private TagsService tagsService;

  private static final String archivedTag = "archived";

  @Secured({ "ROLE_TEACHER" })
  @PostMapping("/archive/run")
  protected ResponseEntity<Map<String, Object>> archiveProject(@RequestParam("runId") RunImpl run) {
    User user = ControllerUtil.getSignedInUser();
    tagsService.addTag(user, run, archivedTag);
    return ResponseEntityGenerator.createSuccess("projectArchived");
  }

  @Secured({ "ROLE_TEACHER" })
  @PostMapping("/archive/run/many")
  protected ResponseEntity<Map<String, Object>> archiveProjects(
      @RequestParam("runIds") List<RunImpl> runs) {
    User user = ControllerUtil.getSignedInUser();
    for (Run run : runs) {
      tagsService.addTag(user, run, archivedTag);
    }
    return ResponseEntityGenerator.createSuccess("projectsArchived");
  }

  @Secured({ "ROLE_TEACHER" })
  @PostMapping("/unarchive/run")
  protected ResponseEntity<Map<String, Object>> unarchiveProject(
      @RequestParam("runId") RunImpl run) {
    User user = ControllerUtil.getSignedInUser();
    tagsService.removeTag(user, run, archivedTag);
    return ResponseEntityGenerator.createSuccess("projectUnarchived");
  }

  @Secured({ "ROLE_TEACHER" })
  @PostMapping("/unarchive/run/many")
  protected ResponseEntity<Map<String, Object>> unarchiveProjects(
      @RequestParam("runIds") List<RunImpl> runs) {
    User user = ControllerUtil.getSignedInUser();
    for (Run run : runs) {
      tagsService.removeTag(user, run, archivedTag);
    }
    return ResponseEntityGenerator.createSuccess("projectsUnarchived");
  }

}
