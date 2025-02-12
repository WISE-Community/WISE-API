package org.wise.vle.domain.webservice.crater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wise.portal.presentation.util.http.Base64;

import lombok.Setter;

@Setter
public class CRaterScoringRequest extends AbstractCRaterRequest {
  String responseId;
  String responseText;

  public String generateBodyData() throws JSONException {
    JSONObject body = new JSONObject(super.generateBodyData());
    JSONArray responses = new JSONArray();
    JSONObject response = new JSONObject();
    response.put("response_id", responseId);
    response.put("response_text", Base64.encodeBytes(responseText.getBytes()));
    responses.put(response);
    body.put("responses", responses);
    return body.toString();
  }
}
