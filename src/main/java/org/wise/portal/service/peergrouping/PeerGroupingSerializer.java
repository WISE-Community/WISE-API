package org.wise.portal.service.peergrouping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.stereotype.Service;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;

@Service
public class PeerGroupingSerializer extends JsonSerializer<PeerGroupingImpl> {

  @Override
  public void serialize(PeerGroupingImpl peerGrouping, JsonGenerator gen,
      SerializerProvider serializers) throws IOException {
    gen.writeStartObject();
    gen.writeObjectField("id", peerGrouping.getId());
    gen.writeObjectField("runId", peerGrouping.getRun().getId());
    gen.writeObjectField("logic", peerGrouping.getLogic());
    gen.writeObjectField("tag", peerGrouping.getTag());
    gen.writeObjectField("maxMembershipCount", peerGrouping.getMaxMembershipCount());
    gen.writeEndObject();
  }
}
