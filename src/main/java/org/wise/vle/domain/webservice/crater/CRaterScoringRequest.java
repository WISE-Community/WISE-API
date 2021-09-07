package org.wise.vle.domain.webservice.crater;

import lombok.Getter;
import lombok.Setter;

@Setter
public class CRaterScoringRequest implements CRaterRequest {
  String itemId;
  String responseId;
  String responseText;
  String cRaterClientId;

  @Getter
  String cRaterUrl;

  public String generateBodyData() {
    return "<crater-request includeRNS='N'><client id='" + cRaterClientId + "'/>" +
        "<items><item id='" + itemId + "'><responses>" +
        "<response id='" + responseId + "'><![CDATA[" + responseText + "]]></response>" +
        "</responses></item></items></crater-request>";
  }
}
