package org.wise.portal.service.password.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.service.password.PasswordService;

@RunWith(EasyMockRunner.class)
public class PasswordServiceImplTest {
  private static final String INVALID_PASSWORD_MISSING_LETTER = "1234567!";
  private static final String INVALID_PASSWORD_MISSING_NUMBER = "abcdefg!";
  private static final String INVALID_PASSWORD_MISSING_SYMBOL = "abcd1234";
  private static final String INVALID_PASSWORD_TOO_SHORT = "abc123!";

  @TestSubject
  private PasswordService passwordService = new PasswordServiceImpl();

  @Test
  public void isValid_MissingLetter_ShouldReturnTrue() {
    assertFalse(passwordService.isValid(INVALID_PASSWORD_MISSING_LETTER));
  }

  @Test
  public void getErrors_MissingLetter_ShouldReturnErrors() {
    Map<String, Object> errors = passwordService.getErrors(INVALID_PASSWORD_MISSING_LETTER);
    assertErrors(errors, true, false, false, false);
  }

  @Test
  public void isValid_MissingNumber_ShouldReturnTrue() {
    assertFalse(passwordService.isValid(INVALID_PASSWORD_MISSING_NUMBER));
  }

  @Test
  public void getErrors_MissingNumber_ShouldReturnErrors() {
    Map<String, Object> errors = passwordService.getErrors(INVALID_PASSWORD_MISSING_NUMBER);
    assertErrors(errors, false, true, false, false);
  }

  @Test
  public void isValid_MissingSymbol_ShouldReturnTrue() {
    assertFalse(passwordService.isValid(INVALID_PASSWORD_MISSING_SYMBOL));
  }

  @Test
  public void getErrors_MissingSymbol_ShouldReturnErrors() {
    Map<String, Object> errors = passwordService.getErrors(INVALID_PASSWORD_MISSING_SYMBOL);
    assertErrors(errors, false, false, true, false);
  }

  @Test
  public void isValid_TooShort_ShouldReturnTrue() {
    assertFalse(passwordService.isValid(INVALID_PASSWORD_TOO_SHORT));
  }

  @Test
  public void getErrors_TooShort_ShouldReturnErrors() {
    Map<String, Object> errors = passwordService.getErrors(INVALID_PASSWORD_TOO_SHORT);
    assertErrors(errors, false, false, false, true);
  }

  private void assertErrors(Map<String, Object> errors, Boolean missingLetter,
      Boolean missingNumber, Boolean missingSymbol, Boolean tooShort) {
    assertEquals(missingLetter, (Boolean) errors.get("missingLetter"));
    assertEquals(missingNumber, (Boolean) errors.get("missingNumber"));
    assertEquals(missingSymbol, (Boolean) errors.get("missingSymbol"));
    assertEquals(tooShort, (Boolean) errors.get("tooShort"));
  }

  @Test
  public void isValid_IsValid_ShouldReturnTrue() {
    assertTrue(passwordService.isValid("abcd123!"));
  }
}
