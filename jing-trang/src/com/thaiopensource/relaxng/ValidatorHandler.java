package com.thaiopensource.relaxng;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;

/**
 * A SAX 2 content handler that validates the content it receives.  A <code>ValidatorHandler</code>
 * is <em>not</em> safe for concurrent access for multiple threads. A single <code>ValidatorHandler</code>
 * can be used to validate only a single document at a time. It can be used to validate a sequence of
 * documents by calling <code>reset</code> between documents.  If multiple documents must be validated
 * concurrently, then a separate <code>ValidatorHandler</code> must be used for each document.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface ValidatorHandler extends ContentHandler {
  /**
   * Reports whether the content received so far is valid.  If this is called before
   * <code>endDocument</code>, then it reports whether the content received so far
   * is such that subsequent content may result in a valid document.  If this is
   * called after <code>endDocument</code>, then it reports whether the content
   * received constitutes a valid document.
   *
   * @return <code>true</code> if the content is valid; <code>false</code> otherwise.
   */
  boolean isValid();

  /**
   * Reports whether the complete document has been received, that is, whether
   * <code>endDocument</code> has been called.
   *
   * @return <code>true</code> if the complete document has been received; <code>false</code>
   * otherwise.
   */
  boolean isComplete();

  /**
   * Prepares to receive the content of another document.  Immediately after <code>reset</code> is called
   * <code>isValid</code> will return <code>true</code> and <code>isComplete</code> will return false.
   * The current <code>ErrorHandler</code> is not affected.
   */
  void reset();

  /**
   * Sets the current <code>ErrorHandler</code> to be used for reporting validation errors.  This
   * may be called at any time, even after this <code>ValidatorHandler</code> has started to
   * received content.
   *
   * @param eh the error handler to use for reporting errors; <code>null</code> if errors should
   * not be reported
   */
  void setErrorHandler(ErrorHandler eh);

  /**
   * Returns the current <code>ErrorHandler</code> as set by <code>setErrorHandler</code>.
   *
   * @return the current <code>ErrorHandler</code>; maybe <code>null</code> if no <code>ErrorHandler</code>
   * has been set.
   */
  ErrorHandler getErrorHandler();
}
