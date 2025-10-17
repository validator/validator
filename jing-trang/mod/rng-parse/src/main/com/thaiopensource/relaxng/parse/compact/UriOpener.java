package com.thaiopensource.relaxng.parse.compact;

import com.thaiopensource.relaxng.parse.BuildException;
import org.xml.sax.InputSource;

public interface UriOpener {
  InputSource resolve(String href, String base) throws BuildException;
  InputSource open(InputSource in) throws BuildException;
}
