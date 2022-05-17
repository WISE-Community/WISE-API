package org.wise.portal.service.peergrouping;

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
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.service.run.RunService;

import lombok.Setter;

@Service
public class PeerGroupingDeserializer extends JsonDeserializer<PeerGroupingImpl> {

  @Autowired
  @Setter
  RunService runService;

  @Override
  public PeerGroupingImpl deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    ObjectCodec objectCodec = parser.getCodec();
    JsonNode node = objectCodec.readTree(parser);
    PeerGroupingImpl peerGrouping = new PeerGroupingImpl();
    if (node.has("id")) {
      peerGrouping.setId(node.get("id").asLong());
    }
    peerGrouping.setLogic(node.get("logic").asText());
    peerGrouping.setTag(node.get("tag").asText());
    peerGrouping.setMaxMembershipCount(node.get("maxMembershipCount").asInt());
    try {
      if (node.has("runId")) {
        peerGrouping.setRun(runService.retrieveById(node.get("runId").asLong()));
      }
    } catch (ObjectNotFoundException e) {
      e.printStackTrace();
    }
    return peerGrouping;
  }
}
