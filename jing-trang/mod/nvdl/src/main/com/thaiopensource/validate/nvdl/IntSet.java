package com.thaiopensource.validate.nvdl;

/**
 * Utility class. Stores a set of integers.
 * The set is stored in an array and sorted.
 */
class IntSet {
  /**
   * Initial size.
   */
  static private final int INIT_SIZE = 4;
  /**
   * An int array with the values.
   */
  private int[] v = null;
  
  /**
   * The number of stored values.
   */
  private int len = 0;

  /**
   * Add a new value.
   * @param n The value to be added.
   */
  void add(int n) {
    if (v == null) {
      v = new int[INIT_SIZE];
      v[0] = n;
      len = 1;
      return;
    }
    if (len == v.length) {
      int[] newv = new int[len*2];
      System.arraycopy(v, 0, newv, 0, len);
      v = newv;
    }
    if (n > v[len - 1]) {
      v[len++] = n;
      return;
    }
    int i = 0;
    for (; i < len; i++) {
      if (n <= v[i]) {
        if (n == v[i])
          return;
        break;
      }
    }
    for (int j = len; j >= i; j--)
      v[j + 1] = v[j];
    v[i] = n;
    ++len;
  }

  /**
   * Adds all the values from another set - union.
   * @param is The other integer set.
   */
  void addAll(IntSet is) {
    if (is.len == 0)
      return;
    int[] newv = new int[len + is.len];
    int i = 0, j = 0, k = 0;
    while (i < len && j < is.len) {
      if (v[i] < is.v[j])
        newv[k++] = v[i++];
      else if (is.v[j] < v[i])
        newv[k++] = is.v[j++];
      else {
        newv[k++] = v[i++];
        j++;
      }
    }
    while (i < len)
      newv[k++] = v[i++];
    while (j < is.len)
      newv[k++] = is.v[j++];
    v = newv;
    len = k;
  }

  /**
   * Get the number of values in this set.
   * @return
   */
  int size() {
    return len;
  }

  /**
   * Get the ith value from the set.
   * @param i The index in the set, zero based.
   * @return The value at position i.
   */
  int get(int i) {
   if (i >= len)
     throw new IndexOutOfBoundsException();
    try {
      return v[i];
    }
    catch (NullPointerException e) {
      throw new IndexOutOfBoundsException();
    }
  }
}
