package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.BuildException;
import javax.xml.transform.sax.SAXSource;

public interface UriResolver {
  SAXSource resolve(String href, String base) throws BuildException;
}
