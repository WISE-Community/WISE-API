package org.wise.vle.web.wise5;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wise.portal.domain.run.impl.RunImpl;

@Secured("ROLE_TEACHER")
@Controller
@RequestMapping("/api/teacher/export/{runId}/studentAssets")
public class ExportStudentAssetsController extends ExportController {

  @Autowired
  private Environment appProperties;

  @GetMapping
  public void export(@PathVariable("runId") RunImpl run, HttpServletResponse response)
      throws IOException {
    if (canExport(run)) {
      Long runId = run.getId();
      String studentUploadsBaseDir = appProperties.getProperty("studentuploads_base_dir");
      String sep = System.getProperty("file.separator");
      String runStudentAssetsDir = studentUploadsBaseDir + sep + runId.toString() + sep;
      String zipFileName = runId.toString() + "_student_uploads.zip";
      response.setContentType("application/zip");
      response.addHeader("Content-Disposition", "attachment;filename=\"" + zipFileName + "\"");
      ServletOutputStream outputStream = response.getOutputStream();
      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));
      File zipFolder = new File(runStudentAssetsDir);
      addFolderToZip(zipFolder, out, runStudentAssetsDir);
      out.close();
    } else {
      sendUnauthorizedError(response);
    }
  }

  private void addFolderToZip(File folder, ZipOutputStream zip, String baseName)
      throws IOException {
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          String name = file.getAbsolutePath().substring(baseName.length());
          ZipEntry zipEntry = new ZipEntry(name + "/");
          zip.putNextEntry(zipEntry);
          zip.closeEntry();
          addFolderToZip(file, zip, baseName);
        } else {
          String fileName = file.getAbsolutePath().substring(baseName.length());
          ZipEntry zipEntry = new ZipEntry(fileName);
          zip.putNextEntry(zipEntry);
          IOUtils.copy(new FileInputStream(file), zip);
          zip.closeEntry();
        }
      }
    }
  }
}
