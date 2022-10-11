package org.wise.portal.domain.peergrouping.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DifferentIdeasLogic {

  public static String regex = "differentIdeas\\(\"(\\w+)\",\\s*\"(\\w+)\"\\)";
  private String componentId;
  private String nodeId;

  public DifferentIdeasLogic(String logic) {
    Pattern pattern = Pattern.compile(DifferentIdeasLogic.regex);
    Matcher matcher = pattern.matcher(logic);
    matcher.find();
    this.nodeId = matcher.group(1);
    this.componentId = matcher.group(2);
  }

  public String getComponentId() {
    return componentId;
  }

  public String getNodeId() {
    return nodeId;
  }
}
