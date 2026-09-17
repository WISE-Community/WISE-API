package org.wise.portal.service.peergrouping;

import java.io.Serial;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;

@Service
public class PeerGroupingJsonModule extends SimpleModule {

  @Serial
  private static final long serialVersionUID = 1L;

  public PeerGroupingJsonModule() {
  }

  @Autowired
  public PeerGroupingJsonModule(PeerGroupingSerializer serializer,
      PeerGroupingDeserializer deserializer) {
    this.addSerializer(PeerGroupingImpl.class, serializer);
    this.addDeserializer(PeerGroupingImpl.class, deserializer);
  }
}
