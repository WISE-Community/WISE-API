package org.wise.portal.presentation.web;

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
@RequestMapping("/api/aws-bedrock/chat")
public class AWSBedrockController {

	@Autowired
	Environment appProperties;

	@ResponseBody
	@Secured("ROLE_USER")
	@PostMapping
	protected String sendChatMessage(@RequestBody String body) {
		String awsBedrockApiKey = appProperties.getProperty("aws.bedrock.api.key");
		if (awsBedrockApiKey == null || awsBedrockApiKey.isEmpty()) {
			throw new RuntimeException("aws.bedrock.api.key is not set");
		}
		String awsBedrockApiUrl = appProperties.getProperty("aws.bedrock.api.url");
		if (awsBedrockApiUrl == null || awsBedrockApiUrl.isEmpty()) {
			throw new RuntimeException("aws.bedrock.api.url is not set");
		}
		try {
			URL url = new URL(awsBedrockApiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", "Bearer " + awsBedrockApiKey);
			connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(body);
			writer.flush();
			writer.close();
			BufferedReader br = new BufferedReader(
			    new InputStreamReader(connection.getInputStream(), "UTF-8"));
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
