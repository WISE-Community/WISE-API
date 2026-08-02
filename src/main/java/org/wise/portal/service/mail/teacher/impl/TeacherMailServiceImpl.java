package org.wise.portal.service.mail.teacher.impl;

import java.util.Locale;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.wise.portal.presentation.web.controllers.ControllerUtil;
import org.wise.portal.service.mail.MailService;
import org.wise.portal.service.mail.teacher.TeacherMailService;

@Service
public class TeacherMailServiceImpl extends MailService implements TeacherMailService  {

  private final Environment appProperties;
  private final String emailCodePrefix = 
    "presentation.web.controllers.teacher.registerTeacherController.";
  private final MessageSource messageSource;
  private final String verifyBodyCode = this.emailCodePrefix + "verifyTeacherEmailBody";
  private final String verifySubjectCode = this.emailCodePrefix + "verifyTeacherEmailSubject";
  private final String welcomeBodyCode = this.emailCodePrefix + "welcomeTeacherEmailBody";
  private final String welcomeSocialAccountBodyCode = this.emailCodePrefix + "welcomeTeacherEmailBodyNoUsername";
  private final String welcomeSubjectCode = this.emailCodePrefix + "welcomeTeacherEmailSubject";


  TeacherMailServiceImpl(Environment appProperties, MessageSource messageSource) {
    this.appProperties = appProperties;
    this.messageSource = messageSource;
  }

  @Override
  public void sendWelcomeTeacherEmail(String email, String displayName, String username,
      boolean socialAccount, Locale locale, HttpServletRequest request) {
    if (isSendEmailEnabled()) {
      String subject = getEmailMessage(this.welcomeSubjectCode, this.welcomeSubjectCode, null, locale);
      String body = getWelcomeTeacherBody(displayName, username, socialAccount, locale, request);
      this.sendEmail(email, subject, body);
    }
  }

  @Override
  public void sendVerifyTeacherEmail(String email, String verificationCode, 
      Locale locale, HttpServletRequest request) {
    if (isSendEmailEnabled()) {
      String subject = getEmailMessage(this.verifySubjectCode, this.verifySubjectCode, null, locale);
      String verificationUrl = getVerificationUrl(verificationCode, request);
      Object[] args = new Object[] { verificationUrl };
      String body = getEmailMessage(this.verifyBodyCode, this.verifyBodyCode, args, locale);
      this.sendEmail(email, subject, body);
    }
  }

  @Override
  public boolean isSendEmailEnabled() {
    String sendEmailEnabledStr = appProperties.getProperty("send_email_enabled", "false");
    return Boolean.valueOf(sendEmailEnabledStr);
  }

  private String getEmailMessage(String defaultCode, String code, Object[] args, Locale locale) {
    String defaultMessage = messageSource.getMessage(defaultCode, args, Locale.US);
    return messageSource.getMessage(code, args, defaultMessage, locale);
  }

  private String getWelcomeTeacherBody(String displayName, String username, boolean socialAccount,
      Locale locale, HttpServletRequest request) {
    String gettingStartedUrl = getGettingStartedUrl(request);
    String code = socialAccount ? this.welcomeSocialAccountBodyCode : this.welcomeBodyCode;
    Object[] args = socialAccount 
      ? new Object[] { displayName, gettingStartedUrl } 
      : new Object[] { displayName, username, gettingStartedUrl };
    return getEmailMessage(this.welcomeBodyCode, code, args, locale);
  }

  private String getGettingStartedUrl(HttpServletRequest request) {
    return ControllerUtil.getPortalUrlString(request) + "/help/getting-started";
  }

  private String getVerificationUrl(String verificationCode, HttpServletRequest request) {
    return String.format("%s/api/teacher/register/verify?code=%s", 
        ControllerUtil.getPortalUrlString(request), verificationCode);
  }

  private void sendEmail(String email, String subject, String body) {
    String fromEmail = appProperties.getProperty("portalemailaddress");
    String[] recipients = { email };
    try {
      this.postMail(recipients, subject, body, fromEmail);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }
}
