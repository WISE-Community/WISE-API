package org.wise.portal.service.peergroupactivity;

import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;

@Service
public class PeerGroupActivityJsonModule extends SimpleModule {

  private static final long serialVersionUID = 1L;

  public PeerGroupActivityJsonModule() {}

  @Autowired
  public PeerGroupActivityJsonModule(PeerGroupActivitySerializer serializer,
      PeerGroupActivityDeserializer deserializer) {
    this.addSerializer(PeerGroupActivityImpl.class, serializer);
    this.addDeserializer(PeerGroupActivityImpl.class, deserializer);
  }
}
