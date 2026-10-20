package org.wise.portal.service.teacherpresentation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.teacherpresentation.DiscussionItem;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.work.StudentWork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DiscussionPresentationItemStrategy
    extends AbstractTeacherPresentationItemStrategy<DiscussionItem> {

  @Autowired
  public DiscussionPresentationItemStrategy(StudentWorkDao<StudentWork> studentWorkDao) {
    super(studentWorkDao);
  }

  @Override
  public String getType() {
    return TeacherPresentationConfig.TYPE_DISCUSSION;
  }

  @Override
  public List<DiscussionItem> parseItems(String json, ObjectMapper objectMapper) {
    if (json == null || json.isBlank()) {
      return Collections.emptyList();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<List<DiscussionItem>>() {
      });
    } catch (JsonProcessingException e) {
      return Collections.emptyList();
    }
  }

  @Override
  public String serializeItems(List<DiscussionItem> items, ObjectMapper objectMapper) {
    try {
      return objectMapper.writeValueAsString(items);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public List<DiscussionItem> validateAndNormalize(Run run, Group period, List<?> rawItems) {
    List<Integer> ids = new ArrayList<>();
    if (rawItems != null) {
      for (Object obj : rawItems) {
        if (obj instanceof DiscussionItem di) {
          ids.add(di.studentWorkId());
        } else if (obj instanceof Integer id) {
          ids.add(id);
        } else if (obj != null) {
          throw new IllegalArgumentException("Invalid discussion item type: " + obj.getClass());
        }
      }
    }
    Set<Integer> uniqueIds = validateStudentWorkIds(run, period, ids);
    List<DiscussionItem> normalized = new ArrayList<>();
    for (Integer id : uniqueIds) {
      normalized.add(new DiscussionItem(id));
    }
    return normalized;
  }

  @Override
  public List<Integer> extractStudentWorkIds(List<DiscussionItem> items) {
    List<Integer> ids = new ArrayList<>();
    if (items != null) {
      for (DiscussionItem item : items) {
        if (item != null && item.studentWorkId() != null) {
          ids.add(item.studentWorkId());
        }
      }
    }
    return ids;
  }
}
