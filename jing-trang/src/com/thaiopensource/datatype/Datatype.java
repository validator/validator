package com.thaiopensource.datatype;

public interface Datatype {
  /**
   * Returns true if this datatype allows str and otherwise returns false.
   */
  boolean allows(String str, DatatypeContext content);

  /**
   * Returns null if objects of this datatype do not need to be assigned
   * to any class.
   */
  Object getAssignmentClass();
}
