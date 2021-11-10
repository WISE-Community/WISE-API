package org.wise.portal.dao;

import org.wise.portal.domain.run.Run;

public class Component {
  public Run run;
  public String nodeId;
  public String componentId;

  public Component(Run run, String nodeId, String componentId) {
    this.run = run;
    this.nodeId = nodeId;
    this.componentId = componentId;
  }
}