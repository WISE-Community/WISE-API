package org.wise.portal.presentation.web.controllers.author.project;

import java.io.IOException;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.Document;
import software.amazon.awssdk.services.translate.model.TranslateDocumentRequest;
import software.amazon.awssdk.services.translate.model.TranslateDocumentResponse;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

@RestController
@RequestMapping("/api/author/project/translate/suggest")
@Secured({ "ROLE_AUTHOR" })
public class TranslationSuggestionAPIController {

  @Value("${aws.accessKeyId:}")
  private String accessKey;

  @Value("${aws.secretAccessKey:}")
  private String secretKey;

  @Value("${aws.region:}")
  private String region;

  @PostMapping
  protected String getSuggestedTranslation(Authentication auth, @RequestBody TranslatableText translatableText)
    throws IOException, IllegalArgumentException, ResponseStatusException {
    validateContentType(translatableText);
    if (accessKey.equals("") || secretKey.equals("") || region.equals("")) {
      throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Missing application properties necessary for AWS Translate"
      );
    } else {
      TranslateClient translateClient = buildTranslateClient();
      if (translatableText.getContentType().equals("html")) {
        TranslateDocumentRequest request = buildTranslateDocumentRequest(translatableText);
        String translatedHtml = this.translateDocument(translateClient, request);
        return this.translateLinkTextAttributes(translateClient, translatableText, translatedHtml);
      } else {
        TranslateTextRequest request = buildTranslateTextRequest(translatableText);
        return this.translateText(translateClient, request);
      }
    }
  }

  private void validateContentType(TranslatableText translatableText) throws ResponseStatusException {
    String contentType = translatableText.getContentType();
    if (!contentType.equals("text") && !contentType.equals("html")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported content type: " + contentType);
    } else if (contentType.equals("html") && nonEnglishTranslation(translatableText)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "HTML translation requires English as either the source or target language");
    }
  }

  private boolean nonEnglishTranslation(TranslatableText translatableText) {
    return !translatableText.getSrcLangCode().equals("en") && !translatableText.getTargetLangCode().equals("en");
  }

  protected TranslateClient buildTranslateClient() {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    return TranslateClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
  }

  private TranslateTextRequest buildTranslateTextRequest(TranslatableText translatableText) {
    return buildTranslateTextRequest(translatableText.getSrcText(), translatableText);
  }

  private TranslateTextRequest buildTranslateTextRequest(String text, TranslatableText translatableText) {
    return TranslateTextRequest.builder()
        .text(text)
        .sourceLanguageCode(translatableText.getSrcLangCode())
        .targetLanguageCode(translatableText.getTargetLangCode())
        .build();
  }

  private TranslateDocumentRequest buildTranslateDocumentRequest(TranslatableText translatableText) {
    Document document = Document.builder()
        .content(SdkBytes.fromUtf8String(translatableText.getSrcText()))
        .contentType("text/html")
        .build();
    return TranslateDocumentRequest.builder()
        .document(document)
        .sourceLanguageCode(translatableText.getSrcLangCode())
        .targetLanguageCode(translatableText.getTargetLangCode())
        .build();
  }

  private String translateText(TranslateClient client, TranslateTextRequest request) throws ResponseStatusException {
    TranslateTextResponse textResponse;
      try {
        textResponse = client.translateText(request);
      } catch (Exception e) {
        throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Translation failed"
        );
      }
      return textResponse.translatedText();
  }

  private String translateDocument(TranslateClient client, TranslateDocumentRequest request)
      throws ResponseStatusException {
    TranslateDocumentResponse documentResponse;
    try {
      documentResponse = client.translateDocument(request);
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Translation failed");
    }
    return documentResponse.translatedDocument().content().asUtf8String();
  }

  private String translateLinkTextAttributes(TranslateClient client, TranslatableText translatableText,
      String translatedHtml) {
    List<String> sourceValues = getLinkTextValues(translatableText.getSrcText());
    if (sourceValues.isEmpty()) {
      return translatedHtml;
    }
    Element translatedDocument = parseHtmlFragment(translatedHtml);
    List<Element> translatedLinks = getLinksWithText(translatedDocument);
    if (translatedLinks.size() != sourceValues.size()) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Translation failed to preserve link-text attributes");
    }
    List<String> translatedValues = translateLinkTextValues(client, translatableText, sourceValues);
    replaceLinkTextValues(translatedLinks, translatedValues);
    return translatedDocument.html();
  }

  private List<String> getLinkTextValues(String html) {
    return getLinksWithText(parseHtmlFragment(html)).stream()
        .map(link -> link.attr("link-text"))
        .toList();
  }

  private List<Element> getLinksWithText(Element html) {
    return html.getElementsByTag("wiselink").stream()
        .filter(link -> link.hasAttr("link-text"))
        .toList();
  }

  private Element parseHtmlFragment(String html) {
    Element root = new Element("wise-root");
    for (Node node : Parser.parseFragment(html, root, "")) {
      root.appendChild(node);
    }
    return root;
  }

  private List<String> translateLinkTextValues(TranslateClient client, TranslatableText translatableText,
      List<String> sourceValues) {
    List<String> translatedValues = translateValues(client, translatableText, sourceValues);
    if (translatedValues.size() == sourceValues.size()) {
      return translatedValues;
    } else {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Translation failed to preserve link-text lines");
    }
  }

  private List<String> translateValues(TranslateClient client,
      TranslatableText translatableText, List<String> sourceValues) {
    String joinedSourceValues = String.join("\n", sourceValues);
    String joinedTranslatedValues =
        translateText(client, buildTranslateTextRequest(joinedSourceValues, translatableText));
    List<String> translatedValues = List.of(joinedTranslatedValues.split("\\R", -1));
    return translatedValues;
  }

  private void replaceLinkTextValues(List<Element> translatedLinks, List<String> translatedValues) {
    for (int i = 0; i < translatedValues.size(); i++) {
      translatedLinks.get(i).attr("link-text", translatedValues.get(i));
    }
  }
}
