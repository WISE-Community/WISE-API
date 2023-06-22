package org.wise.portal.service.tags.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.authentication.AclTargetObjectIdentityDao;
import org.wise.portal.dao.tags.TagsDao;
import org.wise.portal.domain.authentication.MutableAclTargetObjectIdentity;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.tags.Tags;
import org.wise.portal.domain.tags.impl.TagsImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.tags.TagsService;

@Service
public class TagsServiceImpl implements TagsService {

  @Autowired
  private AclTargetObjectIdentityDao<MutableAclTargetObjectIdentity> aclTargetObjectIdentityDao;

  @Autowired
  private TagsDao<Tags> tagsDao;

  public List<String> getTags(User user, Run run) {
    Tags tags = tagsDao.get(user, run);
    return tags == null ? new ArrayList<String>() : tags.getTagsList();
  }

  public Tags addTag(User user, Run run, String tag) {
    Tags tags = tagsDao.get(user, run);
    if (tags == null) {
      tags = new TagsImpl(user, getMutableObjectIdentity(run), "[\"" + tag + "\"]");
    } else if (!tags.containsTag(tag)) {
      tags.addTag(tag);
    }
    tagsDao.save(tags);
    return tags;
  }

  public Tags removeTag(User user, Run run, String tag) {
    Tags tags = tagsDao.get(user, run);
    if (tags != null) {
      tags.removeTag(tag);
      tagsDao.save(tags);
    }
    return tags;
  }

  private MutableAclTargetObjectIdentity getMutableObjectIdentity(Run run) {
    ObjectIdentity objectIdentity = new ObjectIdentityImpl(
        HibernateProxyHelper.getClassWithoutInitializingProxy(run), run.getId());
    return aclTargetObjectIdentityDao.retrieveByObjectIdentity(objectIdentity);
  }

}
