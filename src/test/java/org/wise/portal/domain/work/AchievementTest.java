package org.wise.portal.domain.work;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.easymock.EasyMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.domain.DomainTest;
import org.wise.portal.service.work.AchievementJsonModule;
import org.wise.vle.domain.achievement.Achievement;
import org.wise.vle.domain.achievement.AchievementSerializer;

@ExtendWith(EasyMockExtension.class)
public class AchievementTest extends DomainTest {

  Achievement achievement;

  ObjectMapper mapper;

  AchievementJsonModule jsonModule = new AchievementJsonModule();

  @BeforeEach
  public void setup() {
    super.setup();
    jsonModule.addSerializer(Achievement.class, new AchievementSerializer());
    mapper = new ObjectMapper();
    mapper.registerModule(jsonModule);
    achievement = new Achievement();
    achievement.setId(12);
    achievement.setRun(run);
    achievement.setWorkgroup(workgroup);
    achievement.setAchievementId("achievement_1");
    achievement.setType("milestoneReport");
    achievement.setData("{}");
    achievement.setAchievementTime(new Timestamp(1L));
  }

  @Test
  public void serialize() throws JsonProcessingException {
    String json = mapper.writeValueAsString(achievement);
    assertEquals("{\"id\":12,\"runId\":1,\"workgroupId\":64,\"achievementId\":\"achievement_1\","
        + "\"type\":\"milestoneReport\",\"achievementTime\":1,\"data\":{}}", json);
  }
}
