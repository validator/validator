package com.thaiopensource.xml.dtd;

import java.util.Vector;

class Entity {
  static class Reference {
    Reference(Entity entity, int start, int end) {
      this.entity = entity;
      this.start = start;
      this.end = end;
    }
    Entity entity;
    int start;
    int end;
  }

  final String name;
  final boolean isParameter;
  Entity(String name, boolean isParameter) {
    this.name = name;
    this.isParameter = isParameter;
  }
  char[] text;
  String systemId;
  String publicId;
  String baseUri;
  // Which parts of text came from references?
  Reference[] references;
  boolean open;
  String notationName;
  Vector atoms;
  boolean mustReparse;

  static final int INCONSISTENT_LEVEL = -1;
  static final int NO_LEVEL = 0;
  static final int DECL_LEVEL = 1;
  static final int PARAM_LEVEL = 2;
  static final int PARTICLE_LEVEL = 3;

  int referenceLevel = NO_LEVEL;

  static final int GROUP_CONTAINS_OR = 01;
  static final int GROUP_CONTAINS_SEQ = 02;
  static final int GROUP_CONTAINS_PCDATA = 04;
  static final int GROUP_CONTAINS_GROUP = 010;
  static final int GROUP_CONTAINS_ELEMENT_NAME = 020;
  static final int GROUP_CONTAINS_NMTOKEN = 040;

  int groupFlags = 0;

  static final int SEMANTIC_NONE = 0;
  static final int SEMANTIC_MODEL_GROUP = 1;
  static final int SEMANTIC_ATTRIBUTE_GROUP = 2;
  static final int SEMANTIC_ENUM_GROUP = 3;
  static final int SEMANTIC_DATATYPE = 4;
  static final int SEMANTIC_FLAG = 5;

  int semantic = SEMANTIC_NONE;
  ModelGroup modelGroup;
  AttributeGroup attributeGroup;
  EnumGroup enumGroup;
  Datatype datatype;
  Flag flag;

  Decl decl;

  Vector parsed;

  void setParsed(int level, Vector v, int start, int end) {
    if (level == referenceLevel) {
      if (!sliceEqual(parsed, v, start, end)) {
	// XXX give a warning
	parsed = null;
	referenceLevel = INCONSISTENT_LEVEL;
	System.err.println("Warning: entity used inconsistently: " + name);
      }
    }
    else if (referenceLevel == NO_LEVEL) {
      parsed = new Vector();
      appendSlice(parsed, v, start, end);
      referenceLevel = level;
    }
    else if (referenceLevel > 0) {
      System.err.println("Warning: entity used inconsistently: " + name);
      parsed = null;
      referenceLevel = INCONSISTENT_LEVEL;
    }
  }

  int textIndexToAtomIndex(int ti) {
    int nAtoms = atoms.size();
    int len = 0;
    int atomIndex = 0;      
    for (;;) {
      if (len == ti)
	return atomIndex;
      if (atomIndex >= nAtoms)
	break;
      Atom a = (Atom)atoms.elementAt(atomIndex);
      len += a.getToken().length();
      if (len > ti)
	break;
      atomIndex++;
    }
    return -1;
  }

  void unexpandEntities() {
    if (references == null || atoms == null)
      return;
    Vector newAtoms = null;
    int nCopiedAtoms = 0;
    for (int i = 0; i < references.length; i++) {
      int start = textIndexToAtomIndex(references[i].start);
      int end = textIndexToAtomIndex(references[i].end);
      if (start >= 0 && end >= 0) {
	if (newAtoms == null)
	  newAtoms = new Vector();
	appendSlice(newAtoms, atoms, nCopiedAtoms, start);
	newAtoms.addElement(new Atom(references[i].entity));
	if (references[i].entity.atoms == null) {
	  Vector tem = new Vector();
	  references[i].entity.atoms = tem;
	  appendSlice(tem, atoms, start, end);
	  references[i].entity.unexpandEntities();
	}
	nCopiedAtoms = end;
      }
      else {
	System.err.println("Warning: could not preserve reference to entity \""
			   + references[i].entity.name
			   + "\" in entity \""
			   + this.name
			   + "\"");
      }
    }
    if (newAtoms == null)
      return;
    appendSlice(newAtoms, atoms, nCopiedAtoms, atoms.size());
    atoms = newAtoms;
    references = null;
  }

  static boolean sliceEqual(Vector v1, Vector v2, int start, int end) {
    int n = v1.size();
    if (end - start != n)
      return false;
    for (int i = 0; i < n; i++)
      if (!v1.elementAt(i).equals(v2.elementAt(start + i)))
	return false;
    return true;
  }

  static void appendSlice(Vector to, Vector from, int start, int end) {
    for (; start < end; start++)
      to.addElement(from.elementAt(start));
  }

  void analyzeSemantic() {
    switch (referenceLevel) {
    case PARAM_LEVEL:
      analyzeSemanticParam();
      break;
    case PARTICLE_LEVEL:
      analyzeSemanticParticle();
      break;
    }
  }

  private void analyzeSemanticParam() {
    if (isAttributeGroup())
      semantic = SEMANTIC_ATTRIBUTE_GROUP;
    else if (isDatatype())
      semantic = SEMANTIC_DATATYPE;
    else if (isFlag())
      semantic = SEMANTIC_FLAG;
    else if (isModelGroup())
      semantic = SEMANTIC_MODEL_GROUP;
    else
      System.err.println("Warning: could not understand entity: " + name);
  }

  private boolean isAttributeGroup() {
    ParamStream ps = new ParamStream(parsed);
    if (!ps.advance())
      return false;
    do {
      if (ps.type != Param.EMPTY_ATTRIBUTE_GROUP
	  && (ps.type != Param.ATTRIBUTE_NAME
	      || !ps.advance()
	      || (ps.type == Param.ATTRIBUTE_TYPE_NOTATION && !ps.advance())
	      || !ps.advance()
	      || (ps.type == Param.FIXED && !ps.advance())))
	return false;
    } while (ps.advance());
    return true;
  }

  private boolean isDatatype() {
    ParamStream ps = new ParamStream(parsed);
    return (ps.advance()
	    && (ps.type == Param.ATTRIBUTE_TYPE
		|| ps.type == Param.ATTRIBUTE_VALUE_GROUP
		|| (ps.type == Param.ATTRIBUTE_TYPE_NOTATION
		    && ps.advance()))
	    && !ps.advance());
  }

  private boolean isFlag() {
    ParamStream ps = new ParamStream(parsed);
    return (ps.advance()
	    && (ps.type == Param.INCLUDE
		|| ps.type == Param.IGNORE)
	    && !ps.advance());
  }

  private boolean isModelGroup() {
    ParamStream ps = new ParamStream(parsed);
    return (ps.advance()
	    && (ps.type == Param.MODEL_GROUP
		|| ps.type == Param.EMPTY
		|| ps.type == Param.EMPTY)
	    && !ps.advance());
  }

  private void analyzeSemanticParticle() {
    int n = parsed.size();
    if (n == 0) {
      analyzeEmptySemanticParticle();
      return;
    }
    for (int i = 0; i < n; i++) {
      switch (((Particle)parsed.elementAt(i)).type) {
      case Particle.GROUP:
      case Particle.ELEMENT_NAME:
      case Particle.PCDATA:
	semantic = SEMANTIC_MODEL_GROUP;
	return;
      case Particle.NMTOKEN:
	semantic = SEMANTIC_ENUM_GROUP;
	return;
      }
    }
    System.err.println("Warning: could not understand entity: " + name);
  }

  static final int GROUP_MODEL_GROUP_FLAGS 
    = GROUP_CONTAINS_PCDATA|GROUP_CONTAINS_GROUP|GROUP_CONTAINS_ELEMENT_NAME;

  private void analyzeEmptySemanticParticle() {
    if ((groupFlags & GROUP_MODEL_GROUP_FLAGS) == 0) {
      semantic = SEMANTIC_ENUM_GROUP;
      return;
    }
    if ((groupFlags & GROUP_CONTAINS_NMTOKEN) == 0) {
      switch (groupFlags & (GROUP_CONTAINS_SEQ|GROUP_CONTAINS_OR)) {
      case GROUP_CONTAINS_SEQ:
      case GROUP_CONTAINS_OR:
	semantic = SEMANTIC_MODEL_GROUP;
	return;
      }
    }
    System.err.println("Warning: could not understand entity: " + name);
  }

  ModelGroup toModelGroup() {
    if (referenceLevel == PARAM_LEVEL)
      return Param.paramsToModelGroup(parsed);
    if (parsed.size() == 0) {
      if ((groupFlags & GROUP_CONTAINS_SEQ) != 0)
	return new Sequence(new ModelGroup[0]);
      else
	return new Choice(new ModelGroup[0]);
    }
    return Particle.particlesToModelGroup(parsed);
  }
}

