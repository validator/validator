package com.thaiopensource.validate.nvdl;

import org.xml.sax.Attributes;

/**
 * Implementation of the Attributes interface that filters out some of the
 * attributes of an actual Attributes implementation. We will keep only the
 * attributes whose indexes are specified in a given set of indexes.
 */
class FilteredAttributes implements Attributes {
  /**
   * The actual attributes, we will filter out some of them.
   */
  private final Attributes attributes;
  
  /**
   * The set of indexes of the attributes to used.
   */
  private final IntSet indexSet;
  
  /**
   * Maps indexes in the real attributes list to 1 based indexes in the
   * filtered attributes list. For instance if we keep only the 
   * 1st and the 3rd attributes from 4 attributes then the 
   * reverse index map will have as values
   * [0] -->  1
   * [1] -->  0
   * [2] -->  2
   * [3] -->  0
   * 
   */
  private int[] reverseIndexMap;

  /**
   * Creates a filtered attributes instance.
   * @param indexSet The set with indexes that we will keep.
   * @param attributes The actual attributes.
   */
  public FilteredAttributes(IntSet indexSet, Attributes attributes) {
    this.indexSet = indexSet;
    this.attributes = attributes;
  }

  /**
   * Gets the index in the filtered set for a given real index.
   * If the reverseIndexMap is not computed it computes it, 
   * otherwise it just uses the previously computed map. 
   * @param k The index in the real attributes.
   * @return The index in the filtered attributes.
   */
  private int reverseIndex(int k) {
    if (reverseIndexMap == null) {
      reverseIndexMap = new int[attributes.getLength()];
      for (int i = 0, len = indexSet.size(); i < len; i++)
        reverseIndexMap[indexSet.get(i)] = i + 1;
    }
    return reverseIndexMap[k] - 1;
  }

  /**
   * The number of attributes, the same as the length of the list of indexes.
   */
  public int getLength() {
    return indexSet.size();
  }

  /**
   * Get the URI for the index-th attribute.
   */
  public String getURI(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getURI(indexSet.get(index));
  }

  /**
   * Get the local name for the index-th attribute.
   */
  public String getLocalName(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getLocalName(indexSet.get(index));
  }

  /**
   * Get the QName for the index-th attribute.
   */
  public String getQName(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getQName(indexSet.get(index));
  }

  /**
   * Get the type for the index-th attribute.
   */
  public String getType(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getType(indexSet.get(index));
  }

  /**
   * Get the value for the index-th attribute.
   */
  public String getValue(int index) {
    if (index < 0 || index >= indexSet.size())
      return null;
    return attributes.getValue(indexSet.get(index));
  }

  public int getIndex(String uri, String localName) {
    int n = attributes.getIndex(uri, localName);
    if (n < 0)
      return n;
    return reverseIndex(n);
  }

  public int getIndex(String qName) {
    int n = attributes.getIndex(qName);
    if (n < 0)
      return n;
    return reverseIndex(n);
  }

  /**
   * Get the real index, in the initial attributes list of a given attribute.
   * If the attribute is filtered out then return -1.
   * @param uri The attribute uri.
   * @param localName The attribute local name.
   * @return The real index if the attribute is present and not filtered out, otherwise -1.
   */
  private int getRealIndex(String uri, String localName) {
    int index = attributes.getIndex(uri, localName);
    if (index < 0 || reverseIndex(index) < 0)
      return -1;
    return index;
  }

  /**
   * Get the real index, in the initial attributes list of a given attribute.
   * If the attribute is filtered out then return -1.
   * @param qName The attribute qualified name.
   * @return The real index if the attribute is present and not filtered out, otherwise -1.
   */
  private int getRealIndex(String qName) {
    int index = attributes.getIndex(qName);
    if (index < 0 || reverseIndex(index) < 0)
      return -1;
    return index;
  }

  /**
   * Get the type of the attribute.
   * @param uri The attribute uri.
   * @param localName The attribute local name.
   */
  public String getType(String uri, String localName) {
    return attributes.getType(getRealIndex(uri, localName));
  }

  /**
   * Get the value of the attribute.
   * @param uri The attribute uri.
   * @param localName The attribute local name.
   */
  public String getValue(String uri, String localName) {
    return attributes.getValue(getRealIndex(uri, localName));
  }

  /**
   * Get the type of the attribute.
   * @param qName The attribute qualified name.
   */
  public String getType(String qName) {
    return attributes.getType(getRealIndex(qName));
  }

  /**
   * Get the value of the attribute.
   * @param qName The attribute qualified name.
   */
  public String getValue(String qName) {
    return attributes.getValue(getRealIndex(qName));
  }

}
