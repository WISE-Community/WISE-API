package org.wise.vle.domain.webservice.crater;

public class CRaterVerificationResponse {

  String response;

  public CRaterVerificationResponse(String response) {
    this.response = response;
  }

  public boolean isVerified() {
    return this.response.matches("(.*)avail=\"Y\"(.*)");
  }
}
