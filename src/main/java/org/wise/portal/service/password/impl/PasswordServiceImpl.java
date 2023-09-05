package org.wise.portal.service.password.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.wise.portal.service.password.PasswordService;

@Service
public class PasswordServiceImpl implements PasswordService {
  private Integer minLength = 8;
  private String letterRequirement = "^(?=.*[a-zA-Z]).*$";
  private String numberRequirement = "^(?=.*[0-9]).*$";
  private String symbolRequirement = "^(?=.*[!@#$%^&*]).*$";

  private boolean isValidLength(String password) {
    return password.length() >= minLength;
  }

  private boolean isLetterRequirementSatisfied(String password) {
    return Pattern.matches(letterRequirement, password);
  }

  private boolean isNumberRequirementSatisfied(String password) {
    return Pattern.matches(numberRequirement, password);
  }

  private boolean isSymbolRequirementSatisfied(String password) {
    return Pattern.matches(symbolRequirement, password);
  }

  public boolean isValid(String password) {
    return isLetterRequirementSatisfied(password) && isNumberRequirementSatisfied(password)
        && isSymbolRequirementSatisfied(password) && isValidLength(password);
  }

  public Map<String, Object> getErrors(String password) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("missingLetter", !isLetterRequirementSatisfied(password));
    map.put("missingNumber", !isNumberRequirementSatisfied(password));
    map.put("missingSymbol", !isSymbolRequirementSatisfied(password));
    map.put("tooShort", !isValidLength(password));
    if (!isValid(password)) {
      map.put("messageCode", "invalidPassword");
    }
    return map;
  }
}
