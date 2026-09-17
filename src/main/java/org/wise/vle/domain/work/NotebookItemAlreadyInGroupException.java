package org.wise.vle.domain.work;

import java.io.Serial;

public class NotebookItemAlreadyInGroupException extends Exception {
  @Serial
  private static final long serialVersionUID = 1L;

  public NotebookItemAlreadyInGroupException(NotebookItem notebookItem, String group) {
    super("Notebook Item " + notebookItem.getId() + " is already in group " + group);
  }
}
