package org.wise.portal.presentation.web.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat-gpt")
public class ChatGptController {

  @Value("${openai.api.key:}")
  private String openAiApiKey;

  @Value("${openai.chat.api.url:https://api.openai.com/v1/chat/completions}")
  private String openAiChatApiUrl;

  @ResponseBody
  @Secured("ROLE_USER")
  @PostMapping
  protected String sendChatMessage(@RequestBody String body) {
    if (openAiApiKey == null || openAiApiKey.isEmpty()) {
      throw new RuntimeException("OPENAI_API_KEY is not set");
    }
    try {
      URL url = new URL(openAiChatApiUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer " + openAiApiKey);
      connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      connection.setRequestProperty("Accept-Charset", "UTF-8");
      connection.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(body);
      writer.flush();
      writer.close();
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"ISO-8859-1"));
      String line;
      StringBuffer response = new StringBuffer();
      while ((line = br.readLine()) != null) {
        response.append(line);
      }
      br.close();
      return response.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
