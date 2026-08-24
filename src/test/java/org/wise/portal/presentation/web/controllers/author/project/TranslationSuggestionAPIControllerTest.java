package org.wise.portal.presentation.web.controllers.author.project;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

@ExtendWith(EasyMockExtension.class)
public class TranslationSuggestionAPIControllerTest extends APIControllerTest {

  @TestSubject
  private final TranslationSuggestionAPIController controller = new TranslationSuggestionAPIController();

  @Test
  public void getSuggestedTranslation_ThrowIfPropertiesEmpty() throws Exception {
    ReflectionTestUtils.setField(controller, "accessKey", "");
    ReflectionTestUtils.setField(controller, "secretKey", "");
    ReflectionTestUtils.setField(controller, "region", "");

    TranslatableText tt = new TranslatableText("English", "Spanish", "text to translate");

    assertThrows(ResponseStatusException.class, () -> {
      controller.getSuggestedTranslation(teacherAuth, tt);
    });
  }
}

