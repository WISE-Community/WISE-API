package org.wise.portal.service.peergroup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PeerGroupNotFoundException extends Exception {
  
  private static final long serialVersionUID = 1L;

  public PeerGroupNotFoundException() {
    super("PeerGroupNotFound");
  }
}
