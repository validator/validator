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

  static void appendSlice(Vector to, Vector from, int start, int end) {
    for (; start < end; start++)
      to.addElement(from.elementAt(start));
  }
}

