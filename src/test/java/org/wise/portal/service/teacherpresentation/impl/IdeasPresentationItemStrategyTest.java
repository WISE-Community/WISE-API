package org.wise.portal.service.teacherpresentation.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.teacherpresentation.IdeasItem;
import org.wise.portal.service.teacherpresentation.IdeasResponse;
import org.wise.vle.domain.teacherpresentation.TeacherPresentationConfig;
import org.wise.vle.domain.work.StudentWork;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(EasyMockExtension.class)
public class IdeasPresentationItemStrategyTest {

  private IdeasPresentationItemStrategy strategy;

  @Mock
  private StudentWorkDao<StudentWork> studentWorkDao;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private RunImpl run = new RunImpl();
  private PersistentGroup period = new PersistentGroup();

  @BeforeEach
  public void setUp() {
    run.setId(1L);
    period.setId(10L);
    strategy = new IdeasPresentationItemStrategy(studentWorkDao);
  }

  private StudentWork work(Integer id) {
    StudentWork w = new StudentWork();
    w.setId(id);
    w.setRun(run);
    w.setPeriod(period);
    return w;
  }

  @Test
  public void getType_ReturnsIdeas() {
    assertEquals(TeacherPresentationConfig.TYPE_IDEAS, strategy.getType());
  }

  @Test
  public void parseItems_ValidJson_ReturnsList() {
    String json = "[{\"ideaId\":\"idea1\",\"responses\":[{\"studentWorkId\":101,\"workgroupId\":1,\"responseTimestamp\":1000,\"text\":\"test\"}]}]";
    List<IdeasItem> items = strategy.parseItems(json, objectMapper);
    assertEquals(1, items.size());
    assertEquals("idea1", items.get(0).ideaId());
    assertEquals(1, items.get(0).responses().size());
    assertEquals(101, items.get(0).responses().get(0).studentWorkId());
  }

  @Test
  public void parseItems_NullOrEmptyOrInvalid_ReturnsEmptyList() {
    assertTrue(strategy.parseItems(null, objectMapper).isEmpty());
    assertTrue(strategy.parseItems("", objectMapper).isEmpty());
    assertTrue(strategy.parseItems("invalid-json", objectMapper).isEmpty());
  }

  @Test
  public void serializeItems_ExceedsLimit_ThrowsIllegalArgument() {
    // 60,000 bytes limit: generate items exceeding 60,000 bytes
    List<IdeasResponse> responses = new ArrayList<>();
    for (int i = 0; i < 40; i++) {
      responses.add(new IdeasResponse(100 + i, 1L, (long) i, "A".repeat(1800)));
    }
    List<IdeasItem> items = Arrays.asList(new IdeasItem("idea1", responses));
    assertThrows(IllegalArgumentException.class,
        () -> strategy.serializeItems(items, objectMapper));
  }

  @Test
  public void validateAndNormalize_TruncatesLongText() {
    expect(studentWorkDao.getStudentWorkListByIds(Arrays.asList(101)))
        .andReturn(Arrays.asList(work(101)));
    replay(studentWorkDao);

    String longText = "x".repeat(2500);
    List<IdeasItem> rawItems = Arrays
        .asList(new IdeasItem("idea1", Arrays.asList(new IdeasResponse(101, 1L, 1000L, longText))));
    List<IdeasItem> normalized = strategy.validateAndNormalize(run, period, rawItems);
    assertEquals(1, normalized.size());
    assertEquals(2000, normalized.get(0).responses().get(0).text().length());
    verify(studentWorkDao);
  }

  @Test
  public void validateAndNormalize_InvalidIdeaId_ThrowsIllegalArgument() {
    replay(studentWorkDao);
    List<IdeasItem> rawItems = Arrays.asList(new IdeasItem("   ", Collections.emptyList()));
    assertThrows(IllegalArgumentException.class,
        () -> strategy.validateAndNormalize(run, period, rawItems));
  }

  @Test
  public void validateAndNormalize_NullIdeaIdAndEmptyResponses_ThrowsIllegalArgument() {
    replay(studentWorkDao);
    List<IdeasItem> rawItems = Arrays.asList(new IdeasItem(null, Collections.emptyList()));
    assertThrows(IllegalArgumentException.class,
        () -> strategy.validateAndNormalize(run, period, rawItems));
  }

  @Test
  public void validateAndNormalize_MoreThan500Responses_ThrowsIllegalArgument() {
    replay(studentWorkDao);
    List<IdeasResponse> responses = new ArrayList<>();
    for (int i = 0; i < 501; i++) {
      responses.add(new IdeasResponse(i, 1L, (long) i, "msg"));
    }
    List<IdeasItem> rawItems = Arrays.asList(new IdeasItem("idea1", responses));
    assertThrows(IllegalArgumentException.class,
        () -> strategy.validateAndNormalize(run, period, rawItems));
  }

  @Test
  public void extractStudentWorkIds_ReturnsIds() {
    List<IdeasItem> items = Arrays.asList(
        new IdeasItem("idea1", Arrays.asList(new IdeasResponse(101, 1L, null, "a"))),
        new IdeasItem(null, Arrays.asList(new IdeasResponse(102, 2L, null, "b"))));
    List<Integer> ids = strategy.extractStudentWorkIds(items);
    assertEquals(Arrays.asList(101, 102), ids);
  }
}
