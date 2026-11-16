package org.wise.portal.service.teacherpresentation.impl;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.dao.teacherpresentation.TeacherPresentationConfigDao;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.teacherpresentation.TeacherPresentationConfigService;
import org.wise.portal.service.teacherpresentation.TeacherPresentationItemStrategy;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.work.StudentWork;

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

  private final Map<String, TeacherPresentationItemStrategy<?>> strategies = new HashMap<>();

  @Autowired
  public void setStrategies(List<TeacherPresentationItemStrategy<?>> strategyList) {
    this.strategies.clear();
    if (strategyList != null) {
      for (TeacherPresentationItemStrategy<?> strategy : strategyList) {
        this.strategies.put(strategy.getType(), strategy);
      }
    }
  }

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
    String resolvedType = resolveType(config.getType());
    TeacherPresentationItemStrategy<?> strategy = getStrategy(resolvedType);
    if (strategy == null) {
      return Collections.emptyList();
    }
    return strategy.parseItems(config.getItems(), objectMapper);
  }

  @Override
  public List<Integer> getStudentWorkIds(TeacherPresentationConfig config) {
    if (config == null || config.getItems() == null) {
      return Collections.emptyList();
    }
    String resolvedType = resolveType(config.getType());
    TeacherPresentationItemStrategy<?> strategy = getStrategy(resolvedType);
    if (strategy == null) {
      return Collections.emptyList();
    }
    return parseAndExtractStudentWorkIds(strategy, config.getItems());
  }

  @Override
  @Transactional
  public TeacherPresentationConfig saveConfig(Run run, Group period, String nodeId,
      String componentId, String componentType, String type, List<?> items,
      String studentNamesDisplay, Boolean showComponentPrompt, String prompt,
      Workgroup teacherWorkgroup) {
    validateComponent(nodeId, componentId, componentType);
    String resolvedType = resolveType(type);
    TeacherPresentationItemStrategy<?> strategy = getStrategy(resolvedType);
    if (strategy == null) {
      throw new IllegalArgumentException("Invalid type: " + type);
    }
    if (studentNamesDisplay != null && !NAMES_DISPLAY_OPTIONS.contains(studentNamesDisplay)) {
      throw new IllegalArgumentException("Invalid studentNamesDisplay: " + studentNamesDisplay);
    }
    String normalizedPrompt = normalizePrompt(prompt);

    String serializedItems = validateAndSerialize(strategy, run, period, items);

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

  private TeacherPresentationItemStrategy<?> getStrategy(String type) {
    TeacherPresentationItemStrategy<?> strategy = strategies.get(type);
    if (strategy == null) {
      if (TeacherPresentationConfig.TYPE_DISCUSSION.equals(type)) {
        return new DiscussionPresentationItemStrategy(studentWorkDao);
      } else if (TeacherPresentationConfig.TYPE_IDEAS.equals(type)) {
        return new IdeasPresentationItemStrategy(studentWorkDao);
      }
    }
    return strategy;
  }

  private <T> String validateAndSerialize(TeacherPresentationItemStrategy<T> strategy, Run run,
      Group period, List<?> items) {
    List<T> normalized = strategy.validateAndNormalize(run, period, items);
    return strategy.serializeItems(normalized, objectMapper);
  }

  private <T> List<Integer> parseAndExtractStudentWorkIds(
      TeacherPresentationItemStrategy<T> strategy, String itemsJson) {
    List<T> items = strategy.parseItems(itemsJson, objectMapper);
    return strategy.extractStudentWorkIds(items);
  }

  private String resolveType(String type) {
    return type != null ? type : TeacherPresentationConfig.TYPE_DISCUSSION;
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
}
