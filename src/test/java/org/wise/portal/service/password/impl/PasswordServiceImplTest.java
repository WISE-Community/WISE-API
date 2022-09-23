package org.wise.portal.service.password.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
  public void isValidLength_NotLongEnough_ShouldReturnFalse() {
    assertFalse(passwordService.isValidLength("1234567"));
  }

  @Test
  public void isValidLength_IsLongEnough_ShouldReturnTrue() {
    assertTrue(passwordService.isValidLength("12345678"));
  }

  @Test
  public void isValidPattern_NotValid_ShouldReturnFalse() {
    assertFalse(passwordService.isValidPattern("abcd1234"));
  }

  @Test
  public void isValidPattern_IsValid_ShouldReturnTrue() {
    assertTrue(passwordService.isValidPattern("Abcd1234"));
  }
}
