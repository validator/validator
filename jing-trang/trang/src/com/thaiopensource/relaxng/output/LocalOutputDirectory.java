package com.thaiopensource.relaxng.output;

import com.thaiopensource.xml.util.EncodingMap;
import com.thaiopensource.xml.out.CharRepertoire;

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
  private final String defaultEncoding;
  private final boolean alwaysUseDefaultEncoding;
  private final int lineLength;
  // maps URIs to filenames
  private final Map uriMap = new HashMap();

  public LocalOutputDirectory(String mainSourceUri, File mainOutputFile, String extension,
                              String encoding, boolean forceEncoding, int lineLength) {
    this.mainOutputFile = mainOutputFile;
    this.extension = extension;
    this.defaultEncoding = encoding;
    this.alwaysUseDefaultEncoding = forceEncoding;
    this.lineSeparator = System.getProperty("line.separator");
    this.lineLength = lineLength;
    this.uriMap.put(mainSourceUri, mainOutputFile.getName());
  }

  public Stream open(String sourceUri, String encoding) throws IOException {
    if (encoding == null || alwaysUseDefaultEncoding)
      encoding = defaultEncoding;
    String javaEncoding = EncodingMap.getJavaName(encoding);
    File file = new File(mainOutputFile.getParentFile(), mapFilename(sourceUri));
    return new Stream(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), javaEncoding),
                      encoding,
                      CharRepertoire.getInstance(javaEncoding));
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

  public int getLineLength() {
    return lineLength;
  }
}
