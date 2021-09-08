package org.wise.vle.web;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertFalse;
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
  public void scoreItem_SingleScoreItem_ReturnScore() {
    CRaterScoringRequest request = new CRaterScoringRequest();
    String cRaterXMLResponse = "<crater-results><tracking id=\"1767940\" />" +
        "<client id=\"WISETEST2\"/><items><item id=\"GREENROOF-II\" ><responses>" +
        "<response id=\"12345\" score=\"1\" realNumberScore=\"1.1138\" confidenceMeasure=\"0.99\" >" +
        "<advisorylist><advisorycode>0</advisorycode></advisorylist></response>" +
        "</responses></item></items></crater-results>";
    CRaterScoringResponse cRaterResponse = new CRaterScoringResponse(cRaterXMLResponse);
    expect(CRaterHttpClient.getScoringResponse(request)).andReturn(cRaterResponse);
    replayAll();
    HashMap<String, Object> scoreItemResponse = controller.scoreItem(request);
    assertFalse(scoreItemResponse.containsKey("scores"));
    assertTrue(scoreItemResponse.containsKey("score"));
    assertEquals(1, scoreItemResponse.get("score"));
    assertEquals(cRaterXMLResponse, scoreItemResponse.get("cRaterResponse"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void scoreItem_MultipleScoresItem_ReturnScores() {
    CRaterScoringRequest request = new CRaterScoringRequest();
    String cRaterXMLResponse = "<crater-results><tracking id=\"1767886\" />" +
        "<client id=\"WISETEST2\"/><items><item id=\"ColdBeverage1Sub\" ><responses>" +
        "<response id=\"12345\" score=\"\" realNumberScore=\"\" confidenceMeasure=\"0.99\" >" +
        "<scores><score id=\"experimentation\" score=\"1\" realNumberScore=\"1.0302\" />" +
        "<score id=\"science\" score=\"1\" realNumberScore=\"1.0487\" />" +
        "<score id=\"ki\" score=\"2\" realNumberScore=\"1.5486\" /></scores>" +
        "<advisorylist><advisorycode>0</advisorycode></advisorylist></response></responses>" +
        "</item></items></crater-results>";
    CRaterScoringResponse cRaterResponse = new CRaterScoringResponse(cRaterXMLResponse);
    expect(CRaterHttpClient.getScoringResponse(request)).andReturn(cRaterResponse);
    replayAll();
    HashMap<String, Object> scoreItemResponse = controller.scoreItem(request);
    assertFalse(scoreItemResponse.containsKey("score"));
    assertTrue(scoreItemResponse.containsKey("scores"));
    assertEquals(3, ((List<CRaterSubScore>) scoreItemResponse.get("scores")).size());
    assertEquals(1, ((List<CRaterSubScore>) scoreItemResponse.get("scores")).get(0).getScore());
    assertEquals(1, ((List<CRaterSubScore>) scoreItemResponse.get("scores")).get(1).getScore());
    assertEquals(2, ((List<CRaterSubScore>) scoreItemResponse.get("scores")).get(2).getScore());
    assertEquals(cRaterXMLResponse, scoreItemResponse.get("cRaterResponse"));
  }
}
