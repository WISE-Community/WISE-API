package org.wise.portal.domain.peergrouping.logic;

public class DifferentKIScoreLogic extends AbstractPairingLogic {

  private Enum<DifferentKIScoreLogicMode> mode;
  public static String regex = "differentKIScore\\(\"(\\w+)\",\\s*\"(\\w+)?\"(,\\s*\")?(\\w+)?(\")?\\)";

  public DifferentKIScoreLogic(String logic) {
    super(logic);
    String mode = matcher.group(4);
    this.mode = mode == null ? DifferentKIScoreLogicMode.ANY
      : DifferentKIScoreLogicMode.valueOf(mode.toUpperCase());
  }

  public String getRegex() {
    return DifferentKIScoreLogic.regex;
  }

  public Enum<DifferentKIScoreLogicMode> getMode() {
    return mode;
  }
}
