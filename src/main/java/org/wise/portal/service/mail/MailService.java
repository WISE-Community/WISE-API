/**
 * Copyright (c) 2007-2019 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.service.mail;

import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.presentation.web.controllers.ControllerUtil;

/**
 * Compose and send email using the JavaMail Framework
 * @author Anthony Perritano
 */
@Service
public class MailService implements IMailFacade {

  private final String emailCodePrefix = 
    "presentation.web.controllers.teacher.registerTeacherController.";
  private final String verifyBodyCode = this.emailCodePrefix + "verifyTeacherEmailBody";
  private final String verifySubjectCode = this.emailCodePrefix + "verifyTeacherEmailSubject";
  private final String welcomeBodyCode = this.emailCodePrefix + "welcomeTeacherEmailBody";
  private final String welcomeSocialAccountBodyCode = this.emailCodePrefix + "welcomeTeacherEmailBodyNoUsername";
  private final String welcomeSubjectCode = this.emailCodePrefix + "welcomeTeacherEmailSubject";

  @Autowired
  protected Environment appProperties;

  @Autowired
  private JavaMailSender javaMailSender;

  @Autowired
  private MessageSource messageSource;

  public void postMail(String[] recipients, String subject, String message, String from)
      throws MessagingException {
    postMail(recipients, subject, message, from, null);
  }

  public void postMail(String[] recipients, String subject, String message,
      String from, String[] cc) throws MessagingException {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
    helper.setFrom(from);
    helper.setText(message);
    helper.setSubject(subject);

    if (cc != null) {
      helper.setCc(cc);
    }

    for (String receiver : recipients) {
      if (receiver != null) {
        helper.setTo(receiver);
        javaMailSender.send(mimeMessage);
      }
    }
  }

  public void sendWelcomeTeacherEmail(String email, String displayName, String username,
      boolean socialAccount, Locale locale, HttpServletRequest request) {
    if (isSendEmailEnabled()) {
      String subject = getEmailMessage(this.welcomeSubjectCode, this.welcomeSubjectCode, null, locale);
      String body = getWelcomeTeacherBody(displayName, username, socialAccount, locale, request);
      this.sendEmail(email, subject, body);
    }
  }

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
