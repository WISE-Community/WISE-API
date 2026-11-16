package org.wise.portal.service.teacherpresentation.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.teacherpresentation.DiscussionItem;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.work.StudentWork;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(EasyMockExtension.class)
public class DiscussionPresentationItemStrategyTest {

  private DiscussionPresentationItemStrategy strategy;

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private RunImpl run = new RunImpl();
  private PersistentGroup period = new PersistentGroup();

  @BeforeEach
  public void setUp() {
    run.setId(1L);
    period.setId(10L);
    strategy = new DiscussionPresentationItemStrategy(studentWorkDao);
  }

  private StudentWork work(Integer id) {
    StudentWork w = new StudentWork();
    w.setId(id);
    w.setRun(run);
    w.setPeriod(period);
    return w;
  }

  @Test
  public void getType_ReturnsDiscussion() {
    assertEquals(TeacherPresentationConfig.TYPE_DISCUSSION, strategy.getType());
  }

  @Test
  public void parseItems_ValidJson_ReturnsList() {
    String json = "[{\"studentWorkId\":101},{\"studentWorkId\":102}]";
    List<DiscussionItem> items = strategy.parseItems(json, objectMapper);
    assertEquals(2, items.size());
    assertEquals(101, items.get(0).studentWorkId());
    assertEquals(102, items.get(1).studentWorkId());
  }

  @Test
  public void parseItems_NullOrEmptyOrInvalid_ReturnsEmptyList() {
    assertTrue(strategy.parseItems(null, objectMapper).isEmpty());
    assertTrue(strategy.parseItems("", objectMapper).isEmpty());
    assertTrue(strategy.parseItems("invalid-json", objectMapper).isEmpty());
  }

  @Test
  public void serializeItems_ReturnsJson() {
    List<DiscussionItem> items = Arrays.asList(new DiscussionItem(101));
    String json = strategy.serializeItems(items, objectMapper);
    assertEquals("[{\"studentWorkId\":101}]", json);
  }

  @Test
  public void validateAndNormalize_ValidItemsAndIntegerIds_Deduplicates() {
    expect(studentWorkDao.getStudentWorkListByIds(Arrays.asList(101, 102)))
        .andReturn(Arrays.asList(work(101), work(102)));
    replay(studentWorkDao);

    List<?> rawItems = Arrays.asList(new DiscussionItem(101), 102, new DiscussionItem(101));
    List<DiscussionItem> normalized = strategy.validateAndNormalize(run, period, rawItems);
    assertEquals(2, normalized.size());
    assertEquals(101, normalized.get(0).studentWorkId());
    assertEquals(102, normalized.get(1).studentWorkId());
    verify(studentWorkDao);
  }

  @Test
  public void validateAndNormalize_InvalidItemType_ThrowsIllegalArgument() {
    replay(studentWorkDao);
    assertThrows(IllegalArgumentException.class,
        () -> strategy.validateAndNormalize(run, period, Arrays.asList("string-item")));
  }

  @Test
  public void extractStudentWorkIds_ReturnsIds() {
    List<DiscussionItem> items = Arrays.asList(new DiscussionItem(101), new DiscussionItem(102),
        new DiscussionItem(null));
    List<Integer> ids = strategy.extractStudentWorkIds(items);
    assertEquals(Arrays.asList(101, 102), ids);
  }
}
