/**
 * Copyright (c) 2008-2017 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.vle.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

/**
 * Servlet implementation class for Servlet: FileManager
 *
 * @author Patrick Lawler
 */
@Component
public class FileManager {
  static final long serialVersionUID = 1L;

  /**
   * Copies the given <code>File</code> src to the given <code>File</code> dest. If they are
   * directories, recursively copies the contents of the directories.
   *
   * @param src
   * @param dest
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void copy(File src, File dest) throws IOException {
    if (src.isDirectory()) {
      if (!dest.exists()) {
        dest.mkdir();
      }
      String[] files = src.list();
      for (int a = 0; a < files.length; a++) {
        copy(new File(src, files[a]), new File(dest, files[a]));
      }
    } else {
      InputStream in = new FileInputStream(src);
      FileOutputStream out = new FileOutputStream(dest);
      byte[] buffer = new byte[2048];
      int len;
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      in.close();
      out.close();
    }
  }

  /**
   * Import the asset from one project asset folder to another project asset folder
   *
   * @param fromAssetFileName
   *                                  the name of the file in the asset folder
   * @param fromAssetFileContent
   *                                  (optional) the content that we want to save to the to asset.
   *                                  if this is not provided we will obtain the content from the
   *                                  fromAssetFileName handle. this parameter is used when the
   *                                  content in the fromAssetFileName needs to be modified such as
   *                                  when file name references in the content need to be changed
   *                                  due to file name conflicts.
   * @param fromProjectAssetsFolderPath the asset folder path in the from project
   * @param toProjectAssetsFolderPath the asset folder path in the to project
   * @return the name of the asset file that was created in the to project asset folder or null if
   *         we were unable to create the asset in the to project asset folder
   */
  public static String importAssetInContent(String fromAssetFileName, String fromAssetFileContent,
      String fromProjectAssetsFolderPath, String toProjectAssetsFolderPath) {
    String toAssetFileName = null;
    String toAssetFileContent = null;
    File fromAsset = new File(fromProjectAssetsFolderPath, fromAssetFileName);
    if (!FileManager.isFilePathInFolder(fromProjectAssetsFolderPath, fromAssetFileName)) {
      return null;
    }
    if (fromAsset.exists()) {
      toAssetFileName = fromAssetFileName;
      File toAsset = new File(toProjectAssetsFolderPath, toAssetFileName);

      boolean assetCompleted = false;
      int counter = 1;

      /*
       * this while loop will check if the file already exists.
       *
       * if the file already exists, we will check if the content in the "from" file is the same as
       * in the "to" file. if the content is the same, we do not need to do anything. if the content
       * is different, we will look for another file name to use. if the file does not exist, we
       * will make it.
       */
      while (!assetCompleted) {
        if (toAsset.exists()) {
          try {
            toAssetFileContent = FileUtils.readFileToString(toAsset);
          } catch (IOException e1) {
            e1.printStackTrace();
          }

          try {
            boolean contentMatches = false;
            if (fromAssetFileContent != null) {
              /*
               * the from asset file content was passed in so we will compare it with the to asset
               * file content
               */
              if (fromAssetFileContent.equals(toAssetFileContent)) {
                contentMatches = true;
              }
            } else if (FileUtils.contentEquals(fromAsset, toAsset)) {
              contentMatches = true;
            }

            if (contentMatches) {
              assetCompleted = true;
            } else {
              toAssetFileName = createNewFileName(fromAssetFileName, counter);
              toAsset = new File(toProjectAssetsFolderPath, toAssetFileName);
              counter++;
            }
          } catch (IOException e) {
            e.printStackTrace();
            break;
          }
        } else {
          try {
            if (fromAssetFileContent != null) {
              FileUtils.write(toAsset, fromAssetFileContent);
            } else {
              FileUtils.copyFile(fromAsset, toAsset);
            }
            assetCompleted = true;
          } catch (IOException e) {
            e.printStackTrace();
            break;
          }
        }
      }
    }
    return toAssetFileName;
  }

  /**
   * Create a new file name by adding '-' and a number to the end of the file name.
   *
   * before myPicture.jpg after myPicture-1.jpg
   *
   * @param fileName
   *                   the current file name
   * @param counter
   *                   the number to add to the file name
   * @return a new file name with '-' and a number added to the end
   */
  public static String createNewFileName(String fileName, int counter) {
    String newFileName = "";
    int lastDot = fileName.lastIndexOf(".");
    String fileNameBeginning = fileName.substring(0, lastDot);
    String fileNameEnding = fileName.substring(lastDot);
    newFileName = fileNameBeginning + "-" + counter + fileNameEnding;
    return newFileName;
  }

  /**
   * Make sure the file is actually in the folder to prevent file path traversal exploits. If the
   * file path is something like "../../../../password.txt" then we will see that the file is not
   * actually in the folder.
   * @param folderPath The path to the folder
   * Example
   * "src/main/webapp/curriculum/15/assets"
   * @param filePath The file name or path relative to the folder
   * Example
   * "my-image.jpg"
   * "model/my-image.jpg"
   * "../my-image.jpg"
   * "../../my-image.jpg"
   * @return Boolean whether the file is actually in the folder.
   */
  public static boolean isFilePathInFolder(String folderPath, String filePath) {
    try {
      File folder = new File(folderPath);
      File file = new File(folderPath, filePath);
      String folderCononicalPath = folder.getCanonicalPath();
      String fileCononicalPath = file.getCanonicalPath();
      return fileCononicalPath.startsWith(folderCononicalPath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}
