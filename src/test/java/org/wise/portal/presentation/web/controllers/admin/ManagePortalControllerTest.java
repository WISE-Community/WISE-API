package org.wise.portal.presentation.web.controllers.admin;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.ui.ModelMap;
import org.wise.portal.service.portal.PortalService;
import org.wise.portal.service.project.ProjectService;

import java.io.IOException;

@ExtendWith(EasyMockExtension.class)
public class ManagePortalControllerTest {

  @TestSubject
  private ManagePortalController controller = new ManagePortalController();

  @Mock
  private PortalService portalService;

  @Mock
  private ProjectService projectService;

  @Test
  public void addOfficialTagToProjectLibraryGroup_OK() throws JSONException, IOException {
    String projectLibraryGroupJSONString = IOUtils
        .toString(this.getClass().getResourceAsStream("/projectLibraryGroupSample.json"), "UTF-8");
    EasyMock.expect(projectService.addTagToProject("official", Long.valueOf(24447))).andReturn(1);
    EasyMock.expect(projectService.addTagToProject("official", Long.valueOf(24449))).andReturn(1);
    EasyMock.expect(projectService.addTagToProject("official", Long.valueOf(24358))).andReturn(1);
    EasyMock.replay(projectService);
    controller.addOfficialTagToProjectLibraryGroup(projectLibraryGroupJSONString);
    EasyMock.verify(projectService);
  }

}
