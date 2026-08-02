package org.wise.portal.service.mail.teacher;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

public interface TeacherMailService {
  
  void sendWelcomeTeacherEmail(String email, String displayName, String username, 
      boolean socialAccount, Locale locale, HttpServletRequest request);

  void sendVerifyTeacherEmail(String email, String verificationCode, Locale locale, 
      HttpServletRequest request);

  boolean isSendEmailEnabled();
}
