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

  /**
   * Returns true if some values make use of the context argument.
   * Returns false if no values make use of the context argument.
   */
  boolean isContextDependent();

  static final int ID_TYPE_NULL = 0;
  static final int ID_TYPE_ID = 1;
  static final int ID_TYPE_IDREF = 2;
  static final int ID_TYPE_IDREFS = 2;

  /**
   * Returns an integer representing the ID-type of the datatype as defined
   * in the RELAX NG DTD Compatibility Spec.
   */
  int getIdType();
}
