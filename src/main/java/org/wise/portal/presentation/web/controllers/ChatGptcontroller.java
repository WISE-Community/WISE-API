package org.wise.portal.presentation.web.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatGptcontroller {

  @Autowired
  Environment appProperties;

  @ResponseBody
  @Secured("ROLE_USER")
  @PostMapping("/chat-gpt")
  protected String sendChatMessage(@RequestBody String body) {
    String openaiApiKey = appProperties.getProperty("OPENAI_API_KEY");
    if (openaiApiKey == null || openaiApiKey.isEmpty()) {
      throw new RuntimeException("OPENAI_API_KEY is not set");
    }
    try {
      URL url = new URL("https://api.openai.com/v1/chat/completions");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer " + openaiApiKey);
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(body);
      writer.flush();
      writer.close();
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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
