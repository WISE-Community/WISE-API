package org.wise.portal.domain.tags;

import java.util.List;

import org.wise.portal.domain.Persistable;

public interface Tags extends Persistable {

  String getTags();

  void setTags(String tags);

  boolean containsTag(String tag);

  void addTag(String tag);

  void removeTag(String tag);

  Long getIdentifier();

  List<String> getTagsList();
}
