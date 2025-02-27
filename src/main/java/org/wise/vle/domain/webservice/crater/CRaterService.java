/**
 * Copyright (c) 2008-2021 Regents of the University of California (Regents).
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
package org.wise.vle.domain.webservice.crater;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Controller for using the CRater scoring servlet via HTTP
 *
 * @author Hiroki Terashima
 * @author Geoffrey Kwan
 */
@Service
public class CRaterService {

  @Autowired
  private Environment appProperties;

  /**
   * Sends either student work (scoring request) or an item id (verification request) to 
   * the CRater server
   * @param request the scoring or verification request from the client
   * @return scoring or verify response from CRater
   * @throws JSONException
   */
  public String getCRaterResponse(CRaterRequest request) 
  throws JSONException {
    String prefix = request.forBerkeleyEndpoint() ? "berkeley_" : "";
    
    String clientIdVariable = prefix + "cRater_client_id";
    String cRaterUrlVariable = prefix + request.getCRaterUrlVariableBase();
    
    request.setCRaterClientId(appProperties.getProperty(clientIdVariable));
    request.setCRaterUrl(appProperties.getProperty(cRaterUrlVariable));

    return post(request);
  }

  /**
   * POSTs a CRater Request to the CRater Servlet and returns the CRater response string
   *
   * @param CRaterRequest request to send to CRater
   * @return the response string from the CRater server
   */
  private String post(CRaterRequest request) throws JSONException {
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(request.getCRaterUrl());
    try {
      String password = appProperties.getProperty(
          request.forBerkeleyEndpoint() ? "berkeley_cRater_password" : "cRater_password");
      String authHeader = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(
          ("extsyscrtr02dev:" + password).getBytes());
      post.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
      post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
      post.setEntity(new StringEntity(request.generateBodyData(), ContentType.APPLICATION_JSON));
      HttpResponse response = client.execute(post);
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + response.getStatusLine());
      }
      return IOUtils.toString(response.getEntity().getContent());
    } catch (IOException e) {
      System.err.println("Fatal transport error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      post.releaseConnection();
    }
    return null;
  }
}
