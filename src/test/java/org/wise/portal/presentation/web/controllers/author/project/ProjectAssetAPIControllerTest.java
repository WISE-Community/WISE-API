package org.wise.portal.presentation.web.controllers.author.project;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

@ExtendWith(EasyMockExtension.class)
public class ProjectAssetAPIControllerTest extends APIControllerTest {

  @TestSubject
  private ProjectAssetAPIController projectAssetAPIController = new ProjectAssetAPIController();

  private static final String ALLOWED_CONTENT_TYPES = "text/plain,text/csv,text/xml,application/pdf,"
      + "image/gif,image/jpeg,image/png,image/svg+xml,image/gif,audio/mp3,audio/mp4,audio/mpeg,"
      + "audio/wav,audio/vnd.wave,audio/ogg,audio/webm,audio/x-aac,video/mpeg,video/mp4,video/ogg,"
      + "video/quicktime,video/x-flv,video/avi,video/webm";

  // Minimal valid 1x1 PNG image
  private static final byte[] PNG_BYTES = Base64.getDecoder().decode(
      "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

  // Minimal DOS/Windows PE executable signature (MZ header)
  private static final byte[] EXE_BYTES = new byte[] {
      'M', 'Z', (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00,
      0x04, 0x00, 0x00, 0x00, (byte) 0xff, (byte) 0xff, 0x00, 0x00
  };

  @Test
  public void saveProjectAsset_UploadPng_Allowed(@TempDir Path tempDir) throws Exception {
    File assetsDir = new File(tempDir.toFile(), "1/assets");
    assetsDir.mkdirs();

    MockMultipartFile pngFile = new MockMultipartFile(
        "files", "test.png", "image/png", PNG_BYTES);
    List<MultipartFile> files = Collections.singletonList(pngFile);

    expect(projectService.getById(projectId1)).andReturn(project1);
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    expect(appProperties.getProperty("curriculum_base_dir")).andReturn(tempDir.toString());
    expect(appProperties.getProperty("clamav.server.address")).andReturn(null);
    expect(appProperties.getProperty("normalAuthorAllowedProjectAssetContentTypes"))
        .andReturn(ALLOWED_CONTENT_TYPES);
    Map<String, Object> dirInfo = new HashMap<>();
    expect(projectService.getDirectoryInfo(anyObject(File.class))).andReturn(dirInfo);

    replay(projectService, userService, appProperties);

    Map<String, Object> result = projectAssetAPIController.saveProjectAsset(
        teacherAuth, projectId1, files);

    assertNotNull(result);
    List<Map<String, String>> successList = (List<Map<String, String>>) result.get("success");
    List<Map<String, String>> errorList = (List<Map<String, String>>) result.get("error");

    assertEquals(1, successList.size());
    assertEquals("test.png", successList.get(0).get("filename"));
    assertEquals(0, errorList.size());
    assertTrue(new File(assetsDir, "test.png").exists());

    verify(projectService, userService, appProperties);
  }

  @Test
  public void saveProjectAsset_UploadExe_NotAllowed(@TempDir Path tempDir) throws Exception {
    File assetsDir = new File(tempDir.toFile(), "1/assets");
    assetsDir.mkdirs();

    MockMultipartFile exeFile = new MockMultipartFile(
        "files", "test.exe", "application/octet-stream", EXE_BYTES);
    List<MultipartFile> files = Collections.singletonList(exeFile);

    expect(projectService.getById(projectId1)).andReturn(project1);
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(projectService.canAuthorProject(project1, teacher1)).andReturn(true);
    expect(appProperties.getProperty("curriculum_base_dir")).andReturn(tempDir.toString());
    expect(appProperties.getProperty("clamav.server.address")).andReturn(null);
    expect(appProperties.getProperty("normalAuthorAllowedProjectAssetContentTypes"))
        .andReturn(ALLOWED_CONTENT_TYPES);
    Map<String, Object> dirInfo = new HashMap<>();
    expect(projectService.getDirectoryInfo(anyObject(File.class))).andReturn(dirInfo);

    replay(projectService, userService, appProperties);

    Map<String, Object> result = projectAssetAPIController.saveProjectAsset(
        teacherAuth, projectId1, files);

    assertNotNull(result);
    List<Map<String, String>> successList = (List<Map<String, String>>) result.get("success");
    List<Map<String, String>> errorList = (List<Map<String, String>>) result.get("error");

    assertEquals(0, successList.size());
    assertEquals(1, errorList.size());
    assertEquals("test.exe", errorList.get(0).get("filename"));
    assertEquals("Uploading this file is not allowed.", errorList.get(0).get("message"));
    assertFalse(new File(assetsDir, "test.exe").exists());

    verify(projectService, userService, appProperties);
  }
}
