package org.wise.portal.service.ping.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(EasyMockExtension.class)
public class CRaterPingServiceImplTest {
  @TestSubject
  private CRaterPingServiceImpl cRaterPingServiceImpl = new CRaterPingServiceImpl();

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private String testId = "test";

  @Test
  public void hasPingedItem_ItemPinged_ShouldReturnTrue() {
    expect(stringRedisTemplate.opsForValue()).andReturn(valueOperations);
    expect(valueOperations.size(testId)).andReturn(1L);
    replay(stringRedisTemplate, valueOperations);
    assertTrue(cRaterPingServiceImpl.hasPingedItem(testId));
    verify(stringRedisTemplate);
    verify(valueOperations);
  }

  @Test
  public void hasPingedItem_ItemNotPinged_ShouldReturnFalse() {
    expect(stringRedisTemplate.opsForValue()).andReturn(valueOperations);
    expect(valueOperations.size(testId)).andReturn(0L);
    replay(stringRedisTemplate, valueOperations);
    assertFalse(cRaterPingServiceImpl.hasPingedItem(testId));
    verify(stringRedisTemplate);
    verify(valueOperations);

  }

  @Test
  public void cachePingedItem_ShouldCacheAndExpireItemId() {
    expect(stringRedisTemplate.opsForValue()).andReturn(valueOperations);
    expect(stringRedisTemplate.expire(testId, Duration.ofSeconds(1))).andReturn(null);
    expect(valueOperations.setIfAbsent(testId, "pinged")).andReturn(null);
    replay(stringRedisTemplate, valueOperations);
    cRaterPingServiceImpl.cachePingedItem(testId, 1);
    verify(stringRedisTemplate, valueOperations);
  }
}
