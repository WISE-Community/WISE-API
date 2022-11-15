package org.wise.portal.domain.peergrouping.logic;

public class DifferentKIScoresLogic extends AbstractPairingLogic {

  private Enum<DifferentKIScoresLogicMode> mode;
  public static String regex = "differentKIScores\\(\"(\\w+)\",\\s*\"(\\w+)?\"(,\\s*\")?(\\w+)?(\")?\\)";

  public DifferentKIScoresLogic(String logic) {
    super(logic);
    String mode = matcher.group(4);
    this.mode = mode == null ? DifferentKIScoresLogicMode.ANY
      : DifferentKIScoresLogicMode.valueOf(mode.toUpperCase());
  }

  public String getRegex() {
    return DifferentKIScoresLogic.regex;
  }

  public Enum<DifferentKIScoresLogicMode> getMode() {
    return mode;
  }
}
