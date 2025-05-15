package org.wise.portal.service.ping.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(EasyMockExtension.class)
public class CRaterPingServiceImplTest {
  @TestSubject
  private CRaterPingServiceImpl pingEndpointServiceImpl = new CRaterPingServiceImpl();

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private SetOperations<String, String> setOperations;

  private String testId = "test";

  @Test
  public void hasPingedItem_ItemPinged_ShouldReturnTrue() {
    Set<String> members = new HashSet<String>();
    members.add(testId);
    expect(stringRedisTemplate.opsForSet()).andReturn(setOperations);
    expect(setOperations.members(testId)).andReturn(members);
    replay(stringRedisTemplate, setOperations);
    assertTrue(pingEndpointServiceImpl.hasPingedItem(testId));
    verify(stringRedisTemplate);
    verify(setOperations);
  }
 
  @Test
  public void hasPingedItem_ItemNotPinged_ShouldReturnFalse() {
    Set<String> members = new HashSet<String>();
    expect(stringRedisTemplate.opsForSet()).andReturn(setOperations);
    expect(setOperations.members(testId)).andReturn(members);
    replay(stringRedisTemplate, setOperations);
    assertFalse(pingEndpointServiceImpl.hasPingedItem(testId));
    verify(stringRedisTemplate);
    verify(setOperations);

  }


  @Test
  public void cachePingedItem_ShouldCacheAndExpireItemId() {
    expect(stringRedisTemplate.opsForSet()).andReturn(setOperations);
    expect(stringRedisTemplate.expire(testId, Duration.ofSeconds(1))).andReturn(null);
    expect(setOperations.add(testId, "pinged")).andReturn(null);
    replay(stringRedisTemplate, setOperations);
    pingEndpointServiceImpl.cachePingedItem(testId, 1);
    verify(stringRedisTemplate, setOperations);
  }
}