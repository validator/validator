package com.thaiopensource.xml.dtd;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

class DtdBuilder {
  private Vector atoms;
  private Vector decls = new Vector();

  private Hashtable paramEntityTable = new Hashtable();

  DtdBuilder(Vector atoms) {
    this.atoms = atoms;
  }

  Vector getDecls() {
    return decls;
  }

  Entity lookupParamEntity(String name) {
    return (Entity)paramEntityTable.get(name);
  }

  Entity createParamEntity(String name) {
    Entity e = (Entity)paramEntityTable.get(name);
    if (e != null)
      return null;
    e = new Entity(name);
    paramEntityTable.put(name, e);
    return e;
  }

  void unexpandEntities() {
    for (Enumeration e = paramEntityTable.elements();
	 e.hasMoreElements();)
      ((Entity)e.nextElement()).unexpandEntities();
  }
  
  void createDecls() {
    new AtomParser(new AtomStream(atoms),
		   new PrologParser(PrologParser.EXTERNAL_ENTITY),
		   decls).parse();
  }

  void analyzeSemantics() {
    for (Enumeration e = paramEntityTable.elements();
	 e.hasMoreElements();)
      ((Entity)e.nextElement()).analyzeSemantic();
  }

  Vector createTopLevel() {
    return null;
  }

  void dump() {
    dumpEntity("#doc", atoms);
  }

  private static void dumpEntity(String name, Vector atoms) {
    System.out.println("<e name=\"" + name + "\">");
    dumpAtoms(atoms);
    System.out.println("</e>");
  }

  private static void dumpAtoms(Vector v) {
    int n = v.size();
    for (int i = 0; i < n; i++) {
      Atom a = (Atom)v.elementAt(i);
      Entity e = a.getEntity();
      if (e != null)
	dumpEntity(e.name, e.atoms);
      else if (a.getTokenType() != Tokenizer.TOK_PROLOG_S) {
	System.out.print("<t>");
	dumpString(a.getToken());
	System.out.println("</t>");
      }
    }
  }
  
  private static void dumpString(String s) {
    int n = s.length();
    for (int i = 0; i < n; i++)
      switch (s.charAt(i)) {
      case '<':
	System.out.print("&lt;");
	break;
      case '>':
	System.out.print("&gt;");
	break;
      case '&':
	System.out.print("&amp;");
	break;
      default:
	System.out.print(s.charAt(i));
	break;
      }
  }
}
