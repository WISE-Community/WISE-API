package org.wise.portal.domain.peergrouping.logic;

public class DifferentIdeasLogic extends AbstractPairingLogic {

  private Enum<DifferentIdeasLogicMode> mode;
  public static String regex = "differentIdeas\\(\"(\\w+)\",\\s*\"(\\w+)?\"(,\\s*\")?(\\w+)?(\")?\\)";

  public DifferentIdeasLogic(String logic) {
    super(logic);
    String mode = matcher.group(4);
    this.mode = mode == null ? DifferentIdeasLogicMode.ANY
      : DifferentIdeasLogicMode.valueOf(mode.toUpperCase());
  }

  public String getRegex() {
    return DifferentIdeasLogic.regex;
  }

  public Enum<DifferentIdeasLogicMode> getMode() {
    return mode;
  }
}
