package com.thaiopensource.xml.dtd;

import java.util.Vector;

class AtomParser {
  private AtomStream as;
  private PrologParser pp;
  private Vector v;
  private Particle group;

  AtomParser(AtomStream as, PrologParser pp, Vector v) {
    this.as = as;
    this.pp = pp;
    this.v = v;
  }

  AtomParser(AtomStream as, PrologParser pp, Particle group) {
    this.as = as;
    this.pp = pp;
    this.v = group.particles;
    this.group = group;
  }

  void parse() {
    parseDecls();
    try {
      pp.end();
    }
    catch (PrologSyntaxException e) {
      throw new Error("syntax error on reparse at end of file");
    }
  }

  private void parseDecls() {
    while (as.advance()) {
      Decl d = null;
      if (as.entity != null) {
	d = new Decl(Decl.REFERENCE);
	d.entity = as.entity;
	v.addElement(d);
	new AtomParser(new AtomStream(as.entity.atoms), pp, v).parseDecls();
	d = new Decl(Decl.REFERENCE_END);
      }
      else {
	int action = doAction();
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
	    new AtomParser(as, pp, d.params).parseParams();
	  }
	  break;
	case Tokenizer.TOK_COND_SECT_OPEN:
	  {
	    Vector params = new Vector();
	    // current token should be "["
	    if (new AtomParser(as, pp, params).parseParams()) {
	      d = new Decl(Decl.IGNORE_SECTION);
	      as.advance();
	      d.value = as.token.substring(0, as.token.length() - 3);
	    }
	    else
	      d = new Decl(Decl.START_INCLUDE_SECTION);
	    d.params = params;
	  }
	  break;
	case Tokenizer.TOK_COND_SECT_CLOSE:
	  d = new Decl(Decl.END_INCLUDE_SECTION);
	  break;
	default:
	  throw new Error("unexpected decl on reparse");
	}
      }
      if (d != null)
	v.addElement(d);
    }
  }

  // Return true for IGNORE status keyword spec

  private boolean parseParams() {
    while (as.advance()) {
      Param p = null;
      if (as.entity != null) {
	p = new Param(Param.REFERENCE);
	p.entity = as.entity;
	v.addElement(p);
	new AtomParser(new AtomStream(as.entity.atoms), pp, v).parseParams();
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
	  p = new Param(action == PrologParser.ACTION_GROUP_OPEN
			? Param.MODEL_GROUP
			: Param.ATTRIBUTE_VALUE_GROUP);
	  p.group = parseGroup();
	  break;
	case Tokenizer.TOK_LITERAL:
	  switch (action) {
	  case PrologParser.ACTION_DEFAULT_ATTRIBUTE_VALUE:
	    p = new Param(Param.DEFAULT_ATTRIBUTE_VALUE);
	    break;
	  default:
	    p = new Param(Param.LITERAL);
	    break;
	  }
	  p.value = as.token.substring(1, as.token.length() - 1);
	  break;
	case Tokenizer.TOK_PERCENT:
	  p = new Param(Param.PERCENT);
	  break;
	case Tokenizer.TOK_NAME:
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

  private Particle parseGroup() {
    Particle g = new Particle(Particle.GROUP);
    g.particles = new Vector();
    new AtomParser(as, pp, g).parseParticles();
    return g;
  }

  private void parseParticles() {
    while (as.advance()) {
      Particle p = null;
      if (as.entity != null) {
	p = new Particle(Particle.REFERENCE);
	p.entity = as.entity;
	v.addElement(p);
	new AtomParser(new AtomStream(as.entity.atoms), pp, group).parseParticles();
	p = new Particle(Particle.REFERENCE_END);
      }
      else {
	int action = doAction();
	switch (as.tokenType) {
	case Tokenizer.TOK_POUND_NAME:
	  p = new Particle(Particle.PCDATA);
	  group.sep = '|';
	  break;
	case Tokenizer.TOK_NAME:
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
	  group.sep = '|';
	  break;
	case Tokenizer.TOK_COMMA:
	  group.sep = ',';
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

  private int doAction() {
    try {
      return pp.action(as.tokenType, as.token);
    }
    catch (PrologSyntaxException e) {
      throw new Error("syntax error on reparse");
    }
  }
}
