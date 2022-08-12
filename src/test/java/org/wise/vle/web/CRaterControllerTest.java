package org.wise.vle.web;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.vle.domain.webservice.crater.CRaterScoringRequest;
import org.wise.vle.domain.webservice.crater.CRaterService;
import org.wise.vle.domain.webservice.crater.CRaterVerificationRequest;

@RunWith(EasyMockRunner.class)
public class CRaterControllerTest {

  @TestSubject
  private CRaterController controller = new CRaterController();

  @Mock
  private CRaterService cRaterService;

  private String clientId = "wise-test";
  private String itemId = "test-item-id";
  private Long trackingId = 123456789L;

  @Test
  public void verifyItemId_ShouldReturnString() {
    CRaterVerificationRequest request = new CRaterVerificationRequest();
    request.setItemId(itemId);
    try {
      expect(cRaterService.getVerificationResponse(request))
          .andReturn(createVerificationResponseString(itemId, true, trackingId, clientId));
      replay(cRaterService);
      String response = controller.verifyItemId(request);
      assertNotNull(response);
      verify(cRaterService);
    } catch (JSONException exception) {

    }
  }

  private String createVerificationResponseString(String itemId, Boolean available, Long trackingId,
      String clientId) {
    StringBuffer responseBuffer = new StringBuffer();
    responseBuffer.append("{");
    responseBuffer.append("  \"item_id\": \"" + itemId + "\",");
    responseBuffer.append("  \"available\": \"" + available + "\",");
    responseBuffer.append("  \"tracking_id\": " + trackingId + ",");
    responseBuffer.append("  \"client_id\": \"" + clientId + "\"");
    responseBuffer.append("}");
    return responseBuffer.toString();
  }

  @Test
  public void scoreItemId_ShouldReturnString() {
    CRaterScoringRequest request = new CRaterScoringRequest();
    request.setItemId(itemId);
    try {
      expect(cRaterService.getScoringResponse(request))
          .andReturn(createScoringResponseString(itemId, trackingId, clientId));
      replay(cRaterService);
      String response = controller.scoreItem(request);
      assertNotNull(response);
      verify(cRaterService);
    } catch (JSONException exception) {

    }
  }

  private String createScoringResponseString(String itemId, Long trackingId, String clientId) {
    StringBuffer responseBuffer = new StringBuffer();
    responseBuffer.append("{");
    responseBuffer.append("  \"item_id\": \"" + itemId + "\",");
    responseBuffer.append("  \"responses\": {},");
    responseBuffer.append("  \"tracking_id\": " + trackingId + ",");
    responseBuffer.append("  \"client_id\": \"" + clientId + "\"");
    responseBuffer.append("}");
    return responseBuffer.toString();
  }
}
