package org.wise.vle.domain.webservice.crater;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import lombok.Getter;

@Getter
public class CRaterSubScore {
  String id;
  int score;
  float realNumberScore;

  public CRaterSubScore(Node scoreNode) {
    NamedNodeMap attributes = scoreNode.getAttributes();
    this.id = attributes.getNamedItem("id").getNodeValue();
    this.score = Integer.parseInt(attributes.getNamedItem("score").getNodeValue());
    this.realNumberScore =
        Float.parseFloat(attributes.getNamedItem("realNumberScore").getNodeValue());
  }
}
