package com.thaiopensource.relaxng.output;

import java.io.Writer;
import java.io.IOException;

public interface OutputDirectory {
  static final String MAIN = new String("#main");

  Writer open(String sourceUri) throws IOException;
  String reference(String fromSourceUri, String toSourceUri);
  String getLineSeparator();
  String getEncoding();
}
