package com.thaiopensource.xml.dtd;

import java.util.Vector;

class Particle {
  static final int REFERENCE = 0; // entity
  static final int GROUP = 1;	// particles + occur + sep
  static final int ELEMENT_NAME = 2; // value + occur
  static final int NMTOKEN = 3; // value
  static final int PCDATA = 4;
  static final int REFERENCE_END = 5;

  Particle(int type) {
    this.type = type;
  }

  int type;
  char occur;			// * ? + or 0
  char sep;			// , or | or 0
  Vector particles;
  Entity entity;
  String value;
}
