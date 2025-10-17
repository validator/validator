package com.thaiopensource.xml.sax;

public class ResolverInstantiationException extends Exception {
  private Exception exception;
  
  public ResolverInstantiationException(Exception exception) {
    super(exception.getMessage());
    this.exception = exception;
  }

  public ResolverInstantiationException(String message) {
    super(message);
    this.exception = null;
  }

  public Exception getException() {
    return exception;
  }
}
