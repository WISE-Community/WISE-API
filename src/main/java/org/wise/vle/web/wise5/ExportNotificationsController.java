package org.wise.vle.web.wise5;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.vle.wise5.VLEService;

@Secured("ROLE_TEACHER")
@Controller
@RequestMapping("/api/teacher/export/{runId}/notifications")
public class ExportNotificationsController extends ExportController {

  @Autowired
  private VLEService vleService;

  @GetMapping
  public void export(@PathVariable("runId") RunImpl run, HttpServletResponse response)
      throws IOException {
    if (canExport(run)) {
      JSONArray resultArray = vleService.getNotificationsExport(run.getId().intValue());
      PrintWriter writer = response.getWriter();
      writer.write(resultArray.toString());
      writer.close();
    } else {
      sendUnauthorizedError(response);
    }
  }
}
