package org.wise.portal.service.ping.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(EasyMockExtension.class)
public class PingEndpointServiceImplTest {
  @TestSubject
  private PingEndpointServiceImpl pingEndpointServiceImpl = new PingEndpointServiceImpl();

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  private String testId = "test";

  // @Test
  // public void hasPingedItem_ItemPinged_ShouldReturnTrue() {
  //   Set<String> members = new HashSet<String>();
  //   members.add("pinged");
  //   expect(stringRedisTemplate.opsForSet().members(testId)).andReturn(members);
  //   replay(stringRedisTemplate);
  //   assertTrue(pingEndpointServiceImpl.hasPingedItem(testId));
  //   verify(stringRedisTemplate);
  // }
 
  // @Test
  // public void hasPingedItem_ItemNotPinged_ShouldReturnFalse() {
  //   expect(stringRedisTemplate.opsForSet().members(testId)).andReturn(new HashSet<String>());
  //   assertFalse(pingEndpointServiceImpl.hasPingedItem(testId));
  // }


  // @Test
  // public void cachePingedItem_ShouldCacheAndExpireItemId() {
  //   assertTrue(stringRedisTemplateOpsSize(testId) == 0);
  //   pingEndpointServiceImpl.cachePingedItem(testId, 1);
  //   assertTrue(stringRedisTemplateOpsSize(testId) == 1);
  //   try { 
  //     Thread.sleep(1001);
  //   } catch(InterruptedException e) {
  //     e.printStackTrace();
  //   }
  //   assertTrue(stringRedisTemplateOpsSize(testId) == 0);
  // }

  // private int stringRedisTemplateOpsSize(String id) {
  //   return stringRedisTemplate.opsForSet().members(id).size();
  // }
}
