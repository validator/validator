package com.thaiopensource.datatype.xsd;

class NullRegexEngine implements RegexEngine {
  public Regex compile(String re) {
    return new Regex() {
	public boolean matches(String str) {
	  return true;
	}
      };
  }
}
