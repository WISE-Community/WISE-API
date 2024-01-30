package org.wise.portal.service.project.translation;

import java.io.IOException;
import org.wise.portal.domain.project.Project;

public interface TranslateProjectService {

  public void saveTranslations(Project project, String locale, String translations)
      throws IOException;
}
