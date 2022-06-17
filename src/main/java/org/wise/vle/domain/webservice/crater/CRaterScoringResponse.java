package org.wise.vle.domain.webservice.crater;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lombok.Getter;

public class CRaterScoringResponse {

  @Getter
  String response;

  public CRaterScoringResponse(String response) {
    this.response = response;
  }

  public boolean isSingleScore() {
    Node response = getResponseNode();
    String score = response.getAttributes().getNamedItem("score").getNodeValue();
    return score != null && !score.isEmpty();
  }

  /**
   * @param cRaterResponseXML Response XML from CRater with one score.
   *   Example:
   *     <crater-results>
   *       <tracking id="1013701"/>
   *       <client id="WISETEST"/>
   *         <items>
   *           <item id="Photo_Sun">
   *             <responses>
   *               <response id="testID" score="4" concepts="1,2,3,4,5"/>
   *             </responses>
   *           </item>
   *         </items>
   *     </crater-results>
   * @return An integer indicating score. In the example above, this method will return 4.
   */
  public int getScore() {
    Node response = getResponseNode();
    String score = response.getAttributes().getNamedItem("score").getNodeValue();
    return Integer.valueOf(score);
  }

  /**
   * @param cRaterResponseXML Response XML from CRater with multiple scores.
   *   Example:
   *     <crater-results>
   *       <tracking id="1367459" />
   *       <client id="WISETEST" />
   *       <items>
   *         <item id="STRIDES_EX1">
   *           <responses>
   *             <response id="1547591618656" score="" realNumberScore="" confidenceMeasure="0.99">
   *               <scores>
   *                 <score id="science" score="0" realNumberScore="0.2919" />
   *                 <score id="engineering" score="0" realNumberScore="0.2075" />
   *                 <score id="ki" score="0" realNumberScore="0.2075" />
   *               </scores>
   *               <advisorylist>
   *                 <advisorycode>0</advisorycode>
   *               </advisorylist>
   *             </response>
   *           </responses>
   *         </item>
   *       </items>
   *     </crater-results>
   * @return A List of CRaterSubScore objects containing scores.
   */
  public List<CRaterSubScore> getScores() {
    List<CRaterSubScore> scores = new ArrayList<CRaterSubScore>();
    NodeList scoreNodes = getScoreNodes();
    for (int i = 0; i < scoreNodes.getLength(); i++) {
      scores.add(new CRaterSubScore(scoreNodes.item(i)));
    }
    return scores;
  }

 /**
   * @return A List of CRaterIdea objects in the response
   */
  public List<CRaterIdea> getIdeas() {
    List<CRaterIdea> ideas = new ArrayList<CRaterIdea>();
    NodeList ideaNodes = getIdeaNodes();
    for (int i = 0; i < ideaNodes.getLength(); i++) {
      ideas.add(new CRaterIdea(ideaNodes.item(i)));
    }
    return ideas;
  }

  private NodeList getScoreNodes() {
    return getXMLDocument().getElementsByTagName("score");
  }

  private NodeList getIdeaNodes() {
    return getXMLDocument().getElementsByTagName("idea");
  }

  private Node getResponseNode() {
    NodeList responseList = getXMLDocument().getElementsByTagName("response");
    return responseList.item(0);
  }

  private Document getXMLDocument() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      return db.parse(new ByteArrayInputStream(this.response.getBytes()));
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
