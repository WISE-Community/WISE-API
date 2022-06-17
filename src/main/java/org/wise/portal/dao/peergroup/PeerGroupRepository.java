package org.wise.portal.dao.peergroup;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;

/**
 * @author Hiroki Terashima
 */
@Repository
public interface PeerGroupRepository extends PagingAndSortingRepository<PeerGroupImpl, Long> {}
