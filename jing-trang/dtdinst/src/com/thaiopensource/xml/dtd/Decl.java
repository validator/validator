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
    case PROCESSING_INSTRUCTION:
      return createProcessingInstruction();
    case NOTATION:
      return createNotationDecl();
    case ELEMENT:
      return createElementDecl();
    case ATTLIST:
      return createAttlistDecl();
    case ENTITY:
      return createEntityDecl(db);
    case INCLUDE_SECTION:
      return createIncludedSection(db);
    case IGNORE_SECTION:
      return createIgnoredSection();
    }
    return null;
  }

  ElementDecl createElementDecl() {
    ParamStream ps = new ParamStream(params, true);
    NameSpec nameSpec = Param.paramsToNameSpec(ps);
    return new ElementDecl(nameSpec, Param.paramsToModelGroup(ps));
  }

  AttlistDecl createAttlistDecl() {
    ParamStream ps = new ParamStream(params, true);
    NameSpec nameSpec = Param.paramsToNameSpec(ps);
    return new AttlistDecl(nameSpec, Param.paramsToAttributeGroup(ps));
  }
    
  TopLevel createEntityDecl(DtdBuilder db) {
    ParamStream ps = new ParamStream(params);
    ps.advance();
    if (ps.type != Param.PERCENT)
      return createGeneralEntityDecl(db, ps.value);
    ps.advance();
    String name = ps.value;
    Entity entity = db.lookupParamEntity(name);
    if (entity.decl == null)
      entity.decl = this;
    if (entity.decl != this)
      return createOverriddenDef(name, ps);
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
    case Entity.SEMANTIC_NAME_SPEC:
      entity.nameSpec = Param.paramsToNameSpec(entity.parsed);
      return new NameSpecDef(name, entity.nameSpec);
    }
    return null;
  }

  TopLevel createGeneralEntityDecl(DtdBuilder db, String name) {
    Entity entity = db.lookupGeneralEntity(name);
    if (entity.decl == null)
      entity.decl = this;
    if (entity.decl != this)
      return null;
    if (entity.text == null)
      return null;
    return new InternalEntityDecl(name, new String(entity.text));
  }

  IncludedSection createIncludedSection(DtdBuilder db) {
    Flag flag = Param.paramsToFlag(params);
    Vector contents = declsToTopLevel(db, decls);
    TopLevel[] tem = new TopLevel[contents.size()];
    for (int i = 0; i < tem.length; i++)
      tem[i] = (TopLevel)contents.elementAt(i);
    return new IncludedSection(flag, tem);
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

  IgnoredSection createIgnoredSection() {
    return new IgnoredSection(Param.paramsToFlag(params), value);
  }

  ProcessingInstruction createProcessingInstruction() {
    int len = value.length();
    int i;
    for (i = 0; i < len && !isWS(value.charAt(i)); i++)
      ;
    String target = value.substring(0, i);
    if (i < len) {
      for (++i; i < len && isWS(value.charAt(i)); i++)
	;
    }
    return new ProcessingInstruction(target, value.substring(i, len));
  }

  static private boolean isWS(char c) {
    switch (c) {
    case '\n':
    case '\r':
    case '\t':
    case ' ':
      return true;
    }
    return false;
  }
  
  NotationDecl createNotationDecl() {
    return null;
  }

  OverriddenDef createOverriddenDef(String name, ParamStream ps) {
    ps.advance();
    if (ps.type != Param.LITERAL)
      return null;
    return new OverriddenDef(name, ps.value);
  }
}
