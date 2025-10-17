package org.relaxng.datatype;

import org.xml.sax.Locator;

/**
 * An optional interface for exposing the SAX <code>Locator</code>
 * to datatypes.
 * 
 * @version $Id$
 * @author hsivonen@iki.fi
 */
public interface ValidationContext2 extends ValidationContext {
  
  /**
   * Returns the <code>Locator</code> from the underlying parser or
   * <code>null</code> if unavailable.
   * 
   * @return a <code>Locator</code> or <code>null</code>
   */
  public Locator getLocator();
}
