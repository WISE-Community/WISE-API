package org.wise.portal.presentation.web;

import org.springframework.core.env.Environment;
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
@RequestMapping("/api/aws-bedrock/chat")
public class AWSBedrockController {

  private final Environment appProperties;
  private final RestClient restClient;

  public AWSBedrockController(Environment appProperties, RestClient.Builder restClientBuilder) {
    this.appProperties = appProperties;
    this.restClient = restClientBuilder.build();
  }

  @ResponseBody
  @Secured("ROLE_USER")
  @PostMapping(produces = "application/json;charset=UTF-8")
  public String sendChatMessage(@RequestBody String body) {
    String apiKey = appProperties.getProperty("aws.bedrock.api.key");
    if (apiKey == null || apiKey.isEmpty()) {
      throw new RuntimeException("aws.bedrock.api.key is not set");
    }
    String apiEndpoint = appProperties.getProperty("aws.bedrock.runtime.endpoint");
    if (apiEndpoint == null || apiEndpoint.isEmpty()) {
      throw new RuntimeException("aws.bedrock.runtime.endpoint is not set");
    }
    // assume openai-only support for now. We'll add other models later.
    apiEndpoint += "/openai/v1/chat/completions";

    return restClient.post()
        .uri(apiEndpoint)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(String.class);
  }
}
