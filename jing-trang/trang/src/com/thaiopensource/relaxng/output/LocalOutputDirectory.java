package com.thaiopensource.relaxng.output;

import java.io.Writer;
import java.io.IOException;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.HashMap;

public class LocalOutputDirectory implements OutputDirectory {
  private final File mainOutputFile;
  private final String lineSeparator;
  private final String extension;
  private final String encoding;
  // maps URIs to filenames
  private final Map uriMap = new HashMap();

  public LocalOutputDirectory(File mainOutputFile, String extension, String encoding) {
    this.mainOutputFile = mainOutputFile;
    this.extension = extension;
    this.encoding = encoding;
    this.lineSeparator = System.getProperty("line.separator");
    String name = mainOutputFile.getName();
  }

  public Writer open(String sourceUri) throws IOException {
    File file;
    if (sourceUri == MAIN)
      file = mainOutputFile;
    else
      file = new File(mainOutputFile.getParentFile(), mapFilename(sourceUri));
    return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), encoding);
  }

  public String reference(String fromSourceUri, String toSourceUri) {
    return mapFilename(toSourceUri);
  }

  private String mapFilename(String sourceUri) {
    String filename = (String)uriMap.get(sourceUri);
    if (filename == null) {
      filename = chooseFilename(sourceUri);
      uriMap.put(sourceUri, filename);
    }
    return filename;
  }

  private String chooseFilename(String sourceUri) {
    String filename = sourceUri.substring(sourceUri.lastIndexOf('/') + 1);
    int dot = filename.lastIndexOf('.');
    String base = dot < 0 ? filename : filename.substring(0, dot);
    filename = base + extension;
    for (int i = 1; uriMap.containsValue(filename); i++)
      filename = base + Integer.toString(i) + extension;
    return filename;
  }

  public String getLineSeparator() {
    return lineSeparator;
  }

  public String getEncoding() {
    return encoding;
  }
}
