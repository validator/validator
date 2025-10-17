package com.thaiopensource.validate.nvdl;

/**
 * Possible values for match, that is represented as a list of elements and attributes.
 */
class ElementsOrAttributes {
  /**
   * Flag for elements.
   */
  private static final int ELEMENTS_FLAG = 01;
  
  /**
   * Flag for attributes.
   */
  private static final int ATTRIBUTES_FLAG = 02;

  // Define constants for all possible values.
  /**
   * Neither elements nor attributes specified.
   */
  static final ElementsOrAttributes NEITHER = new ElementsOrAttributes(0);
  /**
   * Only elements is specified.
   */
  static final ElementsOrAttributes ELEMENTS = new ElementsOrAttributes(ELEMENTS_FLAG);
  
  /**
   * Only attributes is specified.
   */
  static final ElementsOrAttributes ATTRIBUTES = new ElementsOrAttributes(ATTRIBUTES_FLAG);
  
  /**
   * Bothe elements and attributes are specified.
   */
  static final ElementsOrAttributes BOTH = new ElementsOrAttributes(ELEMENTS_FLAG|ATTRIBUTES_FLAG);

  /**
   * All possible values.
   */
  private static final ElementsOrAttributes values[] = {
    NEITHER,
    ELEMENTS,
    ATTRIBUTES,
    BOTH
  };

  /**
   * Stores this instance flags.
   */
  private int flags = 0;

  /**
   * Creates an instance with the given flags.
   * @param flags
   */
  private ElementsOrAttributes(int flags) {
    this.flags = flags;
  }

  /**
   * Get the value after adding elements to the current instance.
   * @return The value that matches also elements.
   */
  ElementsOrAttributes addElements() {
    return values[flags | ELEMENTS_FLAG];
  }

  /**
   * Get the value after adding attributes to the current instance.
   * @return The value that matches also attributes.
   */
  ElementsOrAttributes addAttributes() {
    return values[flags | ATTRIBUTES_FLAG];
  }

  /**
   * Checks whether the attributes are matched or not. 
   * @return true is attributes are matched.
   */
  boolean containsAttributes() {
    return (flags & ATTRIBUTES_FLAG) != 0;
  }

  /**
   * Checks whether the elements are matched or not. 
   * @return true is elements are matched.
   */
  boolean containsElements() {
    return (flags & ELEMENTS_FLAG) != 0;
  }

}
