package org.wise.portal.presentation.web.controllers.author.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.user.UserService;
import org.wise.vle.utils.FileManager;

import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

/**
 * Project Asset API endpoint
 *
 * @author Hiroki Terashima
 * @author Geoffrey Kwan
 */
@Controller
@RequestMapping("/api/author/project/asset")
@Secured({ "ROLE_AUTHOR" })
public class ProjectAssetAPIController {

  @Autowired
  protected UserService userService;

  @Autowired
  protected ProjectService projectService;

  @Autowired
  protected Environment appProperties;

  private String CLAMAV_ADDRESS = "127.0.0.1";
  private String EXCEEDED_MAX_PROJECT_SIZE_MESSAGE = "Exceeded project max asset size.\n"
      + "Please delete unused assets.\n\nContact WISE if your project needs more disk space.";
  private String UPLOADING_THIS_FILE_NOT_ALLOWED_MESSAGE = "Uploading this file is not allowed.";

  @GetMapping("/{projectId}")
  @ResponseBody
  protected Map<String, Object> getProjectAssets(Authentication auth, @PathVariable Long projectId)
      throws ObjectNotFoundException, IOException {
    Project project = projectService.getById(projectId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (projectService.canAuthorProject(project, user)) {
      return projectService.getDirectoryInfo(new File(getProjectAssetsDirectoryPath(project)));
    }
    return null;
  }

  @PostMapping("/{projectId}")
  @ResponseBody
  protected Map<String, Object> saveProjectAsset(Authentication auth, @PathVariable Long projectId,
      @RequestParam List<MultipartFile> files) throws ObjectNotFoundException, IOException {
    Project project = projectService.getById(projectId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (projectService.canAuthorProject(project, user)) {
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("success", new ArrayList<Object>());
      result.put("error", new ArrayList<Object>());
      String projectAssetsDirPath = getProjectAssetsDirectoryPath(project);
      File projectAssetsDir = new File(projectAssetsDirPath);
      ClamavClient clamavClient = getClamavClient();
      for (MultipartFile file : files) {
        addAsset(project, projectAssetsDir, file, user, result, clamavClient);
      }
      result.put("assetDirectoryInfo", projectService.getDirectoryInfo(projectAssetsDir));
      return result;
    }
    return null;
  }

  private ClamavClient getClamavClient() {
    ClamavClient clamavClient = null;
    try {
      clamavClient = new ClamavClient(CLAMAV_ADDRESS);
    } catch (ClamavException e) {
      e.printStackTrace();
    }
    return clamavClient;
  }

  @SuppressWarnings("unchecked")
  private void addAsset(Project project, File projectAssetsDir, MultipartFile file, User user,
      Map<String, Object> result, ClamavClient clamavClient) throws IOException {
    HashMap<String, String> fileObject = new HashMap<String, String>();
    fileObject.put("filename", file.getOriginalFilename());
    boolean isSuccess = false;
    if (clamavClient == null || isScanOk(clamavClient, file)) {
      if (!isUserAllowedToUpload(user, file)) {
        fileObject.put("message", UPLOADING_THIS_FILE_NOT_ALLOWED_MESSAGE);
      } else if (!isEnoughProjectDiskSpace(project, projectAssetsDir, file)) {
        fileObject.put("message", EXCEEDED_MAX_PROJECT_SIZE_MESSAGE);
      } else {
        Path path = Paths.get(projectAssetsDir.getPath(), file.getOriginalFilename());
        file.transferTo(path);
        isSuccess = true;
      }
    } else {
      fileObject.put("message", UPLOADING_THIS_FILE_NOT_ALLOWED_MESSAGE);
    }
    if (isSuccess) {
      ((ArrayList<HashMap<String, String>>) result.get("success")).add(fileObject);
    } else {
      ((ArrayList<HashMap<String, String>>) result.get("error")).add(fileObject);
    }
  }

  private boolean isScanOk(ClamavClient clamavClient, MultipartFile file) throws IOException {
    boolean result = false;
    try {
      result = clamavClient.scan(file.getInputStream()) instanceof ScanResult.OK;
    } catch (ClamavException e) {
      e.printStackTrace();
    }
    return result;
  }

  private boolean isUserAllowedToUpload(User user, MultipartFile file) {
    String allowedTypes = appProperties.getProperty("normalAuthorAllowedProjectAssetContentTypes");
    if (user.isTrustedAuthor()) {
      allowedTypes += ","
          + appProperties.getProperty("trustedAuthorAllowedProjectAssetContentTypes");
    }
    try {
      return allowedTypes.contains(getRealMimeType(file));
    } catch (IOException e) {
      return false;
    }
  }

  private boolean isEnoughProjectDiskSpace(Project project, File projectAssetsDir,
      MultipartFile file) {
    long sizeOfAssetsDirectory = FileUtils.sizeOfDirectory(projectAssetsDir);
    Long projectMaxTotalAssetsSize = project.getMaxTotalAssetsSize();
    if (projectMaxTotalAssetsSize == null) {
      projectMaxTotalAssetsSize = Long
          .parseLong(appProperties.getProperty("project_max_total_assets_size", "15728640"));
    }
    return sizeOfAssetsDirectory + file.getSize() > projectMaxTotalAssetsSize;
  }

  private String getRealMimeType(MultipartFile file) throws IOException {
    AutoDetectParser parser = new AutoDetectParser();
    Detector detector = parser.getDetector();
    Metadata metadata = new Metadata();
    TikaInputStream stream = TikaInputStream.get(file.getInputStream());
    org.apache.tika.mime.MediaType mediaType = detector.detect(stream, metadata);
    return mediaType.toString();
  }

  @PostMapping("/{projectId}/delete")
  @ResponseBody
  protected Map<String, Object> deleteProjectAsset(Authentication auth,
      @PathVariable Long projectId, @RequestParam String assetFileName)
      throws ObjectNotFoundException {
    Project project = projectService.getById(projectId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (projectService.canAuthorProject(project, user)) {
      String projectAssetsDirPath = getProjectAssetsDirectoryPath(project);
      if (FileManager.isFilePathInFolder(projectAssetsDirPath, assetFileName)) {
        File asset = new File(projectAssetsDirPath, assetFileName);
        asset.delete();
        return projectService.getDirectoryInfo(new File(projectAssetsDirPath));
      }
    }
    return null;
  }

  private String getProjectAssetsDirectoryPath(Project project) {
    String curriculumBaseDir = appProperties.getProperty("curriculum_base_dir");
    String rawProjectUrl = project.getModulePath();
    String projectURL = curriculumBaseDir + rawProjectUrl;
    String projectBaseDir = projectURL.substring(0, projectURL.indexOf("project.json"));
    return projectBaseDir + "/assets";
  }

  @GetMapping(value = "/{projectId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ResponseBody
  protected FileSystemResource downloadProjectAsset(Authentication auth,
      HttpServletResponse response, @PathVariable Long projectId,
      @RequestParam String assetFileName) throws ObjectNotFoundException {
    Project project = projectService.getById(projectId);
    User user = userService.retrieveUserByUsername(auth.getName());
    if (projectService.canAuthorProject(project, user)) {
      String folderPath = getProjectAssetsDirectoryPath(project);
      if (FileManager.isFilePathInFolder(folderPath, assetFileName)) {
        response.setHeader("Content-Disposition", "attachment;filename=\"" + assetFileName + "\"");
        return new FileSystemResource(getProjectAssetsDirectoryPath(project) + "/" + assetFileName);
      }
    }
    return null;
  }

}
