package org.wise.portal.service.ping.impl;

import java.time.Duration;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.wise.portal.service.ping.PingEndpointService;

@Service
public class PingEndpointServiceImpl implements PingEndpointService{
  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  public boolean hasPingedItem(String itemId) {
    Set<String> members = stringRedisTemplate.opsForSet().members(itemId);
    return members.size() > 0;
  }

  public void cachePingedItem(String itemId, int ttl) {
    this.stringRedisTemplate.opsForSet().add(itemId, "pinged");
    this.stringRedisTemplate.expire(itemId, Duration.ofSeconds(ttl));      
  }
}