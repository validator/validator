package com.thaiopensource.datatype;

public interface Datatype {
  /**
   * Returns true if this datatype allows str and otherwise returns false.
   */
  boolean allows(String str, DatatypeContext context);

  /**
   * Create an object representing the value, or return null if the
   * string does not represent a value.
   */
  Object createValue(String str, DatatypeContext context);
}
