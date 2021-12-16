package org.wise.vle.web.wise5;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.vle.domain.work.Event;

@Secured("ROLE_TEACHER")
@Controller
@RequestMapping("/api/teacher/export/events")
public class ExportEventsController {

  @Autowired
  private RunService runService;

  @Autowired
  private VLEService vleService;

  @GetMapping
  public void export(HttpServletResponse response, Authentication auth,
      @RequestParam(value = "runId") RunImpl run,
      @RequestParam(value = "includeStudentEvents") boolean includeStudentEvents,
      @RequestParam(value = "includeTeacherEvents") boolean includeTeacherEvents)
      throws JSONException, ObjectNotFoundException, IOException {
    JSONObject result = new JSONObject();
    if (runService.hasReadPermission(auth, run)) {
      List<Event> events = getEvents(run, includeStudentEvents, includeTeacherEvents);
      JSONArray eventsJSONArray = convertEventsToJSON(events);
      result.put("events", eventsJSONArray);
    }
    PrintWriter writer = response.getWriter();
    writer.write(result.toString());
    writer.close();
  }

  private List<Event> getEvents(Run run, boolean includeStudentEvents, boolean includeTeacherEvents) {
    List<Event> events = vleService.getAllEvents(run);
    if (includeStudentEvents && includeTeacherEvents) {
      events = vleService.getAllEvents(run);
    } else if (includeStudentEvents) {
      events = vleService.getStudentEvents(run);
    } else if (includeTeacherEvents) {
      events = vleService.getTeacherEvents(run);
    }
    return events;
  }

  private JSONArray convertEventsToJSON(List<Event> events) {
    JSONArray eventsJSONArray = new JSONArray();
    for (int i = 0; i < events.size(); i++) {
      Event eventObject = events.get(i);
      eventsJSONArray.put(eventObject.toJSON());
    }
    return eventsJSONArray;
  }
}
