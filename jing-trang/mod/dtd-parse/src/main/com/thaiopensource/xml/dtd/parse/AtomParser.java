package com.thaiopensource.xml.dtd.parse;

import java.util.Vector;

import com.thaiopensource.xml.tok.Tokenizer;

class AtomParser {
  private final DtdBuilder db;
  private final AtomStream as;
  private final PrologParser pp;
  private final Vector v;
  private Particle group;

  AtomParser(DtdBuilder db, AtomStream as, PrologParser pp, Vector v) {
    this.db = db;
    this.as = as;
    this.pp = pp;
    this.v = v;
  }

  AtomParser(DtdBuilder db, AtomStream as, PrologParser pp, Particle group) {
    this.db = db;
    this.as = as;
    this.pp = pp;
    this.v = group.particles;
    this.group = group;
  }

  void parse() {
    try {
      parseDecls();
      pp.end();
    }
    catch (PrologSyntaxException e) {
      throw new Error("syntax error on reparse at end of file");
    }
  }

  private void parseDecls() throws PrologSyntaxException {
    while (as.advance()) {
      Decl d = null;
      if (as.entity != null) {
	d = new Decl(Decl.REFERENCE);
	d.entity = as.entity;
	v.addElement(d);
	int start = v.size();
	new AtomParser(db, new AtomStream(as.entity.atoms), pp, v).parseDecls();
	d.entity.setParsed(Entity.DECL_LEVEL, v, start, v.size());
	d = new Decl(Decl.REFERENCE_END);
      }
      else {
	doAction();
	switch (as.tokenType) {
	case Tokenizer.TOK_COMMENT:
	  d = new Decl(Decl.COMMENT);
	  d.value = as.token.substring(4, as.token.length() - 3);
	  break;
	case Tokenizer.TOK_PI:
	  d = new Decl(Decl.PROCESSING_INSTRUCTION);
	  d.value = as.token.substring(2, as.token.length() - 2);
	  break;
	case Tokenizer.TOK_PROLOG_S:
	case Tokenizer.TOK_XML_DECL:
	  break;
	case Tokenizer.TOK_DECL_OPEN:
	  {
	    int type;
	    if (as.token.equals("<!ENTITY"))
	      type = Decl.ENTITY;
	    else if (as.token.equals("<!ATTLIST"))
	      type = Decl.ATTLIST;
	    else if (as.token.equals("<!ELEMENT"))
	      type = Decl.ELEMENT;
	    else if (as.token.equals("<!NOTATION"))
	      type = Decl.NOTATION;
	    else
	      throw new Error("unexpected decl type"); // should have been caught
	    d = new Decl(type);
	    d.params = new Vector();
	    new AtomParser(db, as, pp, d.params).parseParams();
	  }
	  break;
	case Tokenizer.TOK_COND_SECT_OPEN:
	  {
	    Vector params = new Vector();
	    // current token should be "["
	    if (new AtomParser(db, as, pp, params).parseParams()) {
	      d = new Decl(Decl.IGNORE_SECTION);
	      as.advance();
	      d.value = as.token.substring(0, as.token.length() - 3);
	    }
	    else {
	      d = new Decl(Decl.INCLUDE_SECTION);
	      d.decls = new Vector();
	      new AtomParser(db, as, pp, d.decls).parseDecls();
	    }
	    d.params = params;
	  }
	  break;
	case Tokenizer.TOK_COND_SECT_CLOSE:
	  return;
	default:
	  throw new Error("unexpected decl on reparse");
	}
      }
      if (d != null)
	v.addElement(d);
    }
  }

  // Return true for IGNORE status keyword spec

  private boolean parseParams() throws PrologSyntaxException {
    while (as.advance()) {
      Param p = null;
      if (as.entity != null) {
	p = new Param(Param.REFERENCE);
	p.entity = as.entity;
	PrologParser ppSaved;
	if (p.entity.overrides != null)
	  ppSaved = (PrologParser)pp.clone();
	else
	  ppSaved = null;
	v.addElement(p);
	int start = v.size();
	new AtomParser(db, new AtomStream(as.entity.atoms), pp, v).parseParams();
	if (v.size() == start && pp.expectingAttributeName())
	  v.addElement(new Param(Param.EMPTY_ATTRIBUTE_GROUP));
	p.entity.setParsed(Entity.PARAM_LEVEL, v, start, v.size());
	for (Entity overridden = p.entity.overrides;
	     overridden != null;
	     overridden = overridden.overrides) {
	  if (overridden.atoms != null) {
	    Vector tem = new Vector();
	    AtomParser ap = new AtomParser(db,
					   new AtomStream(overridden.atoms),
					   (PrologParser)ppSaved.clone(),
					   tem);
	    try {
	      ap.parseParams();
	      if (tem.size() == 0 && ap.pp.expectingAttributeName())
		tem.addElement(new Param(Param.EMPTY_ATTRIBUTE_GROUP));
	      if (ap.pp.isCompatible(pp))
		overridden.setParsed(Entity.PARAM_LEVEL, tem, 0, tem.size());
	      else
		overridden.inconsistentParse();
	    }
	    catch (PrologSyntaxException e) {
	      overridden.inconsistentParse();
	    }
	  }
	}
	p = new Param(Param.REFERENCE_END);
      }
      else {
	int action = doAction();
	switch (as.tokenType) {
	case Tokenizer.TOK_OPEN_BRACKET:
	  return action == PrologParser.ACTION_IGNORE_SECT;
	case Tokenizer.TOK_DECL_CLOSE:
	  return false;
	case Tokenizer.TOK_OPEN_PAREN:
	  switch (action) {
	  case PrologParser.ACTION_GROUP_OPEN:
	    p = new Param(Param.MODEL_GROUP);
	    break;
	  case PrologParser.ACTION_ENUM_GROUP_OPEN:
	    p = new Param(Param.ATTRIBUTE_VALUE_GROUP);
	    break;
	  case PrologParser.ACTION_NOTATION_GROUP_OPEN:
	    p = new Param(Param.NOTATION_GROUP);
	    break;
	  }
	  p.group = parseGroup();
	  break;
	case Tokenizer.TOK_LITERAL:
	  switch (action) {
	  case PrologParser.ACTION_DEFAULT_ATTRIBUTE_VALUE:
	    p = new Param(Param.DEFAULT_ATTRIBUTE_VALUE);
	    p.value = db.getNormalized(as.token.substring(1, as.token.length() - 1));
	    break;
	  default:
	    p = new Param(Param.LITERAL);
	    p.value = as.token.substring(1, as.token.length() - 1);
	    break;
	  }
	  break;
	case Tokenizer.TOK_PERCENT:
	  p = new Param(Param.PERCENT);
	  break;
	case Tokenizer.TOK_NAME:
	case Tokenizer.TOK_PREFIXED_NAME:
	  switch (action) {
	  case PrologParser.ACTION_CONTENT_ANY:
	    p = new Param(Param.ANY);
	    break;
	  case PrologParser.ACTION_CONTENT_EMPTY:
	    p = new Param(Param.EMPTY);
	    break;
	  case PrologParser.ACTION_ELEMENT_NAME:
	  case PrologParser.ACTION_ATTLIST_ELEMENT_NAME:
	    p = new Param(Param.ELEMENT_NAME);
	    p.value = as.token;
	    break;
	  case PrologParser.ACTION_ATTRIBUTE_NAME:
	    p = new Param(Param.ATTRIBUTE_NAME);
	    p.value = as.token;
	    break;
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_CDATA:
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_ID:
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_IDREF:
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_IDREFS:
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_ENTITY:
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_ENTITIES:
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_NMTOKEN:
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_NMTOKENS:
	    p = new Param(Param.ATTRIBUTE_TYPE);
	    p.value = as.token;
	    break;
	  case PrologParser.ACTION_ATTRIBUTE_TYPE_NOTATION:
	    p = new Param(Param.ATTRIBUTE_TYPE_NOTATION);
	    break;
	  case PrologParser.ACTION_SECTION_STATUS_IGNORE:
	    p = new Param(Param.IGNORE);
	    break;
	  case PrologParser.ACTION_SECTION_STATUS_INCLUDE:
	    p = new Param(Param.INCLUDE);
	    break;
	  default:
	    p = new Param(Param.OTHER);
	    p.value = as.token;
	  }
	  break;
	case Tokenizer.TOK_POUND_NAME:
	  switch (action) {
	  case PrologParser.ACTION_IMPLIED_ATTRIBUTE_VALUE:
	    p = new Param(Param.IMPLIED);
	    break;
	  case PrologParser.ACTION_REQUIRED_ATTRIBUTE_VALUE:
	    p = new Param(Param.REQUIRED);
	    break;
	  case PrologParser.ACTION_FIXED_ATTRIBUTE_VALUE:
	    p = new Param(Param.FIXED);
	    break;
	  default:
	    throw new Error("unexpected name after #");
	  }
	  break;
	case Tokenizer.TOK_PROLOG_S:
	  break;
	default:
	  throw new Error("unexpected parameter on reparse");
	}
      }
      if (p != null)
	v.addElement(p);
    }
    return false;
  }

  private Particle parseGroup() throws PrologSyntaxException {
    Particle g = new Particle(Particle.GROUP);
    g.particles = new Vector();
    new AtomParser(db, as, pp, g).parseParticles();
    int n = g.particles.size();
    int flags = 0;
    for (int i = 0; i < n; i++) {
      switch (((Particle)g.particles.elementAt(i)).type) {
      case Particle.GROUP:
	flags |= Entity.GROUP_CONTAINS_GROUP;
	break;
      case Particle.CONNECT_OR:
	flags |= Entity.GROUP_CONTAINS_OR;
	break;
      case Particle.CONNECT_SEQ:
	flags |= Entity.GROUP_CONTAINS_SEQ;
	break;
      case Particle.PCDATA:
	flags |= Entity.GROUP_CONTAINS_PCDATA;
	break;
      case Particle.ELEMENT_NAME:
	flags |= Entity.GROUP_CONTAINS_ELEMENT_NAME;
	break;
      case Particle.NMTOKEN:
	flags |= Entity.GROUP_CONTAINS_NMTOKEN;
	break;
      }
    }
    for (int i = 0; i < n; i++) {
      Particle p = (Particle)g.particles.elementAt(i);
      if (p.type == Particle.REFERENCE)
	p.entity.groupFlags |= flags;
    }
    return g;
  }

  private void parseParticles() throws PrologSyntaxException {
    while (as.advance()) {
      Particle p = null;
      if (as.entity != null) {
	p = new Particle(Particle.REFERENCE);
	p.entity = as.entity;
	PrologParser ppSaved;
	if (p.entity.overrides != null)
	  ppSaved = (PrologParser)pp.clone();
	else
	  ppSaved = null;
	v.addElement(p);
	int start = v.size();
	new AtomParser(db, new AtomStream(as.entity.atoms), pp, group).parseParticles();
	p.entity.setParsed(Entity.PARTICLE_LEVEL, v, start, v.size());
	for (Entity overridden = p.entity.overrides;
	     overridden != null;
	     overridden = overridden.overrides) {
	  if (overridden.atoms != null) {
	    Particle g = new Particle(Particle.GROUP);
	    g.particles = new Vector();
	    AtomParser ap = new AtomParser(db,
					   new AtomStream(overridden.atoms),
					   (PrologParser)ppSaved.clone(),
					   g);
	    try {
	      ap.parseParticles();
	      if (ap.pp.isCompatible(pp))
		overridden.setParsed(Entity.PARTICLE_LEVEL,
				     g.particles,
				     0,
				     g.particles.size());
	      else
		overridden.inconsistentParse();

	    }
	    catch (PrologSyntaxException e) {
	      overridden.inconsistentParse();
	    }
	  }
	}
	p = new Particle(Particle.REFERENCE_END);
      } 
      else {
	int action = doAction();
	switch (as.tokenType) {
	case Tokenizer.TOK_POUND_NAME:
	  p = new Particle(Particle.PCDATA);
	  break;
	case Tokenizer.TOK_NAME:
	case Tokenizer.TOK_PREFIXED_NAME:
	  p = new Particle(action == PrologParser.ACTION_CONTENT_ELEMENT
			   ? Particle.ELEMENT_NAME
			   : Particle.NMTOKEN);
	  p.value = as.token;
	  break;
	case Tokenizer.TOK_NMTOKEN:
	  p = new Particle(Particle.NMTOKEN);
	  p.value = as.token;
	  break;
	case Tokenizer.TOK_NAME_QUESTION:
	  p = new Particle(Particle.ELEMENT_NAME);
	  p.value = as.token.substring(0, as.token.length() - 1);
	  p.occur = '?';
	  break;
	case Tokenizer.TOK_NAME_ASTERISK:
	  p = new Particle(Particle.ELEMENT_NAME);
	  p.value = as.token.substring(0, as.token.length() - 1);
	  p.occur = '*';
	  break;
	case Tokenizer.TOK_NAME_PLUS:
	  p = new Particle(Particle.ELEMENT_NAME);
	  p.value = as.token.substring(0, as.token.length() - 1);
	  p.occur = '+';
	  break;
	case Tokenizer.TOK_OPEN_PAREN:
	  p = parseGroup();
	  break;
	case Tokenizer.TOK_CLOSE_PAREN:
	  return;
	case Tokenizer.TOK_CLOSE_PAREN_QUESTION:
	  group.occur = '?';
	  return;
	case Tokenizer.TOK_CLOSE_PAREN_ASTERISK:
	  group.occur = '*';
	  return;
	case Tokenizer.TOK_CLOSE_PAREN_PLUS:
	  group.occur = '+';
	  return;
	case Tokenizer.TOK_OR:
	  p = new Particle(Particle.CONNECT_OR);
	  break;
	case Tokenizer.TOK_COMMA:
	  p = new Particle(Particle.CONNECT_SEQ);
	  break;
	case Tokenizer.TOK_PROLOG_S:
	  break;
	default:
	  throw new Error("unexpected particle on reparse");
	}
      }
      if (p != null)
	v.addElement(p);
    }
  }

  private int doAction() throws PrologSyntaxException {
    return pp.action(as.tokenType, as.token);
  }
}
