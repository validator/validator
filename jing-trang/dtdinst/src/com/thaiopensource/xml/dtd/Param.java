package com.thaiopensource.xml.dtd;

import java.util.Vector;

class Param {
  static final int REFERENCE = 0;
  static final int REFERENCE_END = 1;
  static final int LITERAL = 2;
  static final int MODEL_GROUP = 3;
  static final int PERCENT = 4;
  static final int IMPLIED = 5; // #IMPLIED
  static final int REQUIRED = 6; // #REQUIRED
  static final int FIXED = 7; // #REQUIRED
  static final int EMPTY = 8;
  static final int ANY = 9;
  static final int ELEMENT_NAME = 10; // name after <!ELEMENT or <!ATTLIST
  static final int ATTRIBUTE_NAME = 11;
  static final int ATTRIBUTE_TYPE = 12;
  static final int ATTRIBUTE_TYPE_NOTATION = 13;
  static final int DEFAULT_ATTRIBUTE_VALUE = 14;
  static final int ATTRIBUTE_VALUE_GROUP = 15; // a group in an ATTLIST

  static final int OTHER = 30;

  Param(int type) {
    this.type = type;
  }

  int type;
  Entity entity;
  Particle group;
  String value;

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Param))
      return false;
    Param other = (Param)obj;
    if (this.type != other.type)
      return false;
    if (this.entity != other.entity)
      return false;
    if (this.value != null && !this.value.equals(other.value))
      return false;
    if (this.group != null && !this.group.equals(other.group))
      return false;
    return true;
  }
}
