package org.wise.portal.service.peergroupactivity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.stereotype.Service;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;

@Service
public class PeerGroupActivitySerializer extends JsonSerializer<PeerGroupActivityImpl> {

  @Override
  public void serialize(PeerGroupActivityImpl activity, JsonGenerator gen,
      SerializerProvider serializers) throws IOException {
    gen.writeStartObject();
    gen.writeObjectField("id", activity.getId());
    gen.writeObjectField("runId", activity.getRun().getId());
    gen.writeObjectField("logic", activity.getLogic());
    gen.writeObjectField("tag", activity.getTag());
    gen.writeObjectField("maxMembershipCount", activity.getMaxMembershipCount());
    gen.writeEndObject();
  }
}
