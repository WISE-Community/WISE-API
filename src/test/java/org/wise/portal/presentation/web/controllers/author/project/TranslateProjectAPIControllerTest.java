package org.wise.portal.presentation.web.controllers.author.project;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.presentation.web.controllers.APIControllerTest;
import org.wise.portal.service.project.translation.TranslateProjectService;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(EasyMockExtension.class)
public class TranslateProjectAPIControllerTest extends APIControllerTest {

  @TestSubject
  private TranslateProjectAPIController controller = new TranslateProjectAPIController();

  @Mock
  private TranslateProjectService translateProjectService;

  @Test
  public void saveTranslations_() throws Exception {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    translateProjectService.saveTranslations(project1, "es", "{}");
    expectLastCall();
    replay(projectService, translateProjectService, userService);
    controller.saveTranslations(teacherAuth, project1, "es",
        new ObjectNode(new JsonNodeFactory(false)));
    verify(projectService, translateProjectService, userService);
  }
}
