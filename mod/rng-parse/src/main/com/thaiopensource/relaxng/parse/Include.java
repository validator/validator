package com.thaiopensource.relaxng.parse;

public interface Include extends GrammarSection {
  void endInclude(String href, String base, String ns,
                  Location loc, Annotations anno) throws BuildException, IllegalSchemaException;
}
