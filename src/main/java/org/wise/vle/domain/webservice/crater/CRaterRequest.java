package org.wise.vle.domain.webservice.crater;

import org.json.JSONException;

public interface CRaterRequest {

  String getCRaterUrl();

  String getCRaterUrlVariableBase();

  void setCRaterUrl(String cRaterUrl);

  void setCRaterClientId(String cRaterClientId);

  String generateBodyData() throws JSONException;

  boolean forBerkeleyEndpoint();
}
