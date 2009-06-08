/**
 * Copyright 2009 Marc Stogaitis and Mimi Sun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gmote.server.updater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gmote.server.updater.Updater.UpdateTask;

/**
 * Unzips a file to a directory.
 * @author Marc Stogaitis
 *
 */
public class FileExtractor {
  private static final Logger LOGGER = Logger.getLogger(FileExtractor.class.getName());

  @SuppressWarnings("unchecked")
  public static void unzipArchive(File archive, File outputDir, UpdateTask updateTask) throws IOException {

    ZipFile zipfile = new ZipFile(archive);
    updateTask.setProgressBarSize(zipfile.size());
    int count = 0;
    for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) e.nextElement();
      unzipEntry(zipfile, entry, outputDir);
      
      count++;
      if (count % 10 == 0) {
        updateTask.setProgressForTask(count);
      }
    }
    updateTask.setProgressForTask(zipfile.size());
    updateTask.updateStatusLabel("abc " + zipfile.size());
  }

  private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

    if (entry.isDirectory()) {
      new File(outputDir, entry.getName()).mkdir();
      return;
    }

    File outputFile = new File(outputDir, entry.getName());
    if (!outputFile.getParentFile().exists()) {
      outputFile.getParentFile().mkdirs();
    }

    LOGGER.info("Extracting: " + entry);
    BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

    try {
      copy(inputStream, outputStream);
    } finally {
      outputStream.close();
      inputStream.close();
    }
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    if (in == null)
      throw new NullPointerException("InputStream is null");
    if (out == null)
      throw new NullPointerException("OutputStream is null");
    
    // Transfer bytes from in to out
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }
}