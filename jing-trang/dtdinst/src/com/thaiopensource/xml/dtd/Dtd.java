package com.thaiopensource.xml.dtd;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import java.io.IOException;

public class Dtd {

  private Vector topLevel;
  private String encoding;

  public Dtd(String systemId, EntityManager em) throws IOException {
    this(em.open(new ExternalId(systemId)), em);
  }

  public Dtd(OpenEntity entity, EntityManager em) throws IOException {
    this.encoding = entity.getEncoding();
    DtdBuilder db = new Parser(entity, em).parse();
    db.unexpandEntities();
    db.createDecls();
    db.analyzeSemantics();
    topLevel = db.createTopLevel();
  }

  public String getEncoding() {
    return encoding;
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
