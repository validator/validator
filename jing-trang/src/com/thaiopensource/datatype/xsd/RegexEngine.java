package com.thaiopensource.datatype.xsd;

public interface RegexEngine {
  Regex compile(String str) throws InvalidRegexException;
}
