package com.thaiopensource.xml.dtd;

import java.util.Vector;

class Decl {
  static final int REFERENCE = 0; // entity
  static final int REFERENCE_END = 1;
  static final int ELEMENT = 2; // params
  static final int ATTLIST = 3; // params
  static final int ENTITY = 4;  // params
  static final int NOTATION = 5; // params
  static final int START_INCLUDE_SECTION = 6; // params
  static final int END_INCLUDE_SECTION = 7;
  static final int IGNORE_SECTION = 8; // params + value
  static final int COMMENT = 9; // value
  static final int PROCESSING_INSTRUCTION = 10; // value
  
  Decl(int type) {
    this.type = type;
  }

  int type;
  Vector params;
  String value;
  Entity entity;

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Decl))
      return false;
    Decl other = (Decl)obj;
    if (this.type != other.type)
      return false;
    if (this.entity != other.entity)
      return false;
    if (this.value != null && !this.value.equals(other.value))
      return false;
    if (this.params != null) {
      int n = this.params.size();
      if (other.params.size() != n)
	return false;
      for (int i = 0; i < n; i++)
	if (!this.params.elementAt(i).equals(other.params.elementAt(i)))
	  return false;
    }
    return true;
  }

}
