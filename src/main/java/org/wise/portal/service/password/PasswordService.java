package org.wise.portal.service.password;

import java.util.Map;

public interface PasswordService {
  public boolean isValid(String password);

  public Map<String, Object> getErrors(String password);
}
