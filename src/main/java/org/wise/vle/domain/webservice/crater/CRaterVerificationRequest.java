package org.wise.vle.domain.webservice.crater;

import org.json.JSONException;
import org.json.JSONObject;

public class CRaterVerificationRequest extends AbstractCRaterRequest {
  public String generateBodyData() throws JSONException {
      JSONObject body = new JSONObject(super.generateBodyData());
      body.put("service", "VerificationService");
      return body.toString();
  }
  
  public String getCRaterUrlVariableBase() {
    return "cRater_verification_url";
  }
}
