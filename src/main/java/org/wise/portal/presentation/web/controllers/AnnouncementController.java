package org.wise.portal.presentation.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.portal.Portal;
import org.wise.portal.service.portal.PortalService;

@Controller
public class AnnouncementController {

  @Autowired
  PortalService portalService;

  @ResponseBody
  @GetMapping("/api/announcement")
  protected String getAnnouncement() throws ObjectNotFoundException {
    Portal portal = portalService.getById(new Integer(1));
    return portal.getAnnouncement();
  }
}
