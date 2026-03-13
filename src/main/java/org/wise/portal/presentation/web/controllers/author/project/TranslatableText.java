package org.wise.portal.presentation.web.controllers.author.project;

import lombok.Getter;

@Getter
public class TranslatableText {
  private String srcLangCode;
  private String targetLangCode;
  private String srcText;

  public TranslatableText(String srcLang, String targetLang, String srcText) {
    this.srcLangCode = this.convertLanguageToAWSCode(srcLang);
    this.targetLangCode = this.convertLanguageToAWSCode(targetLang);
    this.srcText = srcText;
  }

  private String convertLanguageToAWSCode(String language) throws IllegalArgumentException {
    return switch (language) {
      case "English" -> "en";
      case "Spanish" -> "es-MX";
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
