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

  @TestSubject
  private PasswordService passwordService = new PasswordServiceImpl();

  @Test
  public void isValid_MissingLetter_ShouldReturnTrue() {
    assertFalse(passwordService.isValid(PasswordServiceImpl.INVALID_PASSWORD_MISSING_LETTER));
  }

  @Test
  public void getErrors_MissingLetter_ShouldReturnErrors() {
    Map<String, Object> errors = passwordService
        .getErrors(PasswordServiceImpl.INVALID_PASSWORD_MISSING_LETTER);
    assertErrors(errors, true, false, false);
  }

  @Test
  public void isValid_MissingNumber_ShouldReturnTrue() {
    assertFalse(passwordService.isValid(PasswordServiceImpl.INVALID_PASSWORD_MISSING_NUMBER));
  }

  @Test
  public void getErrors_MissingNumber_ShouldReturnErrors() {
    Map<String, Object> errors = passwordService
        .getErrors(PasswordServiceImpl.INVALID_PASSWORD_MISSING_NUMBER);
    assertErrors(errors, false, true, false);
  }

  @Test
  public void isValid_TooShort_ShouldReturnTrue() {
    assertFalse(passwordService.isValid(PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT));
  }

  @Test
  public void getErrors_TooShort_ShouldReturnErrors() {
    Map<String, Object> errors = passwordService
        .getErrors(PasswordServiceImpl.INVALID_PASSWORD_TOO_SHORT);
    assertErrors(errors, false, false, true);
  }

  private void assertErrors(Map<String, Object> errors, Boolean missingLetter,
      Boolean missingNumber, Boolean tooShort) {
    assertEquals(missingLetter, (Boolean) errors.get("missingLetter"));
    assertEquals(missingNumber, (Boolean) errors.get("missingNumber"));
    assertEquals(tooShort, (Boolean) errors.get("tooShort"));
  }

  @Test
  public void isValid_isValidPassword_ShouldReturnTrue() {
    assertTrue(passwordService.isValid(PasswordServiceImpl.VALID_PASSWORD));
  }

  @Test
  public void isValid_isValidPasswordWithSymbols_ShouldReturnTrue() {
    assertTrue(passwordService.isValid(PasswordServiceImpl.VALID_PASSWORD + "!$"));
  }
}
