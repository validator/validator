package com.thaiopensource.xml.dtd;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Dtd {

  private Vector topLevel;

  public Dtd(String filename) throws IOException {
    Reader r = new InputStreamReader(new BufferedInputStream(new FileInputStream(filename)));
    System.err.println("Parsing");
    DtdBuilder db = new Parser(r).parse();
    System.err.println("Unexpanding");
    db.unexpandEntities();
    System.err.println("Creating decls");
    db.createDecls();
    System.err.println("Analyze semantics");
    db.analyzeSemantics();
    topLevel = db.createTopLevel();
  }

  public TopLevel[] getAllTopLevel() {
    TopLevel[] tem = new TopLevel[topLevel.size()];
    for (int i = 0; i < tem.length; i++)
      tem[i] = (TopLevel)topLevel.elementAt(i);
    return tem;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    int n = topLevel.size();
    for (int i = 0; i < n; i++)
      ((TopLevel)topLevel.elementAt(i)).accept(visitor);
  }
}
