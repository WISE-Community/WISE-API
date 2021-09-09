package org.wise.vle.domain.webservice.crater;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class CRaterScoringResponseTest {

  String singleScoreXml = "<crater-results><tracking id=\"1013701\"/><client id=\"WISETEST\"/>" +
      "<items><item id=\"Photo_Sun\">" +
      "<responses><response id=\"testID\" score=\"4\" concepts=\"1,2,3,4,5\"/></responses>" +
      "</item></items></crater-results>";

  CRaterScoringResponse singleScoreResponse = new CRaterScoringResponse(singleScoreXml);

  String subScoresXml = "<crater-results><tracking id=\"1367459\" /><client id=\"WISETEST\" />" +
      "<items><item id=\"STRIDES_EX1\"><responses>" +
      "<response id=\"1547591618656\" score=\"\" realNumberScore=\"\" confidenceMeasure=\"0.99\">" +
      "<scores><score id=\"science\" score=\"1\" realNumberScore=\"0.2919\" />" +
      "<score id=\"engineering\" score=\"2\" realNumberScore=\"0.2075\" />" +
      "<score id=\"ki\" score=\"4\" realNumberScore=\"0.2075\" /></scores>" +
      "<advisorylist><advisorycode>0</advisorycode></advisorylist>" +
      "</response></responses></item></items></crater-results>";

  CRaterScoringResponse subScoresResponse = new CRaterScoringResponse(subScoresXml);

  @Test
  public void isSingleScore() {
    assertTrue(singleScoreResponse.isSingleScore());
    assertFalse(subScoresResponse.isSingleScore());
  }

  @Test
  public void getScore_ReturnScore() {
    assertEquals(4, singleScoreResponse.getScore());
  }

  @Test
  public void getScores_ReturnScores() {
    assertEquals(3, subScoresResponse.getScores().size());
    assertEquals(1, subScoresResponse.getScores().get(0).getScore());
    assertEquals(2, subScoresResponse.getScores().get(1).getScore());
    assertEquals(4, subScoresResponse.getScores().get(2).getScore());
  }
}
