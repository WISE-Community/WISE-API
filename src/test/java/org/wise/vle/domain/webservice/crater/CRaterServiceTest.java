package org.wise.vle.domain.webservice.crater;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;

@RunWith(EasyMockRunner.class)
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

  @Before
  public void before() {
    expect(appProperties.getProperty("cRater_client_id")).andReturn(clientId);
    expect(appProperties.getProperty("cRater_password")).andReturn(password);
  }

  @Test
  public void getScoringResponse_ShouldGetCRaterProperties() throws JSONException {
    CRaterScoringRequest request = new CRaterScoringRequest();
    request.setItemId(itemId);
    request.setResponseId("1234567890");
    request.setResponseText("hello");
    expect(appProperties.getProperty("cRater_scoring_url")).andReturn(scoringUrl);
    replay(appProperties);
    cRaterService.getScoringResponse(request);
    verify(appProperties);
  }

  @Test
  public void getVerificationResponse_ShouldGetCRaterProperties() throws JSONException {
    CRaterVerificationRequest request = new CRaterVerificationRequest();
    request.setItemId(itemId);
    expect(appProperties.getProperty("cRater_verification_url")).andReturn(verifyUrl);
    replay(appProperties);
    cRaterService.getVerificationResponse(request);
    verify(appProperties);
  }
}
