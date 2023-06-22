package org.wise.portal.domain.tags.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.json.JSONArray;
import org.json.JSONException;
import org.wise.portal.domain.authentication.MutableAclTargetObjectIdentity;
import org.wise.portal.domain.authentication.impl.PersistentAclTargetObjectIdentity;
import org.wise.portal.domain.tags.Tags;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.user.impl.UserImpl;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = TagsImpl.DATA_STORE_NAME)
public class TagsImpl implements Tags {

  @Transient
  public static final long serialVersionUID = 1L;

  @Transient
  public static final String DATA_STORE_NAME = "object_tags";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Getter
  @Setter
  private Long id = null;

  @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @Getter
  @Setter
  private User user;

  @ManyToOne(cascade = CascadeType.ALL, targetEntity = PersistentAclTargetObjectIdentity.class)
  @JoinColumn(name = "acl_object_identity", nullable = false)
  @Getter
  @Setter
  private MutableAclTargetObjectIdentity targetObjectIdentity;

  @Getter
  @Setter
  @Column(name = "tags")
  private String tags;

  public TagsImpl() {
  }

  public TagsImpl(User user, MutableAclTargetObjectIdentity targetObjectIdentity, String tags) {
    this.user = user;
    this.targetObjectIdentity = targetObjectIdentity;
    this.tags = tags;
  }

  public boolean containsTag(String tag) {
    try {
      JSONArray tagsArray = new JSONArray(this.getTags());
      for (int i = 0; i < tagsArray.length(); i++) {
        if (tagsArray.getString(i).equals(tag)) {
          return true;
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void addTag(String tag) {
    try {
      JSONArray tagsArray = new JSONArray(this.getTags());
      tagsArray.put(tag);
      this.setTags(tagsArray.toString());
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void removeTag(String tag) {
    try {
      JSONArray tagsArray = new JSONArray(this.getTags());
      for (int i = 0; i < tagsArray.length(); i++) {
        if (tagsArray.getString(i).equals(tag)) {
          tagsArray.remove(i);
          this.setTags(tagsArray.toString());
          return;
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public Long getIdentifier() {
    return (Long) this.targetObjectIdentity.getIdentifier();
  }

  public List<String> getTagsList() {
    try {
      JSONArray jsonArray = new JSONArray(this.getTags());
      ArrayList<String> list = new ArrayList<String>();
      for (int i = 0; i < jsonArray.length(); i++) {
        list.add(jsonArray.getString(i));
      }
      return list;
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }
  }
}
