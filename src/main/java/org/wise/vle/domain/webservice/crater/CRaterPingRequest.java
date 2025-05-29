package org.wise.vle.domain.webservice.crater;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.Setter;

@Setter
public class CRaterPingRequest extends AbstractCRaterRequest {
	public String generateBodyData() throws JSONException {
		JSONObject body = new JSONObject(super.generateBodyData());
		body.put("service", "LoadService");
		return body.toString();
	}

	@Override
	public String getCRaterUrlVariableBase() {
		return "cRater_scoring_url";
	}

	public String getItemId() {
		return this.itemId;
	}
}
