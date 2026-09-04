package org.wise.vle.web;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.service.ping.CRaterPingService;
import org.wise.vle.domain.webservice.crater.CRaterPingRequest;
import org.wise.vle.domain.webservice.crater.CRaterScoringRequest;
import org.wise.vle.domain.webservice.crater.CRaterService;
import org.wise.vle.domain.webservice.crater.CRaterVerificationRequest;

@ExtendWith(EasyMockExtension.class)
public class CRaterControllerTest {

  @TestSubject
  private CRaterController controller = new CRaterController();

  @Mock
  private CRaterService cRaterService;

  @Mock
  private CRaterPingService cRaterPingService;

  private String clientId = "wise-test";
  private String itemId = "test-item-id";
  private String berkeleyItemId = "berkeley_test-item-id";
  private Long trackingId = 123456789L;

  @Test
  public void verifyItemId_ShouldReturnString() {
    CRaterVerificationRequest request = new CRaterVerificationRequest();
    request.setItemId(itemId);
    try {
      expect(cRaterService.getCRaterResponse(request))
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
      expect(cRaterService.getCRaterResponse(request))
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

  @Test
  public void pingItem_ItemAlreadyPinged_ShouldReturnEmpty() {
    CRaterPingRequest request = new CRaterPingRequest();
    request.setItemId(berkeleyItemId);
    try {
      expect(cRaterPingService.hasPingedItem(berkeleyItemId)).andReturn(true);
      replay(cRaterService, cRaterPingService);
      String response = controller.pingItem(request);
      assertEquals(response, "");
      verify(cRaterService, cRaterPingService);
    } catch (JSONException exception) {

    }
  }

  @Test
  public void pingItem_ItemNotPinged_ShouldReturnString() {
    CRaterPingRequest request = new CRaterPingRequest();
    request.setItemId(berkeleyItemId);
    try {
      expect(cRaterService.getCRaterResponse(request))
          .andReturn(createPingResponseString(berkeleyItemId, trackingId, clientId));
      expect(cRaterPingService.hasPingedItem(berkeleyItemId)).andReturn(false);
      cRaterPingService.cachePingedItem(berkeleyItemId, 280);
      expectLastCall();
      replay(cRaterService, cRaterPingService);
      String response = controller.pingItem(request);
      assertNotNull(response);
      assertNotEquals(response, "");
      verify(cRaterService, cRaterPingService);
    } catch (JSONException exception) {

    }

  }

  private String createPingResponseString(String itemId, Long trackingId, String clientId) {
    StringBuffer responseBuffer = new StringBuffer();
    responseBuffer.append("{");
    responseBuffer.append("  \"item_id\": \"" + itemId + "\",");
    responseBuffer.append("  \"success\": true,");
    responseBuffer.append("  \"tracking_id\": " + trackingId + ",");
    responseBuffer.append("  \"client_id\": \"" + clientId + "\"");
    responseBuffer.append("}");
    return responseBuffer.toString();
  }
}
