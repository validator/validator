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
  Entity(String name) { this.name = name; }
  char[] text;
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
  static final int PARTICLE_LEVEL = 2;

  int referenceLevel = NO_LEVEL;

  Vector parsed;

  void setParsed(int level, Vector v, int start, int end) {
    if (level == referenceLevel) {
      if (!sliceEqual(parsed, v, start, end)) {
	parsed = null;
	referenceLevel = INCONSISTENT_LEVEL;
      }
    }
    else if (referenceLevel == NO_LEVEL) {
      parsed = new Vector();
      appendSlice(parsed, v, start, end);
      referenceLevel = level;
    }
    else if (referenceLevel > 0) {
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
}

