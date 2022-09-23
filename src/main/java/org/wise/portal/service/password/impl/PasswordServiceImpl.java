package org.wise.portal.service.password.impl;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.wise.portal.service.password.PasswordService;

@Service
public class PasswordServiceImpl implements PasswordService {
  private Integer minLength = 8;
  private String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9]+$";

  public boolean isValidLength(String password) {
    return password.length() >= minLength;
  }

  public boolean isValidPattern(String password) {
    return Pattern.matches(pattern, password);
  }
}
