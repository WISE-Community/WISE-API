package org.wise.vle.domain.work;

import java.io.IOException;
import java.sql.Timestamp;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.service.group.GroupService;
import org.wise.portal.service.peergroup.PeerGroupService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.workgroup.WorkgroupService;

import lombok.Setter;

@Service
public class StudentWorkDeserializer extends JsonDeserializer<StudentWork> {

  @Autowired
  @Setter
  RunService runService;

  @Autowired
  @Setter
  GroupService groupService;

  @Autowired
  @Setter
  WorkgroupService workgroupService;

  @Autowired
  @Setter
  PeerGroupService peerGroupService;

  @Override
  public StudentWork deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    ObjectCodec objectCodec = parser.getCodec();
    JsonNode node = objectCodec.readTree(parser);
    StudentWork studentWork = new StudentWork();
    if (node.has("id")) {
      studentWork.setId(node.get("id").asInt());
    }
    studentWork.setNodeId(node.get("nodeId").asText());
    studentWork.setComponentId(node.get("componentId").asText());
    studentWork.setComponentType(node.get("componentType").asText());
    studentWork.setIsAutoSave(node.get("isAutoSave") != null ? node.get("isAutoSave").asBoolean() : false);
    studentWork.setIsSubmit(node.get("isSubmit") != null ? node.get("isSubmit").asBoolean() : false);
    studentWork.setClientSaveTime(new Timestamp(node.get("clientSaveTime").asLong()));
    if (node.has("serverSaveTime")) {
      studentWork.setServerSaveTime(new Timestamp(node.get("serverSaveTime").asLong()));
    }
    studentWork.setStudentData(node.get("studentData").toString());
    try {
      studentWork.setRun(runService.retrieveById(node.get("runId").asLong()));
      studentWork.setPeriod(groupService.retrieveById(node.get("periodId").asLong()));
      studentWork
          .setWorkgroup(workgroupService.retrieveById(node.get("workgroupId").asLong()));
      if (node.has("peerGroupId")) {
        studentWork.setPeerGroup(peerGroupService.getById(node.get("peerGroupId").asLong()));
      }
    } catch (ObjectNotFoundException e) {
      e.printStackTrace();
    }
    return studentWork;
  }



}
