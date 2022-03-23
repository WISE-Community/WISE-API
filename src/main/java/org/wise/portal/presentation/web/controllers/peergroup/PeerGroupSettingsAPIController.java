package org.wise.portal.presentation.web.controllers.peergroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;

@RestController
@Secured("ROLE_TEACHER")
@RequestMapping("/api/run/{runId}/peer-group-settings")
public class PeerGroupSettingsAPIController {

  @Autowired
  private PeerGroupActivityService peerGroupActivityService;

  @PostMapping
  @PreAuthorize("hasPermission(#run, 'WRITE') or hasRole('ROLE_ADMINISTRATOR')")
  PeerGroupActivity create(@PathVariable("runId") RunImpl run,
      @RequestBody PeerGroupActivityImpl activity) {
    return peerGroupActivityService.createPeerGroupActivity(run, activity);
  }

  @PutMapping("/{tag}")
  @PreAuthorize("hasPermission(#run, 'WRITE') or hasRole('ROLE_ADMINISTRATOR')")
  PeerGroupActivity update(@PathVariable("runId") RunImpl run, @PathVariable("tag") String tag,
      @RequestBody PeerGroupActivityImpl activity) {
    return peerGroupActivityService.updatePeerGroupActivity(run, tag, activity);
  }
}
