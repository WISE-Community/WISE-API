package org.wise.vle.utils;

import static org.junit.Assert.assertEquals;
import org.easymock.EasyMockRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class FileManagerTest {

  @Test
  public void isFilePathInFolder_FilePathIsInFolder_ShouldReturnTrue() {
    assertIsFilePathInFolder("/src/main/webapp/curriculum/15/assets", "my-image.jpg", true);
  }

  @Test
  public void isFilePathInFolder_FilePathIsInChildFolder_ShouldReturnTrue() {
    assertIsFilePathInFolder("/src/main/webapp/curriculum/15/assets", "model/my-image.jpg", true);
  }

  @Test
  public void isFilePathInFolder_FilePathIsNotInFolder_ShouldReturnFalse() {
    assertIsFilePathInFolder("/src/main/webapp/curriculum/15/assets", "../my-image.jpg", false);
  }

  private void assertIsFilePathInFolder(String folderPath, String filePath, boolean isValid) {
    assertEquals(FileManager.isFilePathInFolder(folderPath, filePath), isValid);
  }
}
