package com.thaiopensource.xml.dtd;

import java.io.IOException;

public class ParseException extends IOException {
  private String location;
  private int lineNumber;
  private int columnNumber;

  public ParseException(String message,
			String location,
			int lineNumber,
			int columnNumber) {
    super(message);
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
    this.location = location;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getColumnNumber() {
    return columnNumber;
  }

  public String getLocation() {
    return location;
  }

  public String getMessage() {
    return Localizer.message("MESSAGE",
			     new Object[] {
			       super.getMessage(),
			       location,
			       new Integer(lineNumber),
			       new Integer(columnNumber) });
  }
}
