package com.thaiopensource.xml.dtd;

public class VisitException extends Exception {
  private final Throwable throwable;
  
  public VisitException(Throwable throwable) {
    this.throwable = throwable;
  }

  public Throwable getWrapped() {
    return throwable;
  }
}
