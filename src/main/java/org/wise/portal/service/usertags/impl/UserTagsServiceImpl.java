package org.wise.portal.service.usertags.impl;

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
}
