package org.wise.vle.domain.webservice.crater;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Node;

public class CRaterResponseSubItemTest {

  protected Node getNode(String xmlString) throws Exception {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
      new ByteArrayInputStream(xmlString.getBytes())).getDocumentElement();
  }
}
