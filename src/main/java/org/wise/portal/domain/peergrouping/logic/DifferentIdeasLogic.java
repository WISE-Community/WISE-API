package org.wise.portal.domain.peergrouping.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DifferentIdeasLogic {

  private String componentId;
  private String nodeId;

  public DifferentIdeasLogic(String logic) {
    Pattern pattern = Pattern.compile("\"(\\w+)\"");
    Matcher matcher = pattern.matcher(logic);
    matcher.find();
    this.nodeId = matcher.group(1);
    matcher.find();
    this.componentId = matcher.group(1);
  }

  public String getComponentId() {
    return componentId;
  }

  public String getNodeId() {
    return nodeId;
  }
}
