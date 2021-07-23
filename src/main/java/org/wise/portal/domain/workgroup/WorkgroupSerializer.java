package org.wise.portal.domain.workgroup;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class WorkgroupSerializer extends JsonSerializer<Workgroup> {

  @Override
  public void serialize(Workgroup workgroup, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeStartObject();
    gen.writeObjectField("id", workgroup.getId());
    gen.writeObjectField("periodId", workgroup.getPeriod().getId());
    gen.writeEndObject();
  }
}
