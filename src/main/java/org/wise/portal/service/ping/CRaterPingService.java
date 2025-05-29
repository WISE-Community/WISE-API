package org.wise.portal.service.ping;

public interface CRaterPingService {
	public boolean hasPingedItem(String itemId);
	public void cachePingedItem(String itemId, int ttl);
}
