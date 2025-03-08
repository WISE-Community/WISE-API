package org.wise.vle.domain.webservice.crater;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.env.Environment;

@ExtendWith(EasyMockExtension.class)
public class CRaterServiceTest {

  @TestSubject
  private CRaterService cRaterService = new CRaterService();

  @Mock
  private Environment appProperties;

  private String clientId = "wise-test";
  private String itemId = "test-item-id";
  private String password = "abc123";
  private String scoringUrl = "https://test.org/score";
  private String verifyUrl = "https://test.org/verify";
  private String berkeleyScoringUrl = "https://test.org/score/berkeley";
  private String berkeleyVerifyUrl = "https://test.org/verify/berkeley";

  public void beforeETS() {
    expect(appProperties.getProperty("cRater_client_id")).andReturn(clientId);
    expect(appProperties.getProperty("cRater_password")).andReturn(password);
  }

  public void beforeBerkeley() {
    expect(appProperties.getProperty("berkeley_cRater_client_id")).andReturn(clientId);
    expect(appProperties.getProperty("berkeley_cRater_password")).andReturn(password);
  }

  @Test
  public void getScoringResponse_ShouldGetCRaterProperties() throws JSONException {
    beforeETS();
    CRaterScoringRequest request = new CRaterScoringRequest();
    request.setItemId(itemId);
    request.setResponseId("1234567890");
    request.setResponseText("hello");
    expect(appProperties.getProperty("cRater_scoring_url")).andReturn(scoringUrl);
    replay(appProperties);
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
  }

  @Test
  public void getVerificationResponse_ShouldGetCRaterProperties() throws JSONException {
    beforeETS();
    CRaterVerificationRequest request = new CRaterVerificationRequest();
    request.setItemId(itemId);
    expect(appProperties.getProperty("cRater_verification_url")).andReturn(verifyUrl);
    replay(appProperties);
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
  }

  @Test
  public void getBerkeleyScoringResponse_ShouldGetCRaterProperties() throws JSONException {
    beforeBerkeley();
    CRaterScoringRequest request = new CRaterScoringRequest();
    request.setItemId("berkeley_" + itemId);
    request.setResponseId("1234567890");
    request.setResponseText("hello");
    expect(appProperties.getProperty("berkeley_cRater_scoring_url")).andReturn(berkeleyScoringUrl);
    replay(appProperties);
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
  }

  @Test
  public void getBerkeleyVerificationResponse_ShouldGetCRaterProperties() throws JSONException {
    beforeBerkeley();
    CRaterVerificationRequest request = new CRaterVerificationRequest();
    request.setItemId("berkeley_" + itemId);
    expect(appProperties.getProperty("berkeley_cRater_verification_url"))
        .andReturn(berkeleyVerifyUrl);
    replay(appProperties);
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
  }
}
