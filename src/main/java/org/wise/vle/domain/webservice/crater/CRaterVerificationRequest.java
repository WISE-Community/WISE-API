package org.wise.vle.domain.webservice.crater;

import lombok.Getter;
import lombok.Setter;

@Setter
public class CRaterVerificationRequest implements CRaterRequest {
  String itemId;
  String cRaterClientId;

  @Getter
  String cRaterUrl;

  public String generateBodyData() {
    return "<crater-verify><client id='" + cRaterClientId + "'/>" +
        "<items><item id='" + itemId + "'/></items>" +
        "</crater-verify>";
  }
}
