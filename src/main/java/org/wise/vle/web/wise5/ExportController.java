package org.wise.vle.web.wise5;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.controllers.ControllerUtil;

public class ExportController {

  protected boolean canExport(Run run) {
    User signedInUser = ControllerUtil.getSignedInUser();
    return run.getOwner().equals(signedInUser) || run.getSharedowners().contains(signedInUser)
        || signedInUser.isAdmin();
  }

  protected void sendUnauthorizedError(HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "You are not authorized to access this page");
  }
}
