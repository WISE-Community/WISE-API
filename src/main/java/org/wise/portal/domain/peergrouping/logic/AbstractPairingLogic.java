package org.wise.portal.domain.peergrouping.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPairingLogic implements PairingLogic {
  String componentId;
  Matcher matcher;
  Enum<LogicMode> mode;
  String nodeId;

  public AbstractPairingLogic(String logic) {
    Pattern pattern = Pattern.compile(getRegex());
    matcher = pattern.matcher(logic);
    matcher.find();
    this.nodeId = matcher.group(1);
    this.componentId = matcher.group(2);
    String mode = matcher.group(4);
    this.mode = mode == null ? LogicMode.ANY : LogicMode.valueOf(mode.toUpperCase());
  }

  public String getComponentId() {
    return componentId;
  }

  public Enum<LogicMode> getMode() {
    return mode;
  }

  public String getNodeId() {
    return nodeId;
  }

  public abstract String getRegex();
}
