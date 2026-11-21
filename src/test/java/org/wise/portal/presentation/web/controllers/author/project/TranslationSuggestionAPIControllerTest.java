package org.wise.portal.presentation.web.controllers.author.project;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.easymock.Capture;
import org.easymock.EasyMockExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.Document;
import software.amazon.awssdk.services.translate.model.TranslateDocumentRequest;
import software.amazon.awssdk.services.translate.model.TranslateDocumentResponse;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;
import software.amazon.awssdk.services.translate.model.TranslatedDocument;

@ExtendWith(EasyMockExtension.class)
public class TranslationSuggestionAPIControllerTest extends APIControllerTest {

  private static final String TRANSLATABLE_TEXT_JSON =
      "{\"srcLang\":\"Spanish\",\"targetLang\":\"Italian\",\"srcText\":\"texto para traducir\"}";

  @Test
  public void getSuggestedTranslation_UsesTextTranslationWhenContentTypeIsOmitted() throws Exception {
    TranslateClient translateClient = createMock(TranslateClient.class);
    Capture<TranslateTextRequest> requestCapture = newCapture();
    expect(translateClient.translateText(capture(requestCapture)))
        .andReturn(TranslateTextResponse.builder().translatedText("testo tradotto").build());
    replay(translateClient);

    TranslationSuggestionAPIController controller = createController(translateClient);
    TranslatableText request = readRequest(TRANSLATABLE_TEXT_JSON);

    assertEquals("text", request.getContentType());
    assertEquals("testo tradotto", controller.getSuggestedTranslation(teacherAuth, request));
    assertEquals("texto para traducir", requestCapture.getValue().text());
    assertEquals("es-MX", requestCapture.getValue().sourceLanguageCode());
    assertEquals("it", requestCapture.getValue().targetLanguageCode());
    verify(translateClient);
  }

  @Test
  public void getSuggestedTranslation_UsesDocumentTranslationWhenHtmlHasNoLinkText() throws Exception {
    assertHtmlTranslation(
        "{\"srcLang\":\"English\",\"targetLang\":\"Spanish\",\"srcText\":\"<p "
            + "title=\\\"note link-text='course overview'\\\">Hello</p>\","
            + "\"contentType\":\"html\"}",
        "<p title=\"note link-text='course overview'\">Hello</p>",
        "<p title=\"note link-text='course overview'\">Hola</p>",
        "en",
        "es-MX");
  }

  @Test
  public void getSuggestedTranslation_TranslatesHtmlIntoEnglish() throws Exception {
    assertHtmlTranslation(
        "{\"srcLang\":\"Italian\",\"targetLang\":\"English\",\"srcText\":\"<p>Ciao</p>\","
            + "\"contentType\":\"html\"}",
        "<p>Ciao</p>",
        "<p>Hello</p>",
        "it",
        "en");
  }

  @Test
  public void getSuggestedTranslation_TranslatesLinkTextAttributesAndPreservesHtml() throws Exception {
    String sourceHtml =
        "<p>Visit <wiselink id='keep-me' link-text='course &amp; overview'></wiselink> then "
            + "<wiselink id='keep-me-too' link-text=\"next step\"></wiselink>.</p>";
    String translatedDocumentHtml =
        "<p>Visita <wiselink id='keep-me' link-text='course &amp; overview'></wiselink> luego "
            + "<wiselink id='keep-me-too' link-text=\"next step\"></wiselink>.</p>";
    TranslateClient translateClient = createMock(TranslateClient.class);
    Capture<TranslateDocumentRequest> documentRequestCapture = newCapture();
    Capture<TranslateTextRequest> textRequestCapture = newCapture();
    expect(translateClient.translateDocument(capture(documentRequestCapture)))
        .andReturn(TranslateDocumentResponse.builder()
            .translatedDocument(TranslatedDocument.builder()
                .content(SdkBytes.fromUtf8String(translatedDocumentHtml))
                .build())
            .build());
    expect(translateClient.translateText(capture(textRequestCapture)))
        .andReturn(TranslateTextResponse.builder()
            .translatedText("descripción del curso\nsiguiente paso")
            .build());
    replay(translateClient);

    TranslationSuggestionAPIController controller = createController(translateClient);
    TranslatableText request = new TranslatableText("English", "Spanish", sourceHtml, "html");

    String translatedHtml = controller.getSuggestedTranslation(teacherAuth, request);
    org.jsoup.nodes.Document translatedDocument = Jsoup.parseBodyFragment(translatedHtml);

    assertEquals(1, translatedDocument.select("p").size());
    assertEquals("Visita luego .", translatedDocument.selectFirst("p").text());

    Elements translatedLinks = translatedDocument.select("wiselink[link-text]");
    Element courseOverviewLink = translatedLinks.get(0);
    Element nextStepLink = translatedLinks.get(1);
    assertEquals(2, translatedLinks.size());
    assertEquals("keep-me", courseOverviewLink.attr("id"));
    assertEquals("descripción del curso", courseOverviewLink.attr("link-text"));
    assertEquals("keep-me-too", nextStepLink.attr("id"));
    assertEquals("siguiente paso", nextStepLink.attr("link-text"));
    assertEquals("course & overview\nnext step", textRequestCapture.getValue().text());
    verify(translateClient);
  }

  @Test
  public void getSuggestedTranslation_RejectsHtmlWithoutEnglishLanguage() throws Exception {
    assertRequestRejected(
        "{\"srcLang\":\"Spanish\",\"targetLang\":\"Italian\",\"srcText\":\"texto\","
            + "\"contentType\":\"html\"}",
        "HTML translation requires English as either the source or target language");
  }

  @Test
  public void getSuggestedTranslation_RejectsUnsupportedContentType() throws Exception {
    assertRequestRejected(
        "{\"srcLang\":\"English\",\"targetLang\":\"Spanish\",\"srcText\":\"Hello\","
            + "\"contentType\":\"xml\"}",
        "Unsupported content type: xml");
  }

  @Test
  public void getSuggestedTranslation_ThrowsIfPropertiesEmpty() throws Exception {
    TranslateClient translateClient = createMock(TranslateClient.class);
    TranslationSuggestionAPIController controller = new TestTranslationSuggestionAPIController(translateClient);
    ReflectionTestUtils.setField(controller, "accessKey", "");
    ReflectionTestUtils.setField(controller, "secretKey", "");
    ReflectionTestUtils.setField(controller, "region", "");

    TranslatableText request = readRequest(TRANSLATABLE_TEXT_JSON);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> controller.getSuggestedTranslation(teacherAuth, request));

    assertEquals(500, exception.getStatusCode().value());
  }

  private void assertHtmlTranslation(String requestJson, String sourceHtml, String translatedHtml,
      String sourceLanguageCode, String targetLanguageCode) throws Exception {
    TranslateClient translateClient = createMock(TranslateClient.class);
    Capture<TranslateDocumentRequest> requestCapture = newCapture();
    expect(translateClient.translateDocument(capture(requestCapture)))
        .andReturn(TranslateDocumentResponse.builder()
            .translatedDocument(TranslatedDocument.builder()
                .content(SdkBytes.fromUtf8String(translatedHtml))
                .build())
            .build());
    replay(translateClient);

    TranslationSuggestionAPIController controller = createController(translateClient);
    TranslatableText request = readRequest(requestJson);

    assertEquals(translatedHtml, controller.getSuggestedTranslation(teacherAuth, request));
    Document document = requestCapture.getValue().document();
    assertEquals("text/html", document.contentType());
    assertEquals(sourceHtml, document.content().asUtf8String());
    assertEquals(sourceLanguageCode, requestCapture.getValue().sourceLanguageCode());
    assertEquals(targetLanguageCode, requestCapture.getValue().targetLanguageCode());
    verify(translateClient);
  }

  private void assertRequestRejected(String requestJson, String expectedReason) throws Exception {
    TranslateClient translateClient = createMock(TranslateClient.class);
    replay(translateClient);
    TranslationSuggestionAPIController controller = createController(translateClient);
    TranslatableText request = readRequest(requestJson);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> controller.getSuggestedTranslation(teacherAuth, request));

    assertEquals(400, exception.getStatusCode().value());
    assertEquals(expectedReason, exception.getReason());
    verify(translateClient);
  }

  private TranslationSuggestionAPIController createController(TranslateClient translateClient) {
    TranslationSuggestionAPIController controller = new TestTranslationSuggestionAPIController(translateClient);
    ReflectionTestUtils.setField(controller, "accessKey", "access-key");
    ReflectionTestUtils.setField(controller, "secretKey", "secret-key");
    ReflectionTestUtils.setField(controller, "region", "us-east-1");
    return controller;
  }

  private TranslatableText readRequest(String json) throws Exception {
    return new ObjectMapper().readValue(json, TranslatableText.class);
  }

  private static class TestTranslationSuggestionAPIController extends TranslationSuggestionAPIController {

    private final TranslateClient translateClient;

    private TestTranslationSuggestionAPIController(TranslateClient translateClient) {
      this.translateClient = translateClient;
    }

    @Override
    protected TranslateClient buildTranslateClient() {
      return translateClient;
    }
  }
}
