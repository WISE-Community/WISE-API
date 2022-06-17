package org.wise.vle.domain.webservice.crater;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import lombok.Getter;

@Getter
public class CRaterIdea {

  String name;
  boolean detected;
  List<Integer> characterOffsets;

  public CRaterIdea(Node ideaNode) {
    this.name = getNodeValue(ideaNode, "name");
    this.detected = getNodeValue(ideaNode, "detected").equals("1");
    this.characterOffsets = new ArrayList<Integer>();
  }

  private String getNodeValue(Node node, String name) {
    return node.getAttributes().getNamedItem(name).getNodeValue();
  }
}
