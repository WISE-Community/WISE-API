package org.wise.portal.presentation.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

public class AWSBedrockControllerTest {

  private static final String API_KEY = "bedrock-key-12345";
  private static final String BASE_ENDPOINT = "https://bedrock.example.com";
  private static final String EXPECTED_URL = BASE_ENDPOINT + "/openai/v1/chat/completions";

  private MockEnvironment env;
  private MockRestServiceServer mockServer;
  private AWSBedrockController controller;

  @BeforeEach
  public void setUp() {
    env = new MockEnvironment();
    env.setProperty("aws.bedrock.api.key", API_KEY);
    env.setProperty("aws.bedrock.runtime.endpoint", BASE_ENDPOINT);

    RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    controller = new AWSBedrockController(env, builder);
  }

  @Test
  public void sendChatMessage_successfulResponse() {
    String requestBody = """
        {
          "prompt": "test prompt"
        }""";
    String expectedResponse = """
        {
          "response": "test response"
        }""";

    mockServer.expect(requestTo(EXPECTED_URL))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(requestBody))
        .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

    String result = controller.sendChatMessage(requestBody);

    assertEquals(expectedResponse, result);
    mockServer.verify();
  }

  @Test
  public void sendChatMessage_missingApiKey_throwsException() {
    MockEnvironment missingKeyEnv = new MockEnvironment();
    missingKeyEnv.setProperty("aws.bedrock.runtime.endpoint", BASE_ENDPOINT);
    RestClient.Builder builder = RestClient.builder();
    AWSBedrockController ctrl = new AWSBedrockController(missingKeyEnv, builder);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      ctrl.sendChatMessage("{}");
    });

    assertEquals("aws.bedrock.api.key is not set", exception.getMessage());
  }

  @Test
  public void sendChatMessage_missingEndpoint_throwsException() {
    MockEnvironment missingEndpointEnv = new MockEnvironment();
    missingEndpointEnv.setProperty("aws.bedrock.api.key", API_KEY);
    RestClient.Builder builder = RestClient.builder();
    AWSBedrockController ctrl = new AWSBedrockController(missingEndpointEnv, builder);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      ctrl.sendChatMessage("{}");
    });

    assertEquals("aws.bedrock.runtime.endpoint is not set", exception.getMessage());
  }
}
