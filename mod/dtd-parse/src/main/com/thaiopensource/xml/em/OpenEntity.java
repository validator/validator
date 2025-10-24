package com.thaiopensource.xml.em;

import java.io.Reader;

/**
 * Information about an open external entity.
 * This is used to by <code>EntityManager</code> to return
 * information about an external entity that is has opened.
 * @see EntityManager
 */
public class OpenEntity {
  private final Reader reader;
  private final String baseUri;
  private final String location;
  private final String encoding;

  /**
   * Creates and initializes an <code>OpenEntity</code>. which uses
   */
  public OpenEntity(Reader reader, String location, String baseUri, String encoding) {
    this.reader = reader;
    this.location = location;
    this.baseUri = baseUri;
    this.encoding = encoding;
  }

  /**
   * Returns an Reader containing the entity's bytes.
   * If this is called more than once on the same
   * OpenEntity, it will return the same Reader.
   */
  public final Reader getReader() {
    return reader;
  }

  /**
   * Returns the URI to use as the base URI for resolving relative URIs
   * contained in the entity.
   */
  public final String getBaseUri() {
    return baseUri;
  }

  /**
   * Returns a string representation of the location of the entity
   * suitable for use in error messages.
   */
  public final String getLocation() {
    return location;
  }

  /**
   * Returns the encoding used by the entity or null if the encoding
   * that was used is unknown.
   */
  public final String getEncoding() {
    return encoding;
  }

}
