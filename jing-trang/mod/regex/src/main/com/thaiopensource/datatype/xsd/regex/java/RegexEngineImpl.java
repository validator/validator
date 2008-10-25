package com.thaiopensource.datatype.xsd.regex.java;

import com.thaiopensource.datatype.xsd.regex.Regex;
import com.thaiopensource.datatype.xsd.regex.RegexEngine;
import com.thaiopensource.datatype.xsd.regex.RegexSyntaxException;

import java.util.regex.Pattern;

/**
 * An implementation of <code>RegexEngine</code> using the JDK >= 1.4 <code>java.util.regex</code>
 * package.
 */
public class RegexEngineImpl implements RegexEngine {
  public RegexEngineImpl() {
    // Force a linkage error on instantiation if JDK >= 1.4 is not available.
    boolean b = RegexFeatures.SURROGATES_DIRECT;
  } 

  public Regex compile(String str) throws RegexSyntaxException {
    // Don't catch PatternSyntaxException
    // The Translator should detect all syntax errors
    final Pattern pattern = Pattern.compile(Translator.translate(str));
    return new Regex() {
      public boolean matches(String str) {
        return pattern.matcher(str).matches();
      }
    };
  }
}
