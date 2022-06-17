package org.wise.portal.dao.annotation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wise.portal.dao.WISEHibernateTest;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.annotation.wise5.Annotation;

@SpringBootTest
@RunWith(SpringRunner.class)
public class HibernateAnnotationDaoTest extends WISEHibernateTest {

  private final String COMMENT_TYPE = "comment";
  private final String DUMMY_ANNOTATION_DATA = "Dummy Annotation Data";

  @Autowired
  private AnnotationDao<Annotation> annotationDao;

  @Test
  public void getAnnotations_AnnotationDoesNotExist_ShouldReturnEmptyList() {
    createAndRetrieveAnnotations(NODE_ID2, COMPONENT_ID2, NODE_ID1, COMPONENT_ID1, 0);
  }

  @Test
  public void getAnnotations_AnnotationsExist_ShouldReturnAnnotations() {
    createAndRetrieveAnnotations(NODE_ID1, COMPONENT_ID1, NODE_ID1, COMPONENT_ID1, 1);
  }

  @Test
  public void getAnnotations_FromAllPeriods_ShouldReturnAnnotations() {
    Annotation annotation1 = createAnnotation(run1, run1Period1, teacherWorkgroup1, workgroup1,
        COMMENT_TYPE, NODE_ID1, COMPONENT_ID1, DUMMY_ANNOTATION_DATA);
    addUserToRun(student4, run1, run1Period2);
    Workgroup workgroup4 = addUserToRun(student4, run1, run1Period2);
    Annotation annotation2 = createAnnotation(run1, run1Period2, teacherWorkgroup1, workgroup4,
        COMMENT_TYPE, NODE_ID1, COMPONENT_ID1, DUMMY_ANNOTATION_DATA);
    List<Annotation> annotations = annotationDao.getAnnotations(run1, null, NODE_ID1,
        COMPONENT_ID1);
    assertEquals(2, annotations.size());
    assertTrue(annotations.contains(annotation1));
    assertTrue(annotations.contains(annotation2));
  }

  private void createAndRetrieveAnnotations(String createAnnotationNodeId,
      String createAnnotationComponentId, String retrieveAnnotationNodeId,
      String retrieveAnnotationComponentId, Integer expectedCount) {
    createAnnotation(run1, run1Period1, teacherWorkgroup1, workgroup1, COMMENT_TYPE,
        createAnnotationNodeId, createAnnotationComponentId, DUMMY_ANNOTATION_DATA);
    List<Annotation> annotations = annotationDao.getAnnotations(run1, run1Period1,
        retrieveAnnotationNodeId, retrieveAnnotationComponentId);
    assertEquals(expectedCount, annotations.size());
  }

  private Annotation createAnnotation(Run run, Group period, Workgroup fromWorkgroup,
      Workgroup toWorkgroup, String type, String nodeId, String componentId, String data) {
    Annotation annotation = new Annotation();
    Calendar now = Calendar.getInstance();
    Timestamp timestamp = new Timestamp(now.getTimeInMillis());
    annotation.setClientSaveTime(timestamp);
    annotation.setServerSaveTime(timestamp);
    annotation.setRun(run);
    annotation.setPeriod(period);
    annotation.setNodeId(nodeId);
    annotation.setComponentId(componentId);
    annotation.setFromWorkgroup(fromWorkgroup);
    annotation.setToWorkgroup(toWorkgroup);
    annotation.setType(type);
    annotation.setData(data);
    annotationDao.save(annotation);
    return annotation;
  }

}
