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
  // Pseudo-param representing zero or more attributes in an ATTLIST
  static final int EMPTY_ATTRIBUTE_GROUP = 16;
  static final int NOTATION_GROUP = 17;

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

  static AttributeGroup paramsToAttributeGroup(Vector v) {
    return paramsToAttributeGroup(new ParamStream(v, true));
  }

  static AttributeGroup paramsToAttributeGroup(ParamStream ps) {
    Vector ag = new Vector();
    while (ps.advance()) {
      AttributeGroupMember agm = null;
      switch (ps.type) {
      case REFERENCE:
	agm = new AttributeGroupRef(ps.entity.name, ps.entity.attributeGroup);
	break;
      case EMPTY_ATTRIBUTE_GROUP:
	break;
      case ATTRIBUTE_NAME:
	{
	  String name = ps.value;
	  Datatype datatype = paramsToDatatype(ps);
	  ps.advance();
	  boolean optional = true;
	  String defaultValue = null;
	  switch (ps.type) {
	  case REQUIRED:
	    optional = false;
	    break;
	  case FIXED:
	    optional = false;
	    ps.advance();
	    // fall through
	  case DEFAULT_ATTRIBUTE_VALUE:
	    defaultValue = ps.value;
	    break;
	  }
	  agm = new Attribute(name, optional, datatype, defaultValue);
	}
      }
      if (agm != null)
	ag.addElement(agm);
    }
    AttributeGroupMember[] members = new AttributeGroupMember[ag.size()];
    for (int i = 0; i < members.length; i++)
      members[i] = (AttributeGroupMember)ag.elementAt(i);
    return new AttributeGroup(members);
  }

  static Datatype paramsToDatatype(Vector v) {
    return paramsToDatatype(new ParamStream(v, true));
  }

  static Datatype paramsToDatatype(ParamStream ps) {
    ps.advance();
    switch (ps.type) {
    case REFERENCE:
      return new DatatypeRef(ps.entity.name, ps.entity.datatype);
    case ATTRIBUTE_VALUE_GROUP:
      return new EnumDatatype(Particle.particlesToEnumGroup(ps.group.particles));
    case ATTRIBUTE_TYPE_NOTATION:
      ps.advance();
      return new NotationDatatype(Particle.particlesToEnumGroup(ps.group.particles));
    case ATTRIBUTE_TYPE:
      return new BasicDatatype(ps.value);
    }
    throw new Error();
  }


}
