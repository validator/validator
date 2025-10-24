package com.thaiopensource.relaxng.parse;

public interface SubParser {
  SubParseable createSubParseable(String href, String base) throws BuildException;
}
