package org.wise.portal.service.workgroup;

import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.stereotype.Service;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.WorkgroupSerializer;

@Service
public class WorkgroupJsonModule extends SimpleModule {

  private static final long serialVersionUID = 1L;

  public WorkgroupJsonModule() {
    this.addSerializer(Workgroup.class, new WorkgroupSerializer());
  }
}
