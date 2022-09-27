package org.wise.portal.service.password;

public interface PasswordService {
  public boolean isValidLength(String password);

  public boolean isValidPattern(String password);
}
