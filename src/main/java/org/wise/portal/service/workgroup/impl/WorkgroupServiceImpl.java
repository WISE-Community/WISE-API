/**
 * Copyright (c) 2007-2019 Encore Research Group, University of Toronto
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Public License for more details.
 *
 * You should have received a copy of the GNU Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.wise.portal.service.workgroup.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.dao.group.GroupDao;
import org.wise.portal.dao.workgroup.WorkgroupDao;
import org.wise.portal.domain.Tag;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.impl.ChangeWorkgroupParameters;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.acl.AclService;
import org.wise.portal.service.group.GroupService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WorkgroupService;

/**
 * @author Cynick Young
 * @author Hiroki Terashima
 */
@Service
public class WorkgroupServiceImpl implements WorkgroupService {

  @Autowired
  protected WorkgroupDao<Workgroup> workgroupDao;

  @Autowired
  protected GroupDao<Group> groupDao;

  @Autowired
  private GroupService groupService;

  @Autowired
  protected UserService userService;

  @Autowired
  protected AclService<Workgroup> aclService;

  @Transactional()
  public Workgroup createWorkgroup(String name, Set<User> members, Run run, Group period) {
    Workgroup workgroup = new WorkgroupImpl(name, members, run, period);
    groupDao.save(workgroup.getGroup());
    workgroupDao.save(workgroup);
    aclService.addPermission(workgroup, BasePermission.ADMINISTRATION);
    return workgroup;
  }

  @Transactional(readOnly = true)
  public List<Workgroup> getWorkgroupListByRunAndUser(Run run, User user) {
    return workgroupDao.getListByRunAndUser(run, user);
  }

  @Transactional(readOnly = true)
  public List<Workgroup> getWorkgroupsForUser(User user) {
    return workgroupDao.getListByUser(user);
  }

  @Transactional(readOnly = true)
  public List<Workgroup> getWorkgroupsForRun(Run run) {
    return workgroupDao.getListByRun(run);
  }

  @Transactional()
  public void addMembers(Workgroup workgroup, Set<User> membersToAdd) {
    for (User member : membersToAdd) {
      workgroup.addMember(member);
    }
    groupDao.save(workgroup.getGroup());
    workgroupDao.save(workgroup);
  }

  @Transactional()
  public void removeMembers(Workgroup workgroup, Set<User> membersToRemove) {
    for (User member : membersToRemove) {
      workgroup.removeMember(member);
    }
    groupDao.save(workgroup.getGroup());
    workgroupDao.save(workgroup);
  }

  @Transactional()
  public Workgroup updateWorkgroupMembership(ChangeWorkgroupParameters params) {
    Workgroup toWorkgroup = params.getWorkgroupTo();
    Run run = params.getRun();
    User user = params.getStudent();
    if (toWorkgroup != null && isWorkgroupInRun(toWorkgroup, run)
        && isUserInSamePeriodAsWorkgroup(user, toWorkgroup)) {
      removeUserFromAllWorkgroupsInRun(run, user);
      addMembers(toWorkgroup, Collections.singleton(user));
      return toWorkgroup;
    }
    return null;
  }

  private boolean isWorkgroupInRun(Workgroup workgroup, Run run) {
    return this.getWorkgroupsForRun(run).contains(workgroup);
  }

  private boolean isUserInSamePeriodAsWorkgroup(User user, Workgroup workgroup) {
    return workgroup.getPeriod().getMembers().contains(user);
  }

  public void removeUserFromAllWorkgroupsInRun(Run run, User user) {
    for (Workgroup workgroup : getWorkgroupListByRunAndUser(run, user)) {
      removeMembers(workgroup, Collections.singleton(user));
    }
  }

  public Workgroup retrieveById(Long workgroupId) throws ObjectNotFoundException {
    return workgroupDao.getById(workgroupId);
  }

  /**
   * Check if a user is in any workgroup for the run
   *
   * @param user
   *               the user
   * @param run
   *               the run
   * @return whether the user is in a workgroup for the run
   */
  public boolean isUserInAnyWorkgroupForRun(User user, Run run) {
    List<Workgroup> workgroupsForUser = getWorkgroupListByRunAndUser(run, user);
    return workgroupsForUser.size() > 0;
  }

  /**
   * Check if a user is in a specific workgroup for the run
   *
   * @param user
   *                    the user
   * @param run
   *                    the run
   * @param workgroup
   *                    the workgroup
   * @return whether the user is in the workgroup
   */
  public boolean isUserInWorkgroupForRun(User user, Run run, Workgroup workgroup) {
    boolean result = false;
    if (user != null && workgroup != null) {
      List<Workgroup> workgroupsForUser = getWorkgroupListByRunAndUser(run, user);
      for (Workgroup tempWorkgroup : workgroupsForUser) {
        if (workgroup.equals(tempWorkgroup)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  /**
   * Check if a user is in a workgroup besides the one provided for the run
   *
   * @param user
   *                    the user
   * @param run
   *                    the run
   * @param workgroup
   *                    check if the user is in another workgroup besides this workgroup
   * @return whether the user is in another workgroup for the run
   */
  public boolean isUserInAnotherWorkgroupForRun(User user, Run run, Workgroup workgroup) {
    boolean result = false;
    if (user != null && workgroup != null) {
      List<Workgroup> workgroupsForUser = getWorkgroupListByRunAndUser(run, user);
      for (Workgroup tempWorkgroup : workgroupsForUser) {
        if (!workgroup.equals(tempWorkgroup)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  public void changePeriod(Workgroup workgroup, Group newPeriod) {
    Set<User> members = workgroup.getMembers();
    groupService.removeMembers(workgroup.getPeriod(), members);
    groupService.addMembers(newPeriod, members);
    workgroup.setPeriod(newPeriod);
    workgroupDao.save(workgroup);
  }

  public void addTag(Workgroup workgroup, Tag tag) {
    workgroup.addTag(tag);
    workgroupDao.save(workgroup);
  }

  @Override
  public void removeTag(Workgroup workgroup, Tag tag) {
    workgroup.getTags().removeIf(existingTag -> (existingTag.getId().equals(tag.getId())));
    workgroupDao.save(workgroup);
  }
}
