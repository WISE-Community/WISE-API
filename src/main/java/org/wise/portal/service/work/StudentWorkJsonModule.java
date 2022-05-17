package org.wise.portal.service.work;

import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.vle.domain.work.StudentWork;
import org.wise.vle.domain.work.StudentWorkDeserializer;
import org.wise.vle.domain.work.StudentWorkSerializer;

@Service
public class StudentWorkJsonModule extends SimpleModule {

  private static final long serialVersionUID = 1L;

  public StudentWorkJsonModule() {}

  @Autowired
  public StudentWorkJsonModule(StudentWorkSerializer serializer, StudentWorkDeserializer deserializer) {
    this.addSerializer(StudentWork.class, serializer);
    this.addDeserializer(StudentWork.class, deserializer);
  }
}
