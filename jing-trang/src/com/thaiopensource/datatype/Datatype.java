package com.thaiopensource.datatype;

public interface Datatype {
  /**
   * Returns true if this datatype allows str and otherwise returns false.
   */
  boolean allows(String str, DatatypeContext content);
}
