package com.thaiopensource.xml.dtd;

import java.util.Vector;
import java.util.Enumeration;

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
      return createOverriddenDef(entity, ps);
    if (entity.text == null)
      return new ExternalIdDef(name, entity.getExternalId());
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
    if (entity.problem == Entity.NO_PROBLEM)
      throw new RuntimeException("unexplained problem for entity " + entity.name);
    return new ParamDef(name, entity.entityValue);
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
    Vector contents = declsToTopLevel(db, decls.elements());
    TopLevel[] tem = new TopLevel[contents.size()];
    for (int i = 0; i < tem.length; i++)
      tem[i] = (TopLevel)contents.elementAt(i);
    return new IncludedSection(flag, tem);
  }

  static Vector declsToTopLevel(DtdBuilder db, Enumeration decls) {
    Vector v = new Vector();
    int level = 0;
    while (decls.hasMoreElements()) {
      TopLevel t = null;
      Decl decl = (Decl)decls.nextElement();
      switch (decl.type) {
      case COMMENT:
	t = new Comment(decl.value);
	break;
      case PROCESSING_INSTRUCTION:
	t = decl.createProcessingInstruction();
	break;
      case NOTATION:
	t = decl.createNotationDecl();
	break;
      case ELEMENT:
	t = decl.createElementDecl();
	break;
      case ATTLIST:
	t = decl.createAttlistDecl();
	break;
      case ENTITY:
	t = decl.createEntityDecl(db);
	break;
      case INCLUDE_SECTION:
	t = decl.createIncludedSection(db);
	break;
      case IGNORE_SECTION:
	t = decl.createIgnoredSection();
	break;
      case REFERENCE:
	if (decl.entity.text == null)
	  t = decl.createExternalIdRef(db, decls);
	else
	  level++;
	break;
      case REFERENCE_END:
	if (level == 0)
	  return v;
	--level;
	break;
      }
      if (t != null)
	v.addElement(t);
    }
    return v;
  }

  ExternalIdRef createExternalIdRef(DtdBuilder db, Enumeration decls) {
    Vector v = declsToTopLevel(db, decls);
    TopLevel[] tem = new TopLevel[v.size()];
    for (int i = 0; i < tem.length; i++)
      tem[i] = (TopLevel)v.elementAt(i);
    return new ExternalIdRef(entity.name,
			     entity.getExternalId(),
			     tem);
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

  OverriddenDef createOverriddenDef(Entity entity, ParamStream ps) {
    ps.advance();
    if (ps.type != Param.LITERAL)
      return null;
    return new OverriddenDef(entity.name,
			     ps.value,
			     ps.value.equals(entity.entityValue));
  }
}
