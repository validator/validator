package com.thaiopensource.relaxng.translate.util;

public interface Param {
  static class InvalidValueException extends Exception {
    public InvalidValueException() { }
    public InvalidValueException(String message) {
      super(message);
    }
  }
  static class ValuePresenceException extends Exception { }
  boolean allowRepeat();
  void set(String value) throws InvalidValueException, ValuePresenceException;
  void set(boolean value) throws InvalidValueException, ValuePresenceException;
}
