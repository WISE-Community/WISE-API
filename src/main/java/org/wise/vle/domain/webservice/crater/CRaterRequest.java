package org.wise.vle.domain.webservice.crater;

import org.json.JSONException;

public interface CRaterRequest {

  String getCRaterUrl();

  void setCRaterUrl(String cRaterUrl);

  String generateBodyData() throws JSONException;

  boolean forBerkeleyEndpoint();
}
