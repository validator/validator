package com.thaiopensource.relaxng.translate.util;

public class AbstractParam implements Param {

  public boolean allowRepeat() {
    return false;
  }

  public void set(String value) throws InvalidValueException, ValuePresenceException {
    throw new ValuePresenceException();
  }

  public void set(boolean value) throws InvalidValueException, ValuePresenceException {
    throw new ValuePresenceException();
  }
}
