package org.wise.portal.service.project.translation.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.wise.portal.domain.project.Project;
import org.wise.portal.service.project.translation.TranslateProjectService;

@Service
public class TranslateProjectServiceImpl implements TranslateProjectService {

  @Autowired
  private Environment appProperties;

  public void saveTranslations(Project project, String locale, String translations)
      throws IOException {
    String translationFilePath = appProperties.getProperty("curriculum_base_dir")
        + project.getModulePath().replace("project.json", "translations." + locale + ".json");
    Writer writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(new File(translationFilePath)), "UTF-8"));
    writer.write(translations);
    writer.close();
  }
}
