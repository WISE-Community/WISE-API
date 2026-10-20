package org.wise.portal.service.teacherpresentation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wise.portal.dao.work.StudentWorkDao;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.teacherpresentation.TeacherPresentationConfigService;
import org.wise.portal.service.teacherpresentation.TeacherPresentationItemStrategy;
import org.wise.vle.domain.work.StudentWork;

/**
 * Base abstract strategy providing shared student work validation logic across presentation types.
 *
 * @param <T> the typed item domain class
 */
public abstract class AbstractTeacherPresentationItemStrategy<T>
    implements TeacherPresentationItemStrategy<T> {

  protected StudentWorkDao<StudentWork> studentWorkDao;

  public AbstractTeacherPresentationItemStrategy(StudentWorkDao<StudentWork> studentWorkDao) {
    this.studentWorkDao = studentWorkDao;
  }

  /**
   * Validates student work IDs against run and period ownership using batch fetching.
   *
   * @param run the active run
   * @param period the active period group
   * @param ids the list of student work IDs to validate
   * @return set of unique validated student work IDs preserving insertion order
   * @throws IllegalArgumentException if an ID is null, unknown, belongs to another run/period,
   *         or exceeds MAX_ITEMS limit
   */
  protected Set<Integer> validateStudentWorkIds(Run run, Group period, List<Integer> ids) {
    Set<Integer> uniqueIds = new LinkedHashSet<>();
    if (ids != null) {
      uniqueIds.addAll(ids);
    }
    if (uniqueIds.size() > TeacherPresentationConfigService.MAX_ITEMS) {
      throw new IllegalArgumentException("Too many items. The maximum is "
          + TeacherPresentationConfigService.MAX_ITEMS);
    }
    for (Integer id : uniqueIds) {
      if (id == null) {
        throw new IllegalArgumentException("Invalid studentWorkId");
      }
    }
    if (uniqueIds.isEmpty()) {
      return uniqueIds;
    }
    List<StudentWork> works = studentWorkDao.getStudentWorkListByIds(new ArrayList<>(uniqueIds));
    Map<Integer, StudentWork> workMap = new HashMap<>();
    for (StudentWork work : works) {
      if (work != null && work.getId() != null) {
        workMap.put(work.getId(), work);
      }
    }
    for (Integer id : uniqueIds) {
      StudentWork work = workMap.get(id);
      if (work == null || !work.getRun().getId().equals(run.getId())
          || !work.getPeriod().getId().equals(period.getId())) {
        throw new IllegalArgumentException("Unknown studentWorkId: " + id);
      }
    }
    return uniqueIds;
  }
}
