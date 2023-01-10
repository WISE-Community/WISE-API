package org.wise.portal.domain.peergrouping.logic;

public class DifferentKIScoresLogic extends AbstractPairingLogic {

  public static String regex = "differentKIScores\\(\"(\\w+)\",\\s*\"(\\w+)?\"(,\\s*\")?(\\w+)?(\")?\\)";

  public DifferentKIScoresLogic(String logic) {
    super(logic);
  }

  public String getRegex() {
    return DifferentKIScoresLogic.regex;
  }
}
