package com.thaiopensource.relaxng.parse;

import org.xml.sax.SAXException;

public class BuildException extends RuntimeException {
  private final Throwable cause;
  public BuildException(Throwable cause) {
    if (cause == null)
      throw new NullPointerException("null cause");
    this.cause = cause;
  }

  public Throwable getCause() {
    return cause;
  }

  public static BuildException fromSAXException(SAXException e) {
    Exception inner = e.getException();
    if (inner instanceof BuildException)
      return (BuildException)inner;
    return new BuildException(e);
  }
}
