package com.thaiopensource.validate;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;

/**
 * A SAX 2 content handler that validates the content it receives.  A <code>ValidatorHandler</code>
 * is <em>not</em> safe for concurrent access for multiple threads. A single <code>ValidatorHandler</code>
 * can be used to validate only a single document at a time. It can be used to validate a sequence of
 * documents by calling <code>reset</code> between documents.  If multiple documents must be validated
 * concurrently, then a separate <code>ValidatorHandler</code> must be used for each document.
 * <code>ValidatorHandler</code> extends <code>DTDHandler</code> so that validation can have access
 * to information about the DTD, specifically the unparsed entities and notations.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface Validator {
  /**
   * Reports whether the content received so far is valid.  If this is called before
   * <code>endDocument</code>, then it reports whether the content received so far
   * is such that subsequent content may result in a valid document.  If this is
   * called after <code>endDocument</code>, then it reports whether the content
   * received constitutes a valid document.
   *
   * @return <code>true</code> if the content is valid; <code>false</code> otherwise.
   */
  boolean isValidSoFar();

  /**
   * Prepares to receive the content of another document.  Immediately after <code>reset</code> is called
   * <code>isValidSoFar</code> will return <code>true</code> and <code>isComplete</code> will return false.
   * The current <code>ErrorHandler</code> is not affected.
   */
  void reset();

  ContentHandler getContentHandler();
  DTDHandler getDTDHandler();
}
