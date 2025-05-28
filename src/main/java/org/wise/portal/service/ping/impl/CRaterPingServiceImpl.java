package org.wise.portal.service.ping.impl;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.wise.portal.service.ping.CRaterPingService;

@Service
public class CRaterPingServiceImpl implements CRaterPingService {
  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  public boolean hasPingedItem(String itemId) {
    return stringRedisTemplate.opsForValue().size(itemId) == 1;
  }

  public void cachePingedItem(String itemId, int ttl) {
    this.stringRedisTemplate.opsForValue().setIfAbsent(itemId, "pinged");
    this.stringRedisTemplate.expire(itemId, Duration.ofSeconds(ttl));
  }
}
