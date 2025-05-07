package org.wise.portal.service.ping;

public interface PingEndpointService {
	public boolean hasPingedItem(String itemId);
	public void cachePingTimestamp(String itemId);
}
