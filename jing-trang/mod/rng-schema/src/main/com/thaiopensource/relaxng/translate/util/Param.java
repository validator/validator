package com.thaiopensource.relaxng.translate.util;

public interface Param {
  boolean allowRepeat();
  void set(String value) throws InvalidParamValueException, ParamValuePresenceException;
  void set(boolean value) throws InvalidParamValueException, ParamValuePresenceException;
}
