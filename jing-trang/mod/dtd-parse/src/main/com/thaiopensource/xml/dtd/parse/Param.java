package com.thaiopensource.xml.dtd.parse;

import java.util.Vector;
import java.util.Enumeration;

import com.thaiopensource.xml.dtd.om.*;

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
  static final int IGNORE = 18;
  static final int INCLUDE = 19;

  static final int OTHER = 30;

  Param(int type) {
    this.type = type;
  }

  final int type;
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
      case EMPTY_ATTRIBUTE_GROUP:
	break;
      case REFERENCE:
	if (ps.entity.semantic == Entity.SEMANTIC_ATTRIBUTE_GROUP) {
	  agm = new AttributeGroupRef(ps.entity.name, ps.entity.attributeGroup);
	  break;
	}
	// fall through
      case ATTRIBUTE_NAME:
	{
	  NameSpec nameSpec = currentParamToNameSpec(ps);
	  Datatype datatype = paramsToDatatype(ps);
	  AttributeDefault ad = paramsToAttributeDefault(ps);
	  agm = new Attribute(nameSpec, datatype, ad);
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
      return new NotationDatatype(paramToEnumGroup(ps));
    case ATTRIBUTE_TYPE:
      if (ps.value.equals("CDATA"))
	return new CdataDatatype();
      else
	return new TokenizedDatatype(ps.value);
    }
    throw new Error();
  }

  static EnumGroup paramToEnumGroup(ParamStream ps) {
    if (ps.type == REFERENCE)
      return new EnumGroup(new EnumGroupMember[]{new EnumGroupRef(ps.entity.name, ps.entity.enumGroup)});
    else
      return Particle.particlesToEnumGroup(ps.group.particles);
  }

  static AttributeDefault paramsToAttributeDefault(Vector v) {
    return paramsToAttributeDefault(new ParamStream(v, true));
  }

  static AttributeDefault paramsToAttributeDefault(ParamStream ps) {
    ps.advance();
    switch (ps.type) {
    case REFERENCE:
      return new AttributeDefaultRef(ps.entity.name,
				     ps.entity.attributeDefault);
    case REQUIRED:
      return new RequiredValue();
    case FIXED:
      ps.advance();
      return new FixedValue(ps.value);
    case DEFAULT_ATTRIBUTE_VALUE:
      return new DefaultValue(ps.value);
    case IMPLIED:
      return new ImpliedValue();
    }
    throw new Error();
  }

  static ModelGroup paramsToModelGroup(Vector v) {
    return paramsToModelGroup(new ParamStream(v, true));
  }

  static ModelGroup paramsToModelGroup(ParamStream ps) {
    ps.advance();
    switch (ps.type) {
    case Param.REFERENCE:
      return new ModelGroupRef(ps.entity.name, ps.entity.modelGroup);
    case Param.ANY:
      return new Any();
    case Param.EMPTY:
      return new Sequence(new ModelGroup[0]);
    case Param.MODEL_GROUP:
      return ps.group.createModelGroup();
    }
    throw new Error();
  }

  static Flag paramsToFlag(Vector v) {
    return paramsToFlag(new ParamStream(v, true));
  }

  static Flag paramsToFlag(ParamStream ps) {
    ps.advance();
    switch (ps.type) {
    case Param.REFERENCE:
      return new FlagRef(ps.entity.name, ps.entity.flag);
    case Param.IGNORE:
      return new Ignore();
    case Param.INCLUDE:
      return new Include();
    }
    throw new Error();
  }

  static NameSpec paramsToNameSpec(Vector v) {
    return paramsToNameSpec(new ParamStream(v, true));
  }

  static NameSpec paramsToNameSpec(ParamStream ps) {
    ps.advance();
    return currentParamToNameSpec(ps);
  }

  static private NameSpec currentParamToNameSpec(ParamStream ps) {
    switch (ps.type) {
    case Param.REFERENCE:
      return new NameSpecRef(ps.entity.name, ps.entity.nameSpec);
    case Param.ELEMENT_NAME:
    case Param.ATTRIBUTE_NAME:
      return new Name(ps.value);
    }
    throw new Error();
  }

  static void examineElementNames(DtdBuilder db, Enumeration params) {
    while (params.hasMoreElements()) {
      Param param = (Param)params.nextElement();
      switch (param.type) {
      case ELEMENT_NAME:
	db.noteElementName(param.value, null);
	break;
      case MODEL_GROUP:
	Particle.examineElementNames(db, param.group.particles.elements());
	break;
      }
    }
  }
	

}
