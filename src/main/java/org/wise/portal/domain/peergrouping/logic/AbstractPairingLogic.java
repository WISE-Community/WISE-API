package org.wise.portal.domain.peergrouping.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPairingLogic implements PairingLogic {
  String componentId;
  Matcher matcher;
  String nodeId;

  public AbstractPairingLogic(String logic) {
    Pattern pattern = Pattern.compile(getRegex());
    matcher = pattern.matcher(logic);
    matcher.find();
    this.nodeId = matcher.group(1);
    this.componentId = matcher.group(2);
  }

  public abstract String getRegex();

  public String getComponentId() {
    return componentId;
  }

  public String getNodeId() {
    return nodeId;
  }
}
