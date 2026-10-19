package org.wise.portal.service.teacherpresentation.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.dao.teacherpresentation.TeacherPresentationConfigDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.teacherpresentation.DiscussionItem;
import org.wise.portal.service.teacherpresentation.IdeasItem;
import org.wise.portal.service.teacherpresentation.IdeasResponse;
import org.wise.portal.service.teacherpresentation.TeacherPresentationConfigService;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.work.StudentWork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TeacherPresentationConfigServiceImpl implements TeacherPresentationConfigService {

  private static final int MAX_ID_LENGTH = 30;
  private static final int MAX_RESPONSE_TEXT_LENGTH = 2000;
  // items column is a MySQL text column (65535 bytes)
  private static final int MAX_SERIALIZED_ITEMS_LENGTH = 60000;
  private static final Set<String> NAMES_DISPLAY_OPTIONS = Set.of(
      TeacherPresentationConfig.NAMES_SHOW, TeacherPresentationConfig.NAMES_HIDE,
      TeacherPresentationConfig.NAMES_ANONYMIZE);
  private static final Set<String> TYPE_OPTIONS = Set.of(
      TeacherPresentationConfig.TYPE_DISCUSSION,
      TeacherPresentationConfig.TYPE_IDEAS);

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private TeacherPresentationConfigDao<TeacherPresentationConfig> teacherPresentationConfigDao;

  @Autowired
  private StudentWorkDao<StudentWork> studentWorkDao;

  @Override
  @Transactional(readOnly = true)
  public TeacherPresentationConfig getConfig(Run run, Group period, String nodeId,
      String componentId) {
    return teacherPresentationConfigDao.getConfig(run, period, nodeId, componentId);
  }

  @Override
  public List<?> getItems(TeacherPresentationConfig config) {
    if (config == null || config.getItems() == null) {
      return Collections.emptyList();
    }
    String type = config.getType();
    try {
      if (TeacherPresentationConfig.TYPE_IDEAS.equals(type)) {
        return objectMapper.readValue(config.getItems(),
            new TypeReference<List<IdeasItem>>() {
            });
      } else {
        return objectMapper.readValue(config.getItems(),
            new TypeReference<List<DiscussionItem>>() {
            });
      }
    } catch (JsonProcessingException e) {
      return Collections.emptyList();
    }
  }

  @Override
  public List<Integer> getStudentWorkIds(TeacherPresentationConfig config) {
    List<?> items = getItems(config);
    List<Integer> ids = new ArrayList<>();
    for (Object item : items) {
      if (item instanceof DiscussionItem di) {
        if (di.studentWorkId() != null) {
          ids.add(di.studentWorkId());
        }
      } else if (item instanceof IdeasItem ii) {
        for (IdeasResponse response : ii.responses()) {
          if (response.studentWorkId() != null) {
            ids.add(response.studentWorkId());
          }
        }
      }
    }
    return ids;
  }

  @Override
  @Transactional
  public TeacherPresentationConfig saveConfig(Run run, Group period, String nodeId,
      String componentId, String componentType, String type, List<?> items,
      String studentNamesDisplay, Boolean showComponentPrompt, String prompt,
      Workgroup teacherWorkgroup) {
    validateComponent(nodeId, componentId, componentType);
    String resolvedType = type != null ? type : TeacherPresentationConfig.TYPE_DISCUSSION;
    if (!TYPE_OPTIONS.contains(resolvedType)) {
      throw new IllegalArgumentException("Invalid type: " + type);
    }
    if (studentNamesDisplay != null && !NAMES_DISPLAY_OPTIONS.contains(studentNamesDisplay)) {
      throw new IllegalArgumentException("Invalid studentNamesDisplay: " + studentNamesDisplay);
    }
    String normalizedPrompt = normalizePrompt(prompt);

    String serializedItems;
    if (TeacherPresentationConfig.TYPE_IDEAS.equals(resolvedType)) {
      List<IdeasItem> ideasItems = validateAndNormalizeIdeasItems(run, period, items);
      serializedItems = serializeIdeasItems(ideasItems);
      if (serializedItems.getBytes(java.nio.charset.StandardCharsets.UTF_8).length
          > MAX_SERIALIZED_ITEMS_LENGTH) {
        throw new IllegalArgumentException("Too many selected responses");
      }
    } else {
      List<DiscussionItem> discussionItems = validateAndNormalizeDiscussionItems(run, period, items);
      serializedItems = serializeDiscussionItems(discussionItems);
    }

    TeacherPresentationConfig config = getOrCreateConfig(run, period, nodeId, componentId,
        componentType, resolvedType);
    config.setComponentType(componentType);
    config.setType(resolvedType);
    config.setItems(serializedItems);
    if (studentNamesDisplay != null) {
      config.setStudentNamesDisplay(studentNamesDisplay);
    }
    if (showComponentPrompt != null) {
      config.setShowComponentPrompt(showComponentPrompt);
    }
    config.setPrompt(normalizedPrompt);
    config.setUpdatedByWorkgroup(teacherWorkgroup);
    config.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    teacherPresentationConfigDao.save(config);
    return config;
  }

  private TeacherPresentationConfig getOrCreateConfig(Run run, Group period, String nodeId,
      String componentId, String componentType, String type) {
    TeacherPresentationConfig config = teacherPresentationConfigDao.getConfig(run, period, nodeId,
        componentId);
    if (config == null) {
      Timestamp now = new Timestamp(System.currentTimeMillis());
      config = new TeacherPresentationConfig();
      config.setRun(run);
      config.setPeriod(period);
      config.setNodeId(nodeId);
      config.setComponentId(componentId);
      config.setComponentType(componentType);
      config.setType(type);
      config.setCreatedAt(now);
      config.setUpdatedAt(now);
    }
    return config;
  }

  private void validateComponent(String nodeId, String componentId, String componentType) {
    if (nodeId == null || nodeId.isBlank() || nodeId.length() > MAX_ID_LENGTH) {
      throw new IllegalArgumentException("Invalid nodeId");
    }
    if (componentId == null || componentId.isBlank() || componentId.length() > MAX_ID_LENGTH) {
      throw new IllegalArgumentException("Invalid componentId");
    }
    if (componentType == null || componentType.isBlank()
        || componentType.length() > MAX_ID_LENGTH) {
      throw new IllegalArgumentException("Invalid componentType");
    }
  }

  private String normalizePrompt(String prompt) {
    if (prompt == null || prompt.isBlank()) {
      return null;
    }
    if (prompt.length() > MAX_PROMPT_LENGTH) {
      throw new IllegalArgumentException("prompt is too long");
    }
    return prompt;
  }

  private List<DiscussionItem> validateAndNormalizeDiscussionItems(Run run, Group period, List<?> items) {
    List<Integer> ids = new ArrayList<>();
    if (items != null) {
      for (Object obj : items) {
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

  private List<IdeasItem> validateAndNormalizeIdeasItems(Run run, Group period, List<?> items) {
    if (items == null) {
      return Collections.emptyList();
    }
    if (items.size() > MAX_ITEMS) {
      throw new IllegalArgumentException("Too many items. The maximum is " + MAX_ITEMS);
    }
    List<IdeasItem> normalized = new ArrayList<>();
    List<Integer> allStudentWorkIds = new ArrayList<>();
    int responseCount = 0;
    for (Object obj : items) {
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
    if (responseCount > MAX_ITEMS) {
      throw new IllegalArgumentException("Too many responses. The maximum is " + MAX_ITEMS);
    }
    validateStudentWorkIds(run, period, allStudentWorkIds);
    return normalized;
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

  /**
   * Each student work must belong to the run and period. It is not required to belong to the
   * component, because discussion posts can come from connected components.
   */
  private Set<Integer> validateStudentWorkIds(Run run, Group period, List<Integer> ids) {
    Set<Integer> uniqueIds = new LinkedHashSet<>();
    if (ids != null) {
      uniqueIds.addAll(ids);
    }
    if (uniqueIds.size() > MAX_ITEMS) {
      throw new IllegalArgumentException("Too many items. The maximum is " + MAX_ITEMS);
    }
    for (Integer id : uniqueIds) {
      if (id == null) {
        throw new IllegalArgumentException("Invalid studentWorkId");
      }
      StudentWork work;
      try {
        work = studentWorkDao.getById(id);
      } catch (ObjectNotFoundException e) {
        throw new IllegalArgumentException("Unknown studentWorkId: " + id);
      }
      if (work == null || !work.getRun().getId().equals(run.getId())
          || !work.getPeriod().getId().equals(period.getId())) {
        throw new IllegalArgumentException("Unknown studentWorkId: " + id);
      }
    }
    return uniqueIds;
  }

  private String serializeDiscussionItems(List<DiscussionItem> items) {
    try {
      return objectMapper.writeValueAsString(items);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private String serializeIdeasItems(List<IdeasItem> items) {
    try {
      return objectMapper.writeValueAsString(items);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
