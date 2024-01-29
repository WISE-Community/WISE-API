package org.wise.portal.presentation.web.controllers.author.project;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.project.translation.TranslateProjectService;
import org.wise.portal.service.user.UserService;

import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@RequestMapping("/api/author/project/translate")
@Secured({ "ROLE_AUTHOR" })
public class TranslateProjectAPIController {

  @Autowired
  protected ProjectService projectService;

  @Autowired
  protected UserService userService;

  @Autowired
  protected TranslateProjectService translateProjectService;

  @PostMapping("{projectId}/{locale}")
  @ResponseBody
  protected void saveTranslations(Authentication auth,
      @PathVariable("projectId") ProjectImpl project, @PathVariable("locale") String locale,
      @RequestBody ObjectNode translations) throws IOException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (projectService.canAuthorProject(project, user)) {
      translateProjectService.saveTranslations(project, locale, translations.toPrettyString());
    }
  }
}
