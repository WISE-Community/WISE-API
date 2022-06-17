package org.wise.vle.domain.webservice.crater;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.w3c.dom.Node;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CRaterSubScore {
  String id;
  float realNumberScore;
  int score;
  Integer scoreRangeMax;
  Integer scoreRangeMin;

  public CRaterSubScore(Node scoreNode) {
    this.id = getNodeValue(scoreNode, "id");
    this.score = Integer.parseInt(getNodeValue(scoreNode, "score"));
    this.realNumberScore = Float.parseFloat(getNodeValue(scoreNode, "realNumberScore"));
    if (scoreNode.getAttributes().getNamedItem("score_range_min") != null) {
      this.scoreRangeMin = Integer.parseInt(getNodeValue(scoreNode, "score_range_min"));
    }
    if (scoreNode.getAttributes().getNamedItem("score_range_max") != null) {
      this.scoreRangeMax = Integer.parseInt(getNodeValue(scoreNode, "score_range_max"));
    }
  }

  private String getNodeValue(Node node, String name) {
    return node.getAttributes().getNamedItem(name).getNodeValue();
  }
}
