package com.thaiopensource.relaxng.output.dtd;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.util.Localizer;

class ErrorReporter {
  private Localizer localizer = new Localizer(ErrorReporter.class);
  private ErrorHandler eh;
  boolean hadError = false;

  static class WrappedSAXException extends RuntimeException {
    private SAXException exception;

    private WrappedSAXException(SAXException exception) {
      this.exception = exception;
    }

    public SAXException getException() {
      return exception;
    }
  }

  ErrorReporter(ErrorHandler eh) {
    this.eh = eh;
  }

  void error(String key, SourceLocation loc) {
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

  void error(String key, String arg, SourceLocation loc) {
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

  void error(String key, String arg1, String arg2, SourceLocation loc) {
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

  void warning(String key, SourceLocation loc) {
    if (eh == null)
      return;
    try {
      eh.warning(makeParseException(localizer.message(key), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  void warning(String key, String arg, SourceLocation loc) {
    if (eh == null)
      return;
    try {
      eh.warning(makeParseException(localizer.message(key, arg), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }

  void warning(String key, String arg1, String arg2, SourceLocation loc) {
    if (eh == null)
      return;
    try {
      eh.warning(makeParseException(localizer.message(key, arg1, arg2), loc));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
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
}
