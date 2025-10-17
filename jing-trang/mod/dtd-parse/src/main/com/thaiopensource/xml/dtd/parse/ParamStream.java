package com.thaiopensource.xml.dtd.parse;

import java.util.Vector;

class ParamStream {
  int type;
  Entity entity;
  Particle group;
  String value;

  private int i = 0;
  private final Vector v;
  private final boolean showReferences;

  ParamStream(Vector v) {
    this.v = v;
    this.showReferences = false;
  }

  ParamStream(Vector v, boolean showReferences) {
    this.v = v;
    this.showReferences = showReferences;
  }
  
  boolean advance() {
    while (i < v.size()) {
      Param p = (Param)v.elementAt(i++);
      type = p.type;
      entity = p.entity;
      group = p.group;
      value = p.value;
      switch (type) {
      case Param.REFERENCE:
	if (showReferences && entity.semantic > 0) {
	  int level = 0;
	  for (;;) {
	    p = (Param)v.elementAt(i++);
	    if (p.type == Param.REFERENCE)
	      level++;
	    else if (p.type == Param.REFERENCE_END
		     && level-- == 0)
	      break;
	  }
	  return true;
	}
	break;
      case Param.REFERENCE_END:
	break;
      default:
	return true;
      }
    }
    type = -1;
    return false;
  }
}

