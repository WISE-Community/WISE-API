package org.wise.portal.service.teacherpresentation.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.teacherpresentation.IdeasItem;
import org.wise.portal.service.teacherpresentation.IdeasResponse;
import org.wise.portal.service.teacherpresentation.TeacherPresentationConfigService;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.work.StudentWork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class IdeasPresentationItemStrategy
    extends AbstractTeacherPresentationItemStrategy<IdeasItem> {

  private static final int MAX_RESPONSE_TEXT_LENGTH = 2000;
  // items column is a MySQL text column (65535 bytes)
  private static final int MAX_SERIALIZED_ITEMS_LENGTH = 60000;

  @Autowired
  public IdeasPresentationItemStrategy(StudentWorkDao<StudentWork> studentWorkDao) {
    super(studentWorkDao);
  }

  @Override
  public String getType() {
    return TeacherPresentationConfig.TYPE_IDEAS;
  }

  @Override
  public List<IdeasItem> parseItems(String json, ObjectMapper objectMapper) {
    if (json == null || json.isBlank()) {
      return Collections.emptyList();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<List<IdeasItem>>() {
      });
    } catch (JsonProcessingException e) {
      return Collections.emptyList();
    }
  }

  @Override
  public String serializeItems(List<IdeasItem> items, ObjectMapper objectMapper) {
    try {
      String serialized = objectMapper.writeValueAsString(items);
      if (serialized.getBytes(StandardCharsets.UTF_8).length > MAX_SERIALIZED_ITEMS_LENGTH) {
        throw new IllegalArgumentException("Too many selected responses");
      }
      return serialized;
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public List<IdeasItem> validateAndNormalize(Run run, Group period, List<?> rawItems) {
    if (rawItems == null) {
      return Collections.emptyList();
    }
    if (rawItems.size() > TeacherPresentationConfigService.MAX_ITEMS) {
      throw new IllegalArgumentException("Too many items. The maximum is "
          + TeacherPresentationConfigService.MAX_ITEMS);
    }
    List<IdeasItem> normalized = new ArrayList<>();
    List<Integer> allStudentWorkIds = new ArrayList<>();
    int responseCount = 0;
    for (Object obj : rawItems) {
      if (obj instanceof IdeasItem ii) {
        String ideaId = ii.ideaId();
        if (ideaId != null) {
          ideaId = ideaId.trim();
          if (ideaId.isEmpty() || ideaId.length() > 100) {
            throw new IllegalArgumentException("Invalid ideaId: " + ideaId);
          }
        }
        if (ideaId == null && ii.responses().isEmpty()) {
          throw new IllegalArgumentException("Item must specify ideaId or responses");
        }
        List<IdeasResponse> responses = new ArrayList<>();
        for (IdeasResponse response : ii.responses()) {
          responses.add(normalizeIdeasResponse(response));
          allStudentWorkIds.add(response.studentWorkId());
        }
        responseCount += responses.size();
        normalized.add(new IdeasItem(ideaId, responses));
      } else if (obj != null) {
        throw new IllegalArgumentException("Invalid ideas item type: " + obj.getClass());
      }
    }
    if (responseCount > TeacherPresentationConfigService.MAX_ITEMS) {
      throw new IllegalArgumentException("Too many responses. The maximum is "
          + TeacherPresentationConfigService.MAX_ITEMS);
    }
    validateStudentWorkIds(run, period, allStudentWorkIds);
    return normalized;
  }

  @Override
  public List<Integer> extractStudentWorkIds(List<IdeasItem> items) {
    List<Integer> ids = new ArrayList<>();
    if (items != null) {
      for (IdeasItem ii : items) {
        if (ii != null && ii.responses() != null) {
          for (IdeasResponse response : ii.responses()) {
            if (response != null && response.studentWorkId() != null) {
              ids.add(response.studentWorkId());
            }
          }
        }
      }
    }
    return ids;
  }

  private IdeasResponse normalizeIdeasResponse(IdeasResponse response) {
    if (response == null || response.studentWorkId() == null) {
      throw new IllegalArgumentException("Invalid studentWorkId");
    }
    if (response.workgroupId() == null) {
      throw new IllegalArgumentException("Invalid workgroupId");
    }
    String text = response.text() != null ? response.text() : "";
    if (text.length() > MAX_RESPONSE_TEXT_LENGTH) {
      text = text.substring(0, MAX_RESPONSE_TEXT_LENGTH);
    }
    return new IdeasResponse(response.studentWorkId(), response.workgroupId(),
        response.responseTimestamp(), text);
  }
}
