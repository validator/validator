package com.thaiopensource.xml.em;

import java.io.IOException;

/**
 * This interface is used by the parser to access external entities.
 * @see Parser
 */
public interface EntityManager {
  /**
   * Opens an external entity with the specified external identifier.
   */
  OpenEntity open(ExternalId xid) throws IOException;
}
