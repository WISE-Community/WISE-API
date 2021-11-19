package org.wise.portal.dao.run;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.wise.portal.domain.run.impl.RunImpl;

/**
 * @author Hiroki Terashima
 */
@Repository
public interface RunRepository extends PagingAndSortingRepository<RunImpl, Long> {}
