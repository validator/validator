package com.thaiopensource.xml.dtd;

import java.io.IOException;

/**
 * This interface is used by the parser to access external entities.
 * @see Parser
 */
public interface EntityManager {
  /**
   * Opens an external entity.
   * @param systemId the system identifier specified in the entity declaration
   * @param baseUri the base URI relative to which the system identifier
   * should be resolved; null if no base URI is available
   * @param publicId the public identifier specified in the entity declaration;
   * null if no public identifier was specified
   */
  OpenEntity open(String systemId, String baseUri, String publicId) throws IOException;
}
