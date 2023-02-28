package org.wise.portal.service.session.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.service.session.SessionService;

@Service
public class SessionServiceImpl<S extends Session> implements SessionService {

  @Autowired
  private Environment appProperties;

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  public void addSignedInUser(UserDetails userDetails) {
    stringRedisTemplate.opsForSet().add("signedInUsers", userDetails.getUsername());
    if (userDetails instanceof StudentUserDetails) {
      stringRedisTemplate.opsForSet().add("signedInStudents", userDetails.getUsername());
    } else if (userDetails instanceof TeacherUserDetails) {
      stringRedisTemplate.opsForSet().add("signedInTeachers", userDetails.getUsername());
    }
  }

  @Override
  public Set<String> getLoggedInStudents() {
    return stringRedisTemplate.opsForSet().members("signedInStudents");
  }

  @Override
  public Set<String> getLoggedInTeachers() {
    return stringRedisTemplate.opsForSet().members("signedInTeachers");
  }

  public void removeSignedInUser(UserDetails userDetails) {
    String username = userDetails.getUsername();
    stringRedisTemplate.opsForSet().remove("signedInUsers", username);
    if (userDetails instanceof StudentUserDetails) {
      stringRedisTemplate.opsForSet().remove("signedInStudents", username);
    } else if (userDetails instanceof TeacherUserDetails) {
      stringRedisTemplate.opsForSet().remove("signedInTeachers", username);
    }
  }

  @Override
  public Set<String> getCurrentlyAuthoredProjects() {
    return stringRedisTemplate.opsForSet().members("currentlyAuthoredProjects");
  }

  public int getNumberSignedInUsers() {
    return stringRedisTemplate.opsForSet().members("signedInUsers").size();
  }

  public void addCurrentAuthor(Serializable projectId, String authorUsername) {
    stringRedisTemplate.opsForSet().add("currentlyAuthoredProjects", projectId.toString());
    stringRedisTemplate.opsForSet().add("currentAuthors:" + projectId, authorUsername);
  }

  @Override
  public void removeCurrentAuthor(UserDetails author) {
    Set<String> currentlyAuthoredProjects = stringRedisTemplate.opsForSet()
        .members("currentlyAuthoredProjects");
    for (String projectId : currentlyAuthoredProjects) {
      removeCurrentAuthor(projectId, author.getUsername());
    }
  }

  public void removeCurrentAuthor(Serializable projectId, String authorUsername) {
    stringRedisTemplate.opsForSet().remove("currentAuthors:" + projectId, authorUsername);
    Long numCurrentAuthorsForProject = stringRedisTemplate.opsForSet()
        .size("currentAuthors:" + projectId);
    if (numCurrentAuthorsForProject == 0) {
      stringRedisTemplate.opsForSet().remove("currentlyAuthoredProjects", projectId.toString());
    }
  }

  @Override
  public void removeUser(UserDetails user) {
    removeSignedInUser(user);
    if (user instanceof TeacherUserDetails) {
      removeCurrentAuthor(user);
    }
  }

  public Set<String> getCurrentAuthors(Serializable projectId) {
    return stringRedisTemplate.opsForSet().members("currentAuthors:" + projectId);
  }

  public boolean isCkBoardAvailable() {
    String ckBoardUrl = appProperties.getProperty("ck_board_url");
    return ckBoardUrl != null && !ckBoardUrl.equals("");
  }

  public void signOutOfCkBoard(HttpServletRequest request) {
    String ckSessionCookie = getCkSessionCookie(request);
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost ckBoardLogoutRequest = new HttpPost(getCkBoardLogoutUrl());
    ckBoardLogoutRequest.setHeader("Authorization", "Bearer " + ckSessionCookie);
    try {
      client.execute(ckBoardLogoutRequest);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String getCkBoardLogoutUrl() {
    String ckBoardUrl = appProperties.getProperty("ck_board_url");

    // The CK Board local backend url is only used for local development and should only be set in
    // local development environments. When we are running locally, we need the local IP and port of
    // the CK Board backend because the SCORE API is served using Docker. If the SCORE API makes a
    // request to localhost:8001, it won't be able to access the CK Board backend. This is because
    // the SCORE API expects localhost to be within the container but the CK Board backend is not in
    // the container.
    String ckBoardLocalBackendUrl = appProperties.getProperty("ck_board_local_backend_url");
    if (ckBoardLocalBackendUrl != null && !ckBoardLocalBackendUrl.equals("")) {
      ckBoardUrl = ckBoardLocalBackendUrl;
    }
    return ckBoardUrl + "/api/auth/logout";
  }

  private String getCkSessionCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals("CK_SESSION")) {
        return cookie.getValue();
      }
    }
    return null;
  }

}
