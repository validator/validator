package com.thaiopensource.xml.dtd.parse;

import java.io.IOException;

import com.thaiopensource.xml.dtd.om.DtdParser;
import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.em.*;

public class DtdParserImpl implements DtdParser {
  public DtdParserImpl() { }

  public Dtd parse(String systemId, EntityManager em) throws IOException {
    return parse(em.open(new ExternalId(systemId)), em);
  }

  public Dtd parse(OpenEntity entity, EntityManager em) throws IOException {
    DtdBuilder db = new Parser(entity, em).parse();
    db.unexpandEntities();
    db.createDecls();
    db.analyzeSemantics();
    return new DtdImpl(db.createTopLevel(),
		       entity.getBaseUri(),
		       entity.getEncoding());
  }
}
