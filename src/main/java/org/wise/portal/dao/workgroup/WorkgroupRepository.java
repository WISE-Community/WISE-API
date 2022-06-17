package org.wise.portal.dao.workgroup;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

/**
 * @author Hiroki Terashima
 */
@Repository
public interface WorkgroupRepository extends PagingAndSortingRepository<WorkgroupImpl, Long> {}
