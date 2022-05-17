package org.wise.portal.dao.group;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.wise.portal.domain.group.impl.PersistentGroup;

/**
 * @author Hiroki Terashima
 */
@Repository
public interface GroupRepository extends PagingAndSortingRepository<PersistentGroup, Long> {}
