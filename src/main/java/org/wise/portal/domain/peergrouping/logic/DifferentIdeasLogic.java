package org.wise.portal.domain.peergrouping.logic;

public class DifferentIdeasLogic extends AbstractPairingLogic {

  public static String regex = "differentIdeas\\(\"(\\w+)\",\\s*\"(\\w+)?\"(,\\s*\")?(\\w+)?(\")?\\)";

  public DifferentIdeasLogic(String logic) {
    super(logic);
  }

  public String getRegex() {
    return DifferentIdeasLogic.regex;
  }
}
