package org.wise.portal.service.teacherpresentation;

import java.util.List;

public record IdeasItem(String ideaId, List<IdeasResponse> responses) {
  public IdeasItem {
    if (responses == null) {
      responses = List.of();
    }
  }
}
