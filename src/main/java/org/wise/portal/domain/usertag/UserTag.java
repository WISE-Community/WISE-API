package org.wise.portal.domain.usertag;

import org.wise.portal.domain.Persistable;
import org.wise.portal.domain.user.User;

public interface UserTag extends Persistable {

  String getText();

  void setText(String text);

  User getUser();
}