package com.thaiopensource.xml.dtd;

import java.util.Vector;

class Particle {
  static final int REFERENCE = 0; // entity
  static final int GROUP = 1;	// particles + occur
  static final int ELEMENT_NAME = 2; // value + occur
  static final int NMTOKEN = 3; // value
  static final int PCDATA = 4;
  static final int REFERENCE_END = 5;
  static final int CONNECT_OR = 6;
  static final int CONNECT_SEQ = 7;
  // Pseudo-tokens
  static final int EMPTY_MODEL_GROUP = 8;
  static final int EMPTY_ATTRIBUTE_VALUE_GROUP = 9;

  Particle(int type) {
    this.type = type;
  }

  int type;
  char occur;			// * ? + or 0
  Vector particles; 
  Entity entity;
  String value;

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Particle))
      return false;
    Particle other = (Particle)obj;
    if (this.type != other.type)
      return false;
    if (this.occur != other.occur)
      return false;
    if (this.entity != other.entity)
      return false;
    if (this.value != null && !this.value.equals(other.value))
      return false;
    if (this.particles != null) {
      int n = this.particles.size();
      if (other.particles.size() != n)
	return false;
      for (int i = 0; i < n; i++)
	if (!this.particles.elementAt(i).equals(other.particles.elementAt(i)))
	  return false;
    }
    return true;
  }

}
