package com.thaiopensource.util;

public interface PropertyMap {
  Object get(PropertyId pid);
  boolean contains(PropertyId pid);
  int size();
  PropertyId getKey(int i);
}
