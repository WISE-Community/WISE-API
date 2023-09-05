package org.wise.portal.presentation.web.response;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseEntityGenerator {
  public static ResponseEntity<Map<String, Object>> createSuccess(String messageCode) {
    Map<String, Object> body = new HashMap<>();
    body.put("messageCode", messageCode);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  public static ResponseEntity<Map<String, Object>> createSuccess(Map<String, Object> map) {
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  public static ResponseEntity<Map<String, Object>> createError(String messageCode) {
    Map<String, Object> body = new HashMap<>();
    body.put("messageCode", messageCode);
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  public static ResponseEntity<Map<String, Object>> createError(Map<String, Object> map) {
    return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
  }
}
