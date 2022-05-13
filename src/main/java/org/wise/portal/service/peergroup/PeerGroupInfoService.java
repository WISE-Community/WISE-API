package org.wise.portal.service.peergroup;

import java.util.Map;

import org.wise.portal.domain.peergrouping.PeerGrouping;

/**
 * @author Hiroki Terashima
 */
public interface PeerGroupInfoService {

  /**
   * Returns a map with 2 elements:
   * 1. "peerGroups": PeerGroups in the specified PeerGrouping
   * 2. "workgroupsNotInPeerGroups": Workgroups that have not been paired into a
   * PeerGroup for the specified PeerGrouping
   * @param peerGrouping PeerGrouping to get the info for
   * @return a Map containing information about PeerGroups for the specified PeerGrouping
   */
	Map<String, Object> getPeerGroupInfo(PeerGrouping peerGrouping);
}
