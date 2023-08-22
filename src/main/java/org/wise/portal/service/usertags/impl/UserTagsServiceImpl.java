package org.wise.portal.service.usertags.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.wise.portal.service.usertags.UserTagsService;

@Service
public class UserTagsServiceImpl implements UserTagsService {

  @Autowired
  private AclTargetObjectIdentityDao<MutableAclTargetObjectIdentity> aclTargetObjectIdentityDao;

  @Autowired
  private UserTagsDao<UserTag> userTagsDao;

  @Override
  public UserTag get(User user, String text) {
    return userTagsDao.get(user, text);
  }

  @Override
  public UserTag createTag(User user, String text) {
    UserTag userTag = new UserTagImpl(user, text);
    userTagsDao.save(userTag);
    return userTag;
  }

  @Override
  public Boolean isOwner(User user, Long tagId) {
    UserTag userTag = userTagsDao.get(tagId);
    return userTag != null && userTag.getUser().getId().equals(user.getId());
  }

  @Override
  public UserTag editTag(Long tagId, String tag) {
    UserTag userTag = userTagsDao.get(tagId);
    userTag.setText(tag);
    userTagsDao.save(userTag);
    return null;
  }

  @Override
  public UserTag deleteTag(Long tagId) {
    UserTag userTag = userTagsDao.get(tagId);
    userTagsDao.delete(userTag);
    return null;
  }

  @Override
  public List<UserTag> getTags(User user) {
    return userTagsDao.get(user);
  }

  @Override
  public Set<UserTag> getTags(User user, Project project) {
    MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
    return mutableObjectIdentity.getTags().stream().filter(t -> t.getUser().equals(user))
        .collect(Collectors.toSet());
  }

  @Override
  public Boolean hasTag(User user, Project project, String tag) {
    MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
    Set<UserTag> tags = mutableObjectIdentity.getTags();
    return tags.stream().anyMatch(t -> t.getUser().equals(user) && t.getText().equals(tag));
  }

  @Override
  public UserTag applyTag(Project project, Long tagId) {
    MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
    mutableObjectIdentity.getTags().add(userTagsDao.get(tagId));
    aclTargetObjectIdentityDao.save(mutableObjectIdentity);
    return null;
  }

  @Override
  public UserTag removeTag(Project project, Long tagId) {
    MutableAclTargetObjectIdentity mutableObjectIdentity = getMutableObjectIdentity(project);
    mutableObjectIdentity.getTags().remove(userTagsDao.get(tagId));
    aclTargetObjectIdentityDao.save(mutableObjectIdentity);
    return null;
  }

  private MutableAclTargetObjectIdentity getMutableObjectIdentity(Project project) {
    ObjectIdentity objectIdentity = new ObjectIdentityImpl(
        HibernateProxyHelper.getClassWithoutInitializingProxy(project), project.getId());
    return aclTargetObjectIdentityDao.retrieveByObjectIdentity(objectIdentity);
  }

}
