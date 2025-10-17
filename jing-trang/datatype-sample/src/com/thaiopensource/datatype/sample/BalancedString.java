package com.thaiopensource.datatype.sample;

public class BalancedString extends SimpleDatatypeLibrary {

  private static final String DATATYPE_LIBRARY 
    = "http://www.thaiopensource.com/relaxng/datatypes/sample";
  private static final String TYPE = "balancedString";

  public BalancedString() {
    super(DATATYPE_LIBRARY, TYPE);
    System.err.println("Loaded balanced string");
  }

  protected boolean isValid(String literal) {
    int len = literal.length();
    int level = 0;
    for (int i = 0; i < len; i++) {
      switch (literal.charAt(i)) {
      case '(':
	++level;
	break;
      case ')':
	if (--level < 0)
	  return false;
	break;
      }
    }
    return level == 0;
  }
}
