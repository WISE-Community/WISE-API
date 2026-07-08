package org.wise.portal.domain.newsitem;

public enum NewsType {
  PUBLIC_ONLY, PUBLIC_AND_TEACHER;

  public static NewsType stringToNewsType(String type) throws IllegalArgumentException {
    return switch (type) {
      case "publicOnly" -> PUBLIC_ONLY;
      case "publicAndTeacher" -> PUBLIC_AND_TEACHER;
      default -> throw new IllegalArgumentException("Invalid news type: " + type);
    };
  }
}
