package org.wise.vle.domain.webservice.crater;

import static org.easymock.EasyMock.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(EasyMockExtension.class)
public class CRaterServiceTest {

  private CRaterService cRaterService;

  @Mock
  private Environment appProperties;

  private MockRestServiceServer mockServer;

  private String clientId = "wise-test";
  private String itemId = "test-item-id";
  private String password = "abc123";
  private String scoringUrl = "https://test.org/score";
  private String verifyUrl = "https://test.org/verify";
  private String berkeleyScoringUrl = "https://test.org/score/berkeley";
  private String berkeleyVerifyUrl = "https://test.org/verify/berkeley";

  @BeforeEach
  public void setUp() {
    RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    cRaterService = new CRaterService(appProperties, builder);
  }

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
    mockServer.expect(requestTo(scoringUrl)).andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
    mockServer.verify();
  }

  @Test
  public void getVerificationResponse_ShouldGetCRaterProperties() throws JSONException {
    beforeETS();
    CRaterVerificationRequest request = new CRaterVerificationRequest();
    request.setItemId(itemId);
    expect(appProperties.getProperty("cRater_verification_url")).andReturn(verifyUrl);
    replay(appProperties);
    mockServer.expect(requestTo(verifyUrl)).andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
    mockServer.verify();
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
    mockServer.expect(requestTo(berkeleyScoringUrl)).andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
    mockServer.verify();
  }

  @Test
  public void getBerkeleyVerificationResponse_ShouldGetCRaterProperties() throws JSONException {
    beforeBerkeley();
    CRaterVerificationRequest request = new CRaterVerificationRequest();
    request.setItemId("berkeley_" + itemId);
    expect(appProperties.getProperty("berkeley_cRater_verification_url"))
        .andReturn(berkeleyVerifyUrl);
    replay(appProperties);
    mockServer.expect(requestTo(berkeleyVerifyUrl)).andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
    mockServer.verify();
  }

  @Test
  public void getScoringResponse_WhenServerReturns500_ShouldHandleGracefully()
      throws JSONException {
    beforeETS();
    CRaterScoringRequest request = new CRaterScoringRequest();
    request.setItemId(itemId);
    request.setResponseId("1234567890");
    request.setResponseText("hello");
    expect(appProperties.getProperty("cRater_scoring_url")).andReturn(scoringUrl);
    replay(appProperties);
    mockServer.expect(requestTo(scoringUrl)).andExpect(method(HttpMethod.POST))
        .andRespond(withServerError());
    cRaterService.getCRaterResponse(request);
    verify(appProperties);
    mockServer.verify();
  }
}
