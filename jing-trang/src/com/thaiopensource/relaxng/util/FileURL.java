package com.thaiopensource.relaxng.util;

import java.io.File;
import java.net.URL;

public class FileURL {
  /**
   * Converts a File to a URL.
   */
  static public URL fileToURL(File file) {
    String path = file.getAbsolutePath();
    String fSep = System.getProperty("file.separator");
    if (fSep != null && fSep.length() == 1)
      path = path.replace(fSep.charAt(0), '/');
    if (path.length() > 0 && path.charAt(0) != '/')
      path = '/' + path;
    try {
      return new URL("file", "", path);
    }
    catch (java.net.MalformedURLException e) {
      /* According to the spec this could only happen if the file
	 protocol were not recognized. */
      throw new Error("unexpected MalformedURLException");
    }
  }
}
