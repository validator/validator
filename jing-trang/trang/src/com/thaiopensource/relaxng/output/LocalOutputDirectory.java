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
  private final String outputExtension;
  private String defaultEncoding;
  private boolean alwaysUseDefaultEncoding;
  private final int lineLength;
  // maps URIs to filenames
  private final Map uriMap = new HashMap();
  private final String mainInputExtension;
  private int indent;

  public LocalOutputDirectory(String mainSourceUri, File mainOutputFile, String extension,
                              String encoding, int lineLength, int indent) {
    this.mainOutputFile = mainOutputFile;
    this.outputExtension = extension;
    this.defaultEncoding = encoding;
    this.lineSeparator = System.getProperty("line.separator");
    this.lineLength = lineLength;
    this.indent = indent;
    this.uriMap.put(mainSourceUri, mainOutputFile.getName());
    int slashOff = mainSourceUri.lastIndexOf('/');
    int dotOff = mainSourceUri.lastIndexOf('.');
    this.mainInputExtension = dotOff > 0 && dotOff > slashOff ? mainSourceUri.substring(dotOff) : "";
  }

  public void setEncoding(String encoding) {
    defaultEncoding = encoding;
    alwaysUseDefaultEncoding = true;
  }

  public OutputDirectory.Stream open(String sourceUri, String encoding) throws IOException {
    if (encoding == null || alwaysUseDefaultEncoding)
      encoding = defaultEncoding;
    String javaEncoding = EncodingMap.getJavaName(encoding);
    File file = new File(mainOutputFile.getParentFile(), mapFilename(sourceUri));
    return new OutputDirectory.Stream(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)),
							     javaEncoding),
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
    String base;
    if (filename.endsWith(mainInputExtension))
      base = filename.substring(0, filename.length() - mainInputExtension.length());
    else
      base = filename;
    filename = base + outputExtension;
    for (int i = 1; uriMap.containsValue(filename); i++)
      filename = base + Integer.toString(i) + outputExtension;
    return filename;
  }

  public String getLineSeparator() {
    return lineSeparator;
  }

  public int getLineLength() {
    return lineLength;
  }

  public int getIndent() {
    return indent;
  }

  public void setIndent(int indent) {
    this.indent = indent;
  }
}
