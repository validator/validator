package com.thaiopensource.relaxng.translate.util;

public class AbstractParam implements Param {

  public boolean allowRepeat() {
    return false;
  }

  public void set(String value) throws InvalidParamValueException, ParamValuePresenceException {
    throw new ParamValuePresenceException();
  }

  public void set(boolean value) throws InvalidParamValueException, ParamValuePresenceException {
    throw new ParamValuePresenceException();
  }
}
