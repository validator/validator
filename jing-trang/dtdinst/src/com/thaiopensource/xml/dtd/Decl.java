package com.thaiopensource.xml.dtd;

import java.util.Vector;

class Decl {
  static final int REFERENCE = 0; // entity
  static final int REFERENCE_END = 1;
  static final int ELEMENT = 2; // params
  static final int ATTLIST = 3; // params
  static final int ENTITY = 4;  // params
  static final int NOTATION = 5; // params
  static final int INCLUDE_SECTION = 6; // params + decls
  static final int IGNORE_SECTION = 7; // params + value
  static final int COMMENT = 8; // value
  static final int PROCESSING_INSTRUCTION = 9; // value
  
  Decl(int type) {
    this.type = type;
  }

  int type;
  Vector params;
  String value;
  Entity entity;
  Vector decls;

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

  TopLevel createTopLevel(DtdBuilder db) {
    switch (type) {
    case COMMENT:
      return new Comment(value);
    case ELEMENT:
      return createElementDecl();
    case ATTLIST:
      return createAttlistDecl();
    case ENTITY:
      return createEntityDecl(db);
    case INCLUDE_SECTION:
      return createIncludeSection(db);
    }
    return null;
  }

  ElementDecl createElementDecl() {
    ParamStream ps = new ParamStream(params, true);
    ps.advance();
    String name = ps.value;
    return new ElementDecl(name, Param.paramsToModelGroup(ps));
  }

  AttlistDecl createAttlistDecl() {
    ParamStream ps = new ParamStream(params, true);
    ps.advance();
    String name = ps.value;
    return new AttlistDecl(name, Param.paramsToAttributeGroup(ps));
  }
    
  TopLevel createEntityDecl(DtdBuilder db) {
    ParamStream ps = new ParamStream(params);
    ps.advance();
    if (ps.type != Param.PERCENT)
      return null;
    ps.advance();
    String name = ps.value;
    Entity entity = db.lookupParamEntity(name);
    if (entity.decl == null)
      entity.decl = this;
    if (entity.decl != this)
      return null;
    switch (entity.semantic) {
    case Entity.SEMANTIC_MODEL_GROUP:
      entity.modelGroup = entity.toModelGroup();
      return new ModelGroupDef(name, entity.modelGroup);
    case Entity.SEMANTIC_ATTRIBUTE_GROUP:
      entity.attributeGroup = 
	Param.paramsToAttributeGroup(entity.parsed);
      return new AttributeGroupDef(name, entity.attributeGroup);
    case Entity.SEMANTIC_DATATYPE:
      entity.datatype = Param.paramsToDatatype(entity.parsed);
      return new DatatypeDef(name, entity.datatype);
    case Entity.SEMANTIC_ENUM_GROUP:
      entity.enumGroup = Particle.particlesToEnumGroup(entity.parsed);
      return new EnumGroupDef(name, entity.enumGroup);
    case Entity.SEMANTIC_FLAG:
      entity.flag = Param.paramsToFlag(entity.parsed);
      return new FlagDef(name, entity.flag);
    }
    return null;
  }

  IncludeSection createIncludeSection(DtdBuilder db) {
    Flag flag = Param.paramsToFlag(params);
    Vector contents = declsToTopLevel(db, decls);
    TopLevel[] tem = new TopLevel[decls.size()];
    for (int i = 0; i < tem.length; i++)
      tem[i] = (TopLevel)contents.elementAt(i);
    return new IncludeSection(flag, tem);
  }

  static Vector declsToTopLevel(DtdBuilder db, Vector decls) {
    Vector v = new Vector();
    int n = decls.size();
    for (int i = 0; i < n; i++) {
      TopLevel t = ((Decl)decls.elementAt(i)).createTopLevel(db);
      if (t != null)
	v.addElement(t);
    }
    return v;
  }
}
