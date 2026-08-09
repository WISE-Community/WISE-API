package org.wise.portal.dao.authentication;

import org.wise.portal.dao.SimpleDao;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;

public interface TeacherUserDetailsDao extends SimpleDao<TeacherUserDetails> {
  
  boolean hasVerificationCode(String verificationCode);
}
