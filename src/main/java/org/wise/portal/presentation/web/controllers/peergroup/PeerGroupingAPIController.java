package org.wise.portal.presentation.web.controllers.peergroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.peergrouping.PeerGroupingService;

@RestController
@Secured("ROLE_TEACHER")
@RequestMapping("/api/run/{runId}/peer-grouping")
public class PeerGroupingAPIController {

  @Autowired
  private PeerGroupingService peerGroupingService;

  @PostMapping
  @PreAuthorize("hasPermission(#run, 'WRITE') or hasRole('ROLE_ADMINISTRATOR')")
  PeerGrouping create(@PathVariable("runId") RunImpl run,
      @RequestBody PeerGroupingImpl peerGrouping) {
    try {
      return peerGroupingService.createPeerGrouping(run, peerGrouping);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate Tag");
    }
  }

  @PutMapping("/{tag}")
  @PreAuthorize("hasPermission(#run, 'WRITE') or hasRole('ROLE_ADMINISTRATOR')")
  PeerGrouping update(@PathVariable("runId") RunImpl run, @PathVariable("tag") String tag,
      @RequestBody PeerGroupingImpl peerGrouping) {
    return peerGroupingService.updatePeerGrouping(run, tag, peerGrouping);
  }
}
