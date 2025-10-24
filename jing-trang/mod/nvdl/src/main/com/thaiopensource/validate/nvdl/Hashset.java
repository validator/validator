package com.thaiopensource.validate.nvdl;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Utility class, stores a set of objects. 
 * It uses a Hashtable for internal storage.
 */
class Hashset {
  /**
   * The internal storage, a hashtable.
   */
  private final Hashtable table = new Hashtable();

  /**
   * Test if an object belongs to this set or not.
   * @param key The object.
   * @return true if the object is contained in this set.
   */
  boolean contains(Object key) {
    return table.get(key) != null;
  }

  /**
   * Adds an object to this set.
   * @param key The object to be added.
   */
  void add(Object key) {
    table.put(key, key);
  }

  /**
   * Adds all the objects from another set to this set - union.
   * @param set The other set.
   */
  void addAll(Hashset set) {
    for (Enumeration e = set.table.keys(); e.hasMoreElements();)
      add(e.nextElement());
  }

  /**
   * Removes all the objects from this set.
   */
  void clear() {
    table.clear();
  }

  /**
   * Get an enumeration will all the objects from this set.
   * @return an enumeration with all the objects from this set.
   */
  Enumeration members() {
    return table.keys();
  }
}
