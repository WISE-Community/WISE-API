package org.wise.vle.web;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.HashMap;
import java.util.List;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wise.vle.domain.webservice.crater.CRaterHttpClient;
import org.wise.vle.domain.webservice.crater.CRaterIdea;
import org.wise.vle.domain.webservice.crater.CRaterScoringRequest;
import org.wise.vle.domain.webservice.crater.CRaterScoringResponse;
import org.wise.vle.domain.webservice.crater.CRaterSubScore;
import org.wise.vle.domain.webservice.crater.CRaterVerificationRequest;
import org.wise.vle.domain.webservice.crater.CRaterVerificationResponse;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest(CRaterHttpClient.class)
public class CRaterControllerTest {

  @TestSubject
  private CRaterController controller = new CRaterController();

  @Before
  public void setUp() {
    PowerMock.mockStatic(CRaterHttpClient.class);
  }

  @Test
  public void verifyItemId_ValidId_ReturnTrue() {
    CRaterVerificationResponse verifiedResponse = new CRaterVerificationResponse(
        "<crater-verification>" +
        "<tracking id=\"1767877\" /><client id=\"WISETEST2\" verified=\"Y\" />" +
        "<items><item id=\"VALID_ID\" avail=\"Y\" ></item></items></crater-verification>");
    expect(CRaterHttpClient.getVerificationResponse(isA(CRaterVerificationRequest.class)))
        .andReturn(verifiedResponse);
    replayAll();
    assertTrue(controller.verifyItemId(new CRaterVerificationRequest()));
    verifyAll();
  }

  @Test
  public void verifyItemId_InvalidId_ReturnFalse() {
    CRaterVerificationResponse unverifiedResponse = new CRaterVerificationResponse(
        "<crater-verification>" +
        "<tracking id=\"1767877\" /><client id=\"WISETEST2\" verified=\"N\" />" +
        "<items><item id=\"INVALID_ID\" avail=\"N\" ></item></items></crater-verification>");
    expect(CRaterHttpClient.getVerificationResponse(isA(CRaterVerificationRequest.class)))
        .andReturn(unverifiedResponse);
    replayAll();
    assertFalse(controller.verifyItemId(new CRaterVerificationRequest()));
    verifyAll();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void scoreItem_SingleScoreItem_ReturnScoreAndIdeas() {
    CRaterScoringRequest request = new CRaterScoringRequest();
    String cRaterXmlResponse = "<crater-results><tracking id=\"1767940\" />" +
        "<client id=\"WISETEST2\"/><items><item id=\"GREENROOF-II\" ><responses>" +
        "<response id=\"12345\" score=\"1\" realNumberScore=\"1.1138\" confidenceMeasure=\"0.99\" >" +
        "<advisorylist><advisorycode>0</advisorycode></advisorylist><feedback><ideas>" +
        "<idea name=\"idea1\" detected=\"1\" character_offsets=\"[]\" />" +
        "</ideas></feedback></response>" +
        "</responses></item></items></crater-results>";
    CRaterScoringResponse cRaterResponse = new CRaterScoringResponse(cRaterXmlResponse);
    expect(CRaterHttpClient.getScoringResponse(request)).andReturn(cRaterResponse);
    replayAll();
    HashMap<String, Object> scoreItemResponse = controller.scoreItem(request);
    assertFalse(scoreItemResponse.containsKey("scores"));
    assertTrue(scoreItemResponse.containsKey("score"));
    assertEquals(1, scoreItemResponse.get("score"));
    assertEquals(cRaterXmlResponse, scoreItemResponse.get("cRaterResponse"));
    assertTrue(scoreItemResponse.containsKey("ideas"));
    List<CRaterIdea> ideas = (List<CRaterIdea>) scoreItemResponse.get("ideas");
    assertEquals(1, ideas.size());
    assertTrue(ideas.get(0).isDetected());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void scoreItem_MultipleScoresItem_ReturnScoresAndIdeas() {
    CRaterScoringRequest request = new CRaterScoringRequest();
    String cRaterXmlResponse = "<crater-results>" +
        "<items><item id=\"ColdBeverage1Sub\" ><responses>" +
        "<response id=\"12345\" score=\"\" realNumberScore=\"\" confidenceMeasure=\"0.99\" >" +
        "<scores><score id=\"experimentation\" score=\"1\" realNumberScore=\"1.0302\" />" +
        "<score id=\"science\" score=\"1\" realNumberScore=\"1.0487\" score_range_min=\"0\" />" +
        "<score id=\"ki\" score=\"2\" realNumberScore=\"1.5486\" /></scores>" +
        "<feedback><ideas>" +
        "<idea name=\"idea1\" detected=\"1\" character_offsets=\"[]\" />" +
        "<idea name=\"idea2\" detected=\"0\" character_offsets=\"[]\" />" +
        "</ideas></feedback>" +
        "</response></responses>" +
        "</item></items></crater-results>";
    CRaterScoringResponse cRaterResponse = new CRaterScoringResponse(cRaterXmlResponse);
    expect(CRaterHttpClient.getScoringResponse(request)).andReturn(cRaterResponse);
    replayAll();
    HashMap<String, Object> scoreItemResponse = controller.scoreItem(request);
    assertFalse(scoreItemResponse.containsKey("score"));
    assertTrue(scoreItemResponse.containsKey("scores"));
    List<CRaterSubScore> subScores = (List<CRaterSubScore>) scoreItemResponse.get("scores");
    assertEquals(3, subScores.size());
    assertEquals(1, subScores.get(0).getScore());
    assertEquals(1, subScores.get(1).getScore());
    assertEquals(2, subScores.get(2).getScore());
    assertNull(subScores.get(0).getScoreRangeMin());
    assertNull(subScores.get(0).getScoreRangeMax());
    assertEquals(0, subScores.get(1).getScoreRangeMin());
    assertEquals(cRaterXmlResponse, scoreItemResponse.get("cRaterResponse"));
    assertTrue(scoreItemResponse.containsKey("ideas"));
    List<CRaterIdea> ideas = (List<CRaterIdea>) scoreItemResponse.get("ideas");
    assertEquals(2, ideas.size());
    assertTrue(ideas.get(0).isDetected());
    assertFalse(ideas.get(1).isDetected());
  }
}
