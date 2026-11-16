package org.wise.portal.service.notebook;

import java.io.Serial;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.stereotype.Service;
import org.wise.vle.domain.work.NotebookItem;
import org.wise.vle.domain.work.NotebookItemSerializer;

@Service
public class NotebookItemJsonModule extends SimpleModule {

  @Serial
  private static final long serialVersionUID = 1L;

  public NotebookItemJsonModule() {
    this.addSerializer(NotebookItem.class, new NotebookItemSerializer());
  }
}
