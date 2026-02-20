package org.wise.portal.presentation.web.controllers.author.project;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.project.translation.TranslateProjectService;
import org.wise.portal.service.user.UserService;

import com.fasterxml.jackson.databind.node.ObjectNode;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

@RestController
@RequestMapping("/api/author/project/translate")
@Secured({ "ROLE_AUTHOR" })
public class TranslateProjectAPIController {

  @Autowired
  protected ProjectService projectService;

  @Autowired
  protected UserService userService;

  @Autowired
  protected TranslateProjectService translateProjectService;

  @Value("${aws.accessKeyId}")
  private String accessKey;

  @Value("${aws.secretAccessKey}")
  private String secretKey;

  @Value("${aws.region}")
  private Region region;

  @PostMapping("{projectId}/{locale}")
  protected void saveTranslations(Authentication auth,
      @PathVariable("projectId") ProjectImpl project, @PathVariable("locale") String locale,
      @RequestBody ObjectNode translations) throws IOException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (projectService.canAuthorProject(project, user)) {
      translateProjectService.saveTranslations(project, locale, translations.toString());
    }
  }

  @PostMapping("translationSuggestions")
  protected String getSuggestedTranslation(Authentication auth, @RequestBody ObjectNode objectNode) throws IOException, IllegalArgumentException {
    String srcLang = objectNode.get("srcLang").asText();
    String targetLang = objectNode.get("targetLang").asText(); 
    String srcText = objectNode.get("srcText").asText(); 
    String srcLangCode = this.convertLanguageToAWSCode(srcLang);
    String targetLangCode = this.convertLanguageToAWSCode(targetLang);
    TranslateClient translateClient = buildTranslateClient();
    TranslateTextRequest request = buildTranslateTextRequest(srcText, srcLangCode, targetLangCode);
    TranslateTextResponse textResponse = translateClient.translateText(request);
    return textResponse.translatedText();
  }

  private TranslateClient buildTranslateClient() {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    return TranslateClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
  }

  private TranslateTextRequest buildTranslateTextRequest(String srcText, String srcLangCode,
      String targetLangCode) {
    return TranslateTextRequest.builder()
                .text(srcText)
                .sourceLanguageCode(srcLangCode)
                .targetLanguageCode(targetLangCode)
                .build();
  }

  private String convertLanguageToAWSCode(String language) throws IllegalArgumentException {
    return switch (language) {
      case "English" -> "en";
      case "Spanish" -> "es";
      case "Italian" -> "it";
      case "Japanese" -> "ja";
      case "German" -> "de";
      case "Chinese (Simplified)" -> "zh";
      case "Chinese (Traditional)" -> "zh-TW";
      case "Dutch" -> "nl";
      case "Korean" -> "ko";
      case "Vietnamese" -> "vi";
      default -> throw new IllegalArgumentException("Invalid language provided");
    };
  }
  
}
