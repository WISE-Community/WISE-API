package org.wise.portal.service.peergroupactivity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.service.run.RunService;

import lombok.Setter;

@Service
public class PeerGroupActivityDeserializer extends JsonDeserializer<PeerGroupActivityImpl> {

  @Autowired
  @Setter
  RunService runService;

  @Override
  public PeerGroupActivityImpl deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    ObjectCodec objectCodec = parser.getCodec();
    JsonNode node = objectCodec.readTree(parser);
    PeerGroupActivityImpl activity = new PeerGroupActivityImpl();
    if (node.has("id")) {
      activity.setId(node.get("id").asLong());
    }
    activity.setLogic(node.get("logic").asText());
    activity.setTag(node.get("tag").asText());
    activity.setMaxMembershipCount(node.get("maxMembershipCount").asInt());
    try {
      if (node.has("runId")) {
        activity.setRun(runService.retrieveById(node.get("runId").asLong()));
      }
    } catch (ObjectNotFoundException e) {
      e.printStackTrace();
    }
    return activity;
  }
}
