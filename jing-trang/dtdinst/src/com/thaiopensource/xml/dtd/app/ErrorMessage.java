package com.thaiopensource.xml.dtd.app;

public class ErrorMessage {
  public static final int ERROR = 0;
  public static final int WARNING = 1;

  private final int severity;
  private final String message;

  public ErrorMessage(int severity, String message) {
    this.severity = severity;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public int getSeverity() {
    return severity;
  }
}
