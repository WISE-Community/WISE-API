package org.wise.portal.dao.project;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.wise.portal.domain.project.impl.ProjectImpl;

/**
 * @author Hiroki Terashima
 */
@Repository
public interface ProjectRepository extends PagingAndSortingRepository<ProjectImpl, Long> {}
