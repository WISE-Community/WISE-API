package org.wise.portal.presentation.web.controllers;

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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

public class ChatGptControllerTest {

  private static final String API_KEY = "sk-test-key-12345";
  private static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";

  private MockRestServiceServer mockServer;
  private ChatGptController controller;

  @BeforeEach
  public void setUp() {
    RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    controller = new ChatGptController(API_KEY, CHAT_URL, builder);
  }

  @Test
  public void sendChatMessage_successfulResponse() {
    String requestBody = """
        {
          "model": "gpt-4",
          "messages": [
            {"role": "user", "content": "hello"}
          ]
        }""";
    String expectedResponse = """
        {
          "choices": [
            {"message": {"role": "assistant", "content": "hi!"}}
          ]
        }""";

    mockServer.expect(requestTo(CHAT_URL))
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
    RestClient.Builder builder = RestClient.builder();
    ChatGptController controllerWithoutKey = new ChatGptController("", CHAT_URL, builder);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      controllerWithoutKey.sendChatMessage("{\"messages\":[]}");
    });

    assertEquals("openai.api.key is not set", exception.getMessage());
  }

  @Test
  public void sendChatMessage_nullApiKey_throwsException() {
    RestClient.Builder builder = RestClient.builder();
    ChatGptController controllerWithoutKey = new ChatGptController(null, CHAT_URL, builder);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      controllerWithoutKey.sendChatMessage("{\"messages\":[]}");
    });

    assertEquals("openai.api.key is not set", exception.getMessage());
  }
}
