package org.wise.portal.presentation.web.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.wise.portal.domain.authentication.MutableUserDetails;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.util.http.Base64;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;

@Controller
public class CkBoardSsoController {
  public static final String STUDENT = "student";
  public static final String TEACHER = "teacher";

  @Autowired
  Environment appProperties;

  @Autowired
  UserService userService;

  @Autowired
  WorkgroupService workgroupService;

  String ckBoardUrl;
  String hashAlgorithm = "HmacSHA256";
  String secretKey;

  @PostConstruct
  public void init() {
    ckBoardUrl = appProperties.getProperty("ck_board_url");
    secretKey = appProperties.getProperty("ck_board_sso_secret_key");
  }

  @GetMapping("/sso/ckboard")
  protected RedirectView ckBoardSsoLogin(@RequestParam("sso") String payload,
      @RequestParam("sig") String sig, @RequestParam("redirectUrl") String redirectUrl,
      Authentication auth) throws IOException, UnsupportedEncodingException {
    String nonce = getNonce(payload);
    if (isCkBoardAvailable() && isValidHash(payload, sig) && nonce != null) {
      User user = userService.retrieveUserByUsername(auth.getName());
      return new RedirectView(generateCkBoardSsoLoginUrl(nonce, user, redirectUrl));
    } else {
      return null;
    }
  }

  private String getNonce(String payloadEncoded) throws IOException, UnsupportedEncodingException {
    String payload = new String(Base64.decode(payloadEncoded), "UTF-8");
    if (payload.startsWith("nonce=")) {
      return payload.substring(6);
    }
    return null;
  }

  private boolean isCkBoardAvailable() {
    return secretKey != null && !secretKey.isEmpty() && ckBoardUrl != null && !ckBoardUrl.isEmpty();
  }

  private boolean isValidHash(String payload, String sig) {
    return hmacDigest(payload, secretKey, hashAlgorithm).equals(sig);
  }

  private String generateCkBoardSsoLoginUrl(String nonce, User user, String redirectUrl)
      throws UnsupportedEncodingException {
    String payload = "";
    if (user.isStudent()) {
      payload = generateStudentPayload(nonce, user, redirectUrl);
    } else if (user.isTeacher()) {
      payload = generateTeacherPayload(nonce, user, redirectUrl);
    }
    String payloadBase64 = Base64.encodeBytes(payload.getBytes());
    String payloadBase64UrlEncoded = URLEncoder.encode(payloadBase64, "UTF-8");
    String payloadBase64Hashed = hmacDigest(payloadBase64, secretKey, hashAlgorithm);
    return ckBoardUrl + "/sso/login/" + payloadBase64UrlEncoded + "/" + payloadBase64Hashed;
  }

  private String generateTeacherPayload(String nonce, User user, String redirectUrl)
      throws UnsupportedEncodingException {
    MutableUserDetails userDetails = user.getUserDetails();
    Long userId = user.getId();
    String username = URLEncoder.encode(userDetails.getUsername(), "UTF-8");
    return generatePayload(nonce, userId, username, TEACHER, redirectUrl);
  }

  private String generateStudentPayload(String nonce, User user, String redirectUrl)
      throws UnsupportedEncodingException {
    Long userId = user.getId();
    String username = user.getUserDetails().getCoreUsername();
    return generatePayload(nonce, userId, username, STUDENT, redirectUrl);
  }

  private String generatePayload(String nonce, Long userId, String username, String role,
      String redirectUrl) {
    StringBuilder payloadBuffer = new StringBuilder();
    payloadBuffer.append("nonce=" + nonce + "&");
    payloadBuffer.append("user-id=" + userId + "&");
    payloadBuffer.append("username=" + username + "&");
    payloadBuffer.append("role=" + role + "&");
    payloadBuffer.append("redirect-url=" + redirectUrl);
    return payloadBuffer.toString();
  }

  public static String hmacDigest(String msg, String secretKey, String algorithm) {
    String digest = null;
    try {
      SecretKeySpec key = new SecretKeySpec((secretKey).getBytes("UTF-8"), algorithm);
      Mac mac = Mac.getInstance(algorithm);
      mac.init(key);
      byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));
      StringBuffer hash = new StringBuffer();
      for (int i = 0; i < bytes.length; i++) {
        String hex = Integer.toHexString(0xFF & bytes[i]);
        if (hex.length() == 1) {
          hash.append('0');
        }
        hash.append(hex);
      }
      digest = hash.toString();
    } catch (UnsupportedEncodingException e) {
    } catch (InvalidKeyException e) {
    } catch (NoSuchAlgorithmException e) {
    }
    return digest;
  }
}
