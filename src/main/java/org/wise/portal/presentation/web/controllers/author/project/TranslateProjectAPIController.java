package org.wise.portal.presentation.web.controllers.author.project;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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

  @Value("${aws.accessKeyId:}")
  private String accessKey;

  @Value("${aws.secretAccessKey:}")
  private String secretKey;

  @Value("${aws.region:}")
  private String region;

  @PostMapping("{projectId}/{locale}")
  protected void saveTranslations(Authentication auth,
      @PathVariable("projectId") ProjectImpl project, @PathVariable("locale") String locale,
      @RequestBody ObjectNode translations) throws IOException {
    User user = userService.retrieveUserByUsername(auth.getName());
    if (projectService.canAuthorProject(project, user)) {
      translateProjectService.saveTranslations(project, locale, translations.toString());
    }
  }

  @PostMapping("suggest")
  protected String getSuggestedTranslation(Authentication auth, @RequestBody TranslatableText translatableText) throws IOException, IllegalArgumentException {
    if (accessKey.equals("") || secretKey.equals("") || region.equals("")) {
      throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Missing application properties necessary for AWS Translate"
      );
    } else {
      TranslateClient translateClient = buildTranslateClient();
      TranslateTextRequest request = buildTranslateTextRequest(translatableText);
      TranslateTextResponse textResponse = translateClient.translateText(request);
      return textResponse.translatedText();
    }
  }

  private TranslateClient buildTranslateClient() {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    return TranslateClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
  }

  private TranslateTextRequest buildTranslateTextRequest(TranslatableText translatableText) {
    return TranslateTextRequest.builder()
                .text(translatableText.getSrcText())
                .sourceLanguageCode(translatableText.getSrcLangCode())
                .targetLanguageCode(translatableText.getTargetLangCode())
                .build();
  }
}
