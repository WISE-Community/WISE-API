package org.wise.vle.domain.webservice.crater;

import org.json.JSONException;

public interface CRaterRequest {

  String getCRaterUrl();

  String generateBodyData() throws JSONException;
}
