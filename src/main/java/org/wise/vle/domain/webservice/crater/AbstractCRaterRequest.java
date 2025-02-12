package org.wise.vle.domain.webservice.crater;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;

@Setter
public abstract class AbstractCRaterRequest implements CRaterRequest {
  String itemId;
  String cRaterClientId;

  @Getter
  String cRaterUrl;

  public String generateBodyData() throws JSONException {
    JSONObject body = new JSONObject();
    body.put("client_id", cRaterClientId);
    body.put("service", "ScoringService");
    body.put("item_id", itemId);
    return body.toString();
  }

  public boolean forBerkeleyEndpoint() {
    return itemId.substring(0, 9).equals("berkeley_");
  }
}
