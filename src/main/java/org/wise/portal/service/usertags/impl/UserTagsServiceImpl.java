package org.wise.portal.service.usertags.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.authentication.AclTargetObjectIdentityDao;
import org.wise.portal.dao.usertags.UserTagsDao;
import org.wise.portal.domain.authentication.MutableAclTargetObjectIdentity;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.usertag.UserTag;
import org.wise.portal.domain.usertag.impl.UserTagImpl;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.usertags.UserTagsService;

@Service
public class UserTagsServiceImpl implements UserTagsService {

  @Autowired
  private AclTargetObjectIdentityDao<MutableAclTargetObjectIdentity> aclTargetObjectIdentityDao;

  @Autowired
  private ProjectService projectService;

  @Autowired
  private UserTagsDao<UserTag> userTagsDao;

  @Override
  public UserTag get(User user, String text) {
    return userTagsDao.get(user, text);
  }

  @Override
  public UserTag get(Long id) {
    return userTagsDao.get(id);
  }

  @Override
  public UserTag createTag(User user, String text, String color) {
    UserTag userTag = new UserTagImpl(user, text, color);
    userTagsDao.save(userTag);
    return userTag;
  }

  @Override
  public Set<UserTag> getTags(User user, Project project) {
    MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
    return mutableObjectIdentity.getTags().stream().filter(t -> t.getUser().equals(user))
        .collect(Collectors.toSet());
  }

  @Override
  public boolean hasTag(User user, String text) {
    return hasTag(user, text, null);
  }

  @Override
  public boolean hasTag(User user, String text, Long idToIgnore) {
    List<UserTag> tags = getTags(user);
    if (idToIgnore != null) {
      tags.remove(this.get(idToIgnore));
    }
    return tags.stream().anyMatch(t -> t.getText().toLowerCase().equals(text.toLowerCase()));
  }

  @Override
  public boolean hasTag(User user, Project project, String text) {
    MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
    Set<UserTag> tags = mutableObjectIdentity.getTags();
    return tags.stream()
        .anyMatch(t -> t.getUser().equals(user) && t.getText().equals(text.toLowerCase()));
  }

  @Override
  public void applyTag(Project project, UserTag tag) {
    MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
    mutableObjectIdentity.getTags().add(tag);
    aclTargetObjectIdentityDao.save(mutableObjectIdentity);
  }

  @Override
  public void removeTag(Project project, UserTag tag) {
    MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
    mutableObjectIdentity.getTags().remove(tag);
    aclTargetObjectIdentityDao.save(mutableObjectIdentity);
  }

  private MutableAclTargetObjectIdentity getMutableObjectIdentity(Project project) {
    ObjectIdentity objectIdentity = new ObjectIdentityImpl(
        HibernateProxyHelper.getClassWithoutInitializingProxy(project), project.getId());
    return aclTargetObjectIdentityDao.retrieveByObjectIdentity(objectIdentity);
  }

  public List<UserTag> getTagsList(User user, Project project) {
    List<UserTag> tagsList = getTags(user, project).stream().collect(Collectors.toList());
    Collections.sort(tagsList,
        (tag1, tag2) -> tag1.getText().toLowerCase().compareTo(tag2.getText().toLowerCase()));
    return tagsList;
  }

  public List<UserTag> getTags(User user) {
    return userTagsDao.get(user);
  }

  public void updateTag(User user, UserTag tag) {
    UserTag userTag = userTagsDao.get((Long) tag.getId());
    if (user.equals(userTag.getUser())) {
      userTagsDao.save(tag);
    } else {
      throw new AccessDeniedException("User does not have permission to update tag.");
    }
  }

  public void deleteTag(User user, UserTag tag) {
    if (user.equals(tag.getUser())) {
      removeTagFromProjects(projectService.getProjectList(user), tag);
      removeTagFromProjects(projectService.getSharedProjectList(user), tag);
      userTagsDao.delete(tag);
    } else {
      throw new AccessDeniedException("User does not have permission to delete tag.");
    }
  }

  private void removeTagFromProjects(List<Project> projects, UserTag userTag) {
    for (Project project : projects) {
      MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
      mutableObjectIdentity.getTags().remove(userTag);
      aclTargetObjectIdentityDao.save(mutableObjectIdentity);
    }
  }
}
