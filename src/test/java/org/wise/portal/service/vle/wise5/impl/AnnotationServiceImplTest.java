package org.wise.portal.service.vle.wise5.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.dao.annotation.wise5.AnnotationDao;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.WISEServiceTest;
import org.wise.vle.domain.annotation.wise5.Annotation;

@RunWith(EasyMockRunner.class)
public class AnnotationServiceImplTest extends WISEServiceTest {

  @TestSubject
  private AnnotationServiceImpl service = new AnnotationServiceImpl();

  @Mock
  private AnnotationDao<Annotation> annotationDao;

  @Test
  public void getLatest_ReturnLatestAnnotations() {
    Annotation annotation1 = createAutoScoreAnnotation(run1Workgroup1, run1Node1Id,
        run1Component1Id);
    Annotation annotation2 = createAutoScoreAnnotation(run1Workgroup2, run1Node1Id,
        run1Component1Id);
    Annotation annotation3 = createAutoScoreAnnotation(run1Workgroup1, run1Node1Id,
        run1Component1Id);
    HashSet<Workgroup> workgroups = new HashSet<Workgroup>();
    workgroups.add(run1Workgroup1);
    workgroups.add(run1Workgroup2);
    expect(annotationDao.getAnnotationsToWorkgroups(workgroups, run1Node1Id, run1Component1Id))
        .andReturn(Arrays.asList(annotation1, annotation2, annotation3));
    replay(annotationDao);
    assertEquals(2,
        service.getLatest(workgroups, run1Node1Id, run1Component1Id, "autoScore").size());
    verify(annotationDao);
  }

  private Annotation createAutoScoreAnnotation(Workgroup toWorkgroup, String nodeId,
      String componentId) {
    Annotation annotation = new Annotation();
    annotation.setToWorkgroup(toWorkgroup);
    annotation.setNodeId(nodeId);
    annotation.setComponentId(componentId);
    annotation.setType("autoScore");
    return annotation;
  }
}
