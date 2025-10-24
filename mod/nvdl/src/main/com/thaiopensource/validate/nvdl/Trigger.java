package com.thaiopensource.validate.nvdl;

import java.util.Set;

/**
 * Stores trigger information.
 * @author george
 */
public class Trigger {
  /**
   * The namespace name for the local element names.
   */
  String namespace;
  /**
   * List with local names.
   */
  Set elementNames;
  
  /**
   * Creates a trigger to store the elements that break sections
   * for a given namespace.
   * @param namespace The namespace for all the elements.
   * @param elementNames A list of local element names.
   */
  Trigger(String namespace, Set elementNames) {
    this.elementNames = elementNames;
    this.namespace = namespace;
  }
}