package com.thaiopensource.datatype.xsd.regex.xerces;

import com.thaiopensource.datatype.xsd.RegexEngine;
import com.thaiopensource.datatype.xsd.Regex;
import com.thaiopensource.datatype.xsd.InvalidRegexException;

import org.apache.xerces.utils.regex.RegularExpression;
import org.apache.xerces.utils.regex.ParseException;

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
  public Regex compile(String expr) throws InvalidRegexException {
    try {
      final RegularExpression re = new RegularExpression(expr, "X");
      return new Regex() {
	  public boolean matches(String str) {
	    return re.matches(str);
	  }
	};
    }
    catch (ParseException e) {
      throw new InvalidRegexException(e.getMessage(), e.getLocation());
    }
  }
}
