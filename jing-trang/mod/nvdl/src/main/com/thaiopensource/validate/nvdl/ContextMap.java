package com.thaiopensource.validate.nvdl;

import com.thaiopensource.util.Equal;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Keeps modes depending on context.
 * The structure of the context map is
 * 
 * stores the mode for 
 *  /  in rootValue
 *  "" in otherValue (this is for relative paths)
 * stores a hash with the last path elements as key and
 * ContextMap objects as values.
 * 
 * A path like a/b and mode x
 * will be represented by 3 ContextMap objects
 * ContextMap b ---> ContextMap a ---> ContextMap otherValue=x
 * 
 * Addind also /a/b and mode y will give
 * 
 * ContextMap b ---> ContextMap a ---> ContextMap (otherValue=x, rootValue=y)
 * 
 * Adding a2/b and mode w will give
 * 
 *  ContextMap b ---> ContextMap a ---> ContextMap (otherValue=x, rootValue=y)
 *                              a2 ---> ContextMap otherValue=w
 */
class ContextMap {
  /**
   * Stores the mode associated with an absolute path.
   */
  private Object rootValue;
  /**
   * Stores a mode associated with a relative path.
   */
  private Object otherValue;
  /**
   * Stores a hash map with with the key the last local name and 
   * as values other ContextMap objects.
   */
  private final Hashtable nameTable = new Hashtable();

  /**
   * Get the mode matching a list of local names.
   * A root more returned means an exact matching of the given local names 
   * with the local names from the context map. Otherwise we can get either 
   * a mode stored as otherValue or null if the given context does not match 
   * any of the stored paths.
   * @param context The list of local names that represent a section context 
   * (path from root local element names from the same namespace).
   * @return A mode or null.
   */  
  Object get(Vector context) {
    return get(context, context.size());
  }
  
  /**
   * Adds a single path (isRoot, names) and a mode to be used for this path = context.
   * @param isRoot True if the path starts with /
   * @param names The local names that form the path.
   * @param value The mode.
   * @return true if there is no duplicate path, false otherwise.
   */
  boolean put(boolean isRoot, Vector names, Object value) {
    return put(isRoot, names, names.size(), value);
  }

  /**
   * Get the mode matching a list of local names.
   * A root more returned means an exact matching of the given local names 
   * with the local names from the context map. Otherwise we can get either 
   * a mode stored as otherValue or null if the given context does not match 
   * any of the stored paths.
   * @param context The list of local names that represent a section context 
   * (path from root local element names from the same namespace).
   * @param len The lenght we should take from the list.
   * @return A mode or null.
   */
  private Object get(Vector context, int len) {
    if (len > 0) {
      ContextMap nestedMap = (ContextMap)nameTable.get(context.elementAt(len - 1));
      if (nestedMap != null) {
        Object value = nestedMap.get(context, len - 1);
        if (value != null)
          return value;
      }
    }
    if (rootValue != null && len == 0)
      return rootValue;
    return otherValue;
  }

  /**
   * Adds a single path (isRoot, names) and a mode to be used for this path = context.
   * @param isRoot True if the path starts with /
   * @param names The local names that form the path.
   * @param len The length if the names vector.
   * @param value The mode.
   * @return true if there is no duplicate path, false otherwise.
   */
  private boolean put(boolean isRoot, Vector names, int len, Object value) {
    if (len == 0) {
      // if we have only /
      if (isRoot) {
        if (rootValue != null)
          return false;
        rootValue = value;
      }
      // We followed all the paths, it is not root, 
      // then we store the mode as the other value.
      else {
        if (otherValue != null)
          return false;
        otherValue = value;
      }
      return true;
    }
    else {
      // get the last local name from the path
      Object name = names.elementAt(len - 1);
      // Get the context map mapped in nameTable to that name.
      ContextMap nestedMap = (ContextMap)nameTable.get(name);
      // Not preset then create it.
      if (nestedMap == null) {
        nestedMap = new ContextMap();
        nameTable.put(name, nestedMap);
      }
      // Add the rest of the path names in the nested context map.
      return nestedMap.put(isRoot, names, len - 1, value);
    }
  }

  /**
   * Chek that this context map is equals with
   * a specified context map.
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof ContextMap))
      return false;
    ContextMap other = (ContextMap)obj;
    if (!Equal.equal(this.rootValue, other.rootValue)
        || !Equal.equal(this.otherValue, other.otherValue))
      return false;
    // We want jing to work with JDK 1.1 so we cannot use Hashtable.equals
    if (this.nameTable.size() != other.nameTable.size())
      return false;
    for (Enumeration e = nameTable.keys(); e.hasMoreElements();) {
      Object key = e.nextElement();
      if (!nameTable.get(key).equals(other.nameTable.get(key)))
        return false;
    }
    return true;
  }

  /**
   * Get a hashcode for this context map.
   */
  public int hashCode() {
    int hc = 0;
    if (rootValue != null)
      hc ^= rootValue.hashCode();
    if (otherValue != null)
      hc ^= otherValue.hashCode();
    for (Enumeration e = nameTable.keys(); e.hasMoreElements();) {
      Object key = e.nextElement();
      hc ^= key.hashCode();
      hc ^= nameTable.get(key).hashCode();
    }
    return hc;
  }

  /**
   * Creates an Enumeration implementation that enumerates all the 
   * modes stored in this context map and in the nested context maps.
   */
  static private class Enumerator implements Enumeration {
    /**
     * Store this context map root value.
     */
    private Object rootValue;
    
    /**
     * Store this context map other value.
     */
    private Object otherValue;
    
    /**
     * Stores the enumeration of modes of the current subMap.
     */
    private Enumeration subMapValues;
    
    /**
     * Stores the ContextMap objects from the nameTable.
     */
    private final Enumeration subMaps;

    private Enumerator(ContextMap map) {
      rootValue = map.rootValue;
      otherValue = map.otherValue;
      subMaps = map.nameTable.elements();
    }

    /**
     * Advance to the next context map values
     * in subMapValues and to the next element 
     * in subMap enumeration, if needed.
     */
    private void prep() {
      while ((subMapValues == null || !subMapValues.hasMoreElements()) && subMaps.hasMoreElements())
        subMapValues = ((ContextMap)subMaps.nextElement()).values();
    }

    /**
     * True if we have more elements.
     */
    public boolean hasMoreElements() {
      prep();
      return rootValue != null || otherValue != null || (subMapValues != null && subMapValues.hasMoreElements());
    }

    /**
     * Get the next element (mode in this case).
     */
    public Object nextElement() {
      if (rootValue != null) {
        Object tem = rootValue;
        rootValue = null;
        return tem;
      }
      if (otherValue != null) {
        Object tem = otherValue;
        otherValue = null;
        return tem;
      }
      prep();
      if (subMapValues == null)
        throw new NoSuchElementException();
      return subMapValues.nextElement();
    }
  }

  /**
   * Get an enumeration with all the modes in this context map.
   * @return An enumeration containing Mode objects.
   */
  Enumeration values() {
    return new Enumerator(this);
  }
}
