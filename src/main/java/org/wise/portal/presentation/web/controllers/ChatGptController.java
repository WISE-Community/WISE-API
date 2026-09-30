package org.wise.portal.presentation.web.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/chat-gpt")
public class ChatGptController {

  private final String openAiApiKey;
  private final String openAiChatApiUrl;
  private final RestClient restClient;

  public ChatGptController(
      @Value("${openai.api.key:}") String openAiApiKey,
      @Value("${openai.chat.api.url:https://api.openai.com/v1/chat/completions}") String openAiChatApiUrl,
      RestClient.Builder restClientBuilder) {
    this.openAiApiKey = openAiApiKey;
    this.openAiChatApiUrl = openAiChatApiUrl;
    this.restClient = restClientBuilder.build();
  }

  @ResponseBody
  @Secured("ROLE_USER")
  @PostMapping(produces = "application/json;charset=UTF-8")
  public String sendChatMessage(@RequestBody String body) {
    if (openAiApiKey == null || openAiApiKey.isEmpty()) {
      throw new RuntimeException("openai.api.key is not set");
    }
    return restClient.post()
        .uri(openAiChatApiUrl)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(String.class);
  }
}
