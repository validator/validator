package com.thaiopensource.relaxng.util;

import java.io.File;
import java.net.URL;

/**
 * Provides a static method for converting a <code>File</code> to a <code>URL</code>.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class FileURL {
  /**
   * Prevents creating an instance of this class.
   */
  private FileURL() { }
  /**
   * Converts a <code>File</code> to a <code>URL</code>.
   *
   * @param file the <code>File</code> to convert
   * @return a <code>URL</code> locating the specified <code>File</code>.
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
