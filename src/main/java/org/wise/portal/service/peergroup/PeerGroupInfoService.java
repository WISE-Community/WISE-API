package org.wise.portal.service.peergroup;

import java.util.Map;

import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;

/**
 * @author Hiroki Terashima
 */
public interface PeerGroupInfoService {

  /**
   * Returns a map with 2 elements:
   * 1. "peerGroups": PeerGroups in the specified activity
   * 2. "workgroupsNotInPeerGroups": Workgroups that have not been paired into a
   * PeerGroup for the specified activity
   * @param activity PeerGroupActivity to get the info for
   * @return a Map containing information about PeerGroupings for the specified activity
   */
	Map<String, Object> getPeerGroupInfo(PeerGroupActivity activity);
}
