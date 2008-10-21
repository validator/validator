package com.thaiopensource.datatype.xsd.regex.xerces2;

import com.thaiopensource.datatype.xsd.regex.RegexEngine;
import com.thaiopensource.datatype.xsd.regex.Regex;
import com.thaiopensource.datatype.xsd.regex.RegexSyntaxException;

import org.apache.xerces.impl.xpath.regex.RegularExpression;
import org.apache.xerces.impl.xpath.regex.ParseException;

/**
 * An implementation of <code>RegexEngine</code> using the Xerces 2 regular expression
 * implementation.
 */
public class RegexEngineImpl implements RegexEngine {
  public RegexEngineImpl() {
    // Force a linkage error on instantiation if the Xerces classes
    // are not available.
    try {
      new RegularExpression("", "X");
    }
    catch (ParseException e) {
    }
  }
  public Regex compile(String expr) throws RegexSyntaxException {
    try {
      final RegularExpression re = new RegularExpression(expr, "X");
      return new Regex() {
	  public boolean matches(String str) {
	    return re.matches(str);
	  }
	};
    }
    catch (ParseException e) {
      throw new RegexSyntaxException(e.getMessage(), e.getLocation());
    }
  }
}
