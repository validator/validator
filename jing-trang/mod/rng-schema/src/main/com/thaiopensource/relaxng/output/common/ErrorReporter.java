package com.thaiopensource.relaxng.output.common;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.util.Localizer;

public class ErrorReporter {
  private final Localizer localizer;
  private final ErrorHandler eh;
  private boolean hadError = false;

  static public class WrappedSAXException extends RuntimeException {
    private final SAXException exception;

    private WrappedSAXException(SAXException exception) {
      this.exception = exception;
    }

    public SAXException getException() {
      return exception;
    }
  }

  public ErrorReporter(ErrorHandler eh, Class cls) {
    this.eh = eh;
    this.localizer = new Localizer(cls);
  }

  public void error(String key, SourceLocation loc) {
    hadError = true;
    if (eh == null)
      return;
    try {
      eh.error(makeParseException(localizer.message(key), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  public void error(String key, String arg, SourceLocation loc) {
    hadError = true;
    if (eh == null)
      return;
    try {
      eh.error(makeParseException(localizer.message(key, arg), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  public void error(String key, String arg1, String arg2, SourceLocation loc) {
    hadError = true;
    if (eh == null)
      return;
    try {
      eh.error(makeParseException(localizer.message(key, arg1, arg2), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  public void warning(String key, SourceLocation loc) {
    if (eh == null)
      return;
    try {
      eh.warning(makeParseException(localizer.message(key), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  public void warning(String key, String arg, SourceLocation loc) {
    if (eh == null)
      return;
    try {
      eh.warning(makeParseException(localizer.message(key, arg), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  public void warning(String key, String arg1, String arg2, SourceLocation loc) {
    if (eh == null)
      return;
    try {
      eh.warning(makeParseException(localizer.message(key, arg1, arg2), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  public boolean getHadError() {
    return hadError;
  }

  private static SAXParseException makeParseException(String message, SourceLocation loc) {
    if (loc == null)
      return new SAXParseException(message, null);
    return new SAXParseException(message,
                                 null,
                                 loc.getUri(),
                                 loc.getLineNumber(),
                                 loc.getColumnNumber());
  }

  public Localizer getLocalizer() {
    return localizer;
  }
}
