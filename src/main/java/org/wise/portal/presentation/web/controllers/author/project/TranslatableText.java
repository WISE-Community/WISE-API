package org.wise.portal.presentation.web.controllers.author.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class TranslatableText {
  protected String srcLangCode;
  protected String targetLangCode;
  protected String srcText;
  protected String contentType;

  public TranslatableText(String srcLang, String targetLang, String srcText) {
    this(srcLang, targetLang, srcText, "text");
  }

  @JsonCreator
  public TranslatableText(
      @JsonProperty("srcLang") String srcLang,
      @JsonProperty("targetLang") String targetLang,
      @JsonProperty("srcText") String srcText,
      @JsonProperty("contentType") String contentType) {
    this.srcLangCode = this.convertLanguageToAWSCode(srcLang);
    this.targetLangCode = this.convertLanguageToAWSCode(targetLang);
    this.srcText = srcText;
    this.contentType = contentType == null ? "text" : contentType;
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
