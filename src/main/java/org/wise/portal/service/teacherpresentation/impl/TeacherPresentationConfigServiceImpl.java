package org.wise.portal.service.teacherpresentation.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.wise.portal.service.teacherpresentation.TeacherPresentationConfigService;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.work.StudentWork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TeacherPresentationConfigServiceImpl implements TeacherPresentationConfigService {

  private static final int MAX_ID_LENGTH = 30;
  private static final Set<String> NAMES_DISPLAY_OPTIONS = Set.of(
      TeacherPresentationConfig.NAMES_SHOW, TeacherPresentationConfig.NAMES_HIDE,
      TeacherPresentationConfig.NAMES_ANONYMIZE);

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
  public List<Integer> getStudentWorkIds(TeacherPresentationConfig config) {
    try {
      List<Map<String, Integer>> items = objectMapper.readValue(config.getItems(),
          new TypeReference<List<Map<String, Integer>>>() {
          });
      List<Integer> ids = new ArrayList<>();
      for (Map<String, Integer> item : items) {
        ids.add(item.get("studentWorkId"));
      }
      return ids;
    } catch (JsonProcessingException e) {
      return new ArrayList<>();
    }
  }

  @Override
  @Transactional
  public TeacherPresentationConfig saveConfig(Run run, Group period, String nodeId,
      String componentId, String componentType, List<Integer> studentWorkIds,
      String studentNamesDisplay, String prompt, Workgroup teacherWorkgroup) {
    validateComponent(nodeId, componentId, componentType);
    if (studentNamesDisplay != null && !NAMES_DISPLAY_OPTIONS.contains(studentNamesDisplay)) {
      throw new IllegalArgumentException("Invalid studentNamesDisplay: " + studentNamesDisplay);
    }
    String normalizedPrompt = normalizePrompt(prompt);
    Set<Integer> uniqueIds = validateStudentWorkIds(run, period, studentWorkIds);
    TeacherPresentationConfig config = getOrCreateConfig(run, period, nodeId, componentId,
        componentType);
    config.setItems(serializeItems(uniqueIds));
    if (studentNamesDisplay != null) {
      config.setStudentNamesDisplay(studentNamesDisplay);
    }
    config.setPrompt(normalizedPrompt);
    config.setUpdatedByWorkgroup(teacherWorkgroup);
    config.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    teacherPresentationConfigDao.save(config);
    return config;
  }

  private TeacherPresentationConfig getOrCreateConfig(Run run, Group period, String nodeId,
      String componentId, String componentType) {
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

  private String serializeItems(Set<Integer> studentWorkIds) {
    List<Map<String, Integer>> items = new ArrayList<>();
    for (Integer id : studentWorkIds) {
      items.add(Map.of("studentWorkId", id));
    }
    try {
      return objectMapper.writeValueAsString(items);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
