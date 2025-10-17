package com.thaiopensource.xml.dtd.parse;

import com.thaiopensource.xml.tok.Tokenizer;

/**
 * Parses the prolog of an XML document.
 * A <code>PrologParser</code> object represents the state of a parse
 * of the prolog.
 * It operates on the tokens returned
 * by <code>Tokenizer.tokenizeProlog</code>.
 * It does not build any data structures to represent the information
 * in the prolog; instead it tells the caller the action needed
 * for each token.
 * The state of the parse can be saved by using the <code>clone</code>
 * method.
 */
public class PrologParser implements Cloneable {
  public static final int ACTION_NONE = 0;
  public static final int ACTION_XML_DECL = ACTION_NONE + 1;
  public static final int ACTION_TEXT_DECL = ACTION_XML_DECL + 1;
  public static final int ACTION_PI = ACTION_TEXT_DECL + 1;
  public static final int ACTION_COMMENT = ACTION_PI + 1;
  public static final int ACTION_DOCTYPE_NAME = ACTION_COMMENT + 1;
  public static final int ACTION_DOCTYPE_SYSTEM_ID = ACTION_DOCTYPE_NAME + 1;
  public static final int ACTION_DOCTYPE_PUBLIC_ID = ACTION_DOCTYPE_SYSTEM_ID + 1;
  public static final int ACTION_DOCTYPE_SUBSET = ACTION_DOCTYPE_PUBLIC_ID + 1;
  public static final int ACTION_DOCTYPE_CLOSE = ACTION_DOCTYPE_SUBSET + 1;
  public static final int ACTION_GENERAL_ENTITY_NAME = ACTION_DOCTYPE_CLOSE + 1;
  public static final int ACTION_PARAM_ENTITY_NAME = ACTION_GENERAL_ENTITY_NAME + 1;
  public static final int ACTION_ENTITY_VALUE_WITH_PEREFS = ACTION_PARAM_ENTITY_NAME + 1;
  public static final int ACTION_ENTITY_VALUE_NO_PEREFS = ACTION_ENTITY_VALUE_WITH_PEREFS + 1;
  public static final int ACTION_ENTITY_SYSTEM_ID = ACTION_ENTITY_VALUE_NO_PEREFS + 1;
  public static final int ACTION_ENTITY_PUBLIC_ID = ACTION_ENTITY_SYSTEM_ID + 1;
  public static final int ACTION_ENTITY_NOTATION_NAME = ACTION_ENTITY_PUBLIC_ID + 1;
  public static final int ACTION_NOTATION_NAME = ACTION_ENTITY_NOTATION_NAME + 1;
  public static final int ACTION_NOTATION_SYSTEM_ID = ACTION_NOTATION_NAME + 1;
  public static final int ACTION_NOTATION_PUBLIC_ID = ACTION_NOTATION_SYSTEM_ID + 1;
  public static final int ACTION_ATTRIBUTE_NAME = ACTION_NOTATION_PUBLIC_ID + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_CDATA = ACTION_ATTRIBUTE_NAME + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_ID = ACTION_ATTRIBUTE_TYPE_CDATA + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_IDREF = ACTION_ATTRIBUTE_TYPE_ID + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_IDREFS = ACTION_ATTRIBUTE_TYPE_IDREF + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_ENTITY = ACTION_ATTRIBUTE_TYPE_IDREFS + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_ENTITIES = ACTION_ATTRIBUTE_TYPE_ENTITY + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_NMTOKEN = ACTION_ATTRIBUTE_TYPE_ENTITIES + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_NMTOKENS = ACTION_ATTRIBUTE_TYPE_NMTOKEN + 1;
  public static final int ACTION_ATTRIBUTE_TYPE_NOTATION = ACTION_ATTRIBUTE_TYPE_NMTOKENS + 1;
  public static final int ACTION_ATTRIBUTE_ENUM_VALUE = ACTION_ATTRIBUTE_TYPE_NOTATION + 1;
  public static final int ACTION_ATTRIBUTE_NOTATION_VALUE = ACTION_ATTRIBUTE_ENUM_VALUE + 1;
  public static final int ACTION_ATTLIST_ELEMENT_NAME = ACTION_ATTRIBUTE_NOTATION_VALUE + 1;
  public static final int ACTION_IMPLIED_ATTRIBUTE_VALUE = ACTION_ATTLIST_ELEMENT_NAME + 1;
  public static final int ACTION_REQUIRED_ATTRIBUTE_VALUE = ACTION_IMPLIED_ATTRIBUTE_VALUE + 1;
  public static final int ACTION_DEFAULT_ATTRIBUTE_VALUE = ACTION_REQUIRED_ATTRIBUTE_VALUE + 1;
  public static final int ACTION_FIXED_ATTRIBUTE_VALUE = ACTION_DEFAULT_ATTRIBUTE_VALUE + 1;
  public static final int ACTION_ELEMENT_NAME = ACTION_FIXED_ATTRIBUTE_VALUE + 1;
  public static final int ACTION_CONTENT_ANY = ACTION_ELEMENT_NAME + 1;
  public static final int ACTION_CONTENT_EMPTY = ACTION_CONTENT_ANY + 1;
  public static final int ACTION_CONTENT_PCDATA = ACTION_CONTENT_EMPTY + 1;
  public static final int ACTION_GROUP_OPEN = ACTION_CONTENT_PCDATA + 1;
  public static final int ACTION_GROUP_CLOSE = ACTION_GROUP_OPEN + 1;
  public static final int ACTION_GROUP_CLOSE_REP = ACTION_GROUP_CLOSE + 1;
  public static final int ACTION_GROUP_CLOSE_OPT = ACTION_GROUP_CLOSE_REP + 1;
  public static final int ACTION_GROUP_CLOSE_PLUS = ACTION_GROUP_CLOSE_OPT + 1;
  public static final int ACTION_GROUP_CHOICE = ACTION_GROUP_CLOSE_PLUS + 1;
  public static final int ACTION_GROUP_SEQUENCE = ACTION_GROUP_CHOICE + 1;
  public static final int ACTION_CONTENT_ELEMENT = ACTION_GROUP_SEQUENCE + 1;
  public static final int ACTION_CONTENT_ELEMENT_REP = ACTION_CONTENT_ELEMENT + 1;
  public static final int ACTION_CONTENT_ELEMENT_OPT = ACTION_CONTENT_ELEMENT_REP + 1;
  public static final int ACTION_CONTENT_ELEMENT_PLUS = ACTION_CONTENT_ELEMENT_OPT + 1;
  public static final int ACTION_OUTER_PARAM_ENTITY_REF = ACTION_CONTENT_ELEMENT_PLUS + 1;
  public static final int ACTION_INNER_PARAM_ENTITY_REF = ACTION_OUTER_PARAM_ENTITY_REF + 1;
  public static final int ACTION_IGNORE_SECT = ACTION_INNER_PARAM_ENTITY_REF + 1;
  public static final int ACTION_DECL_CLOSE = ACTION_IGNORE_SECT + 1;
  public static final int ACTION_ENUM_GROUP_OPEN = ACTION_DECL_CLOSE + 1;
  public static final int ACTION_NOTATION_GROUP_OPEN = ACTION_ENUM_GROUP_OPEN + 1;
  public static final int ACTION_SECTION_STATUS_IGNORE = ACTION_NOTATION_GROUP_OPEN + 1;
  public static final int ACTION_SECTION_STATUS_INCLUDE = ACTION_SECTION_STATUS_IGNORE + 1;
  
  private static final byte prolog0 = 0;
  private static final byte prolog1 = prolog0 + 1;
  private static final byte prolog2 = prolog1 + 1;
  private static final byte doctype0 = prolog2 + 1;
  private static final byte doctype1 = doctype0 + 1;
  private static final byte doctype2 = doctype1 + 1;
  private static final byte doctype3 = doctype2 + 1;
  private static final byte doctype4 = doctype3 + 1;
  private static final byte doctype5 = doctype4 + 1;
  private static final byte internalSubset = doctype5 + 1;
  private static final byte entity0 = internalSubset + 1;
  private static final byte entity1 = entity0 + 1;
  private static final byte entity2 = entity1 + 1;
  private static final byte entity3 = entity2 + 1;
  private static final byte entity4 = entity3 + 1;
  private static final byte entity5 = entity4 + 1;
  private static final byte entity6 = entity5 + 1;
  private static final byte entity7 = entity6 + 1;
  private static final byte entity8 = entity7 + 1;
  private static final byte entity9 = entity8 + 1;
  private static final byte notation0 = entity9 + 1;
  private static final byte notation1 = notation0 + 1;
  private static final byte notation2 = notation1 + 1;
  private static final byte notation3 = notation2 + 1;
  private static final byte notation4 = notation3 + 1;
  private static final byte attlist0 = notation4 + 1;
  private static final byte attlist1 = attlist0 + 1;
  private static final byte attlist2 = attlist1 + 1;
  private static final byte attlist3 = attlist2 + 1;
  private static final byte attlist4 = attlist3 + 1;
  private static final byte attlist5 = attlist4 + 1;
  private static final byte attlist6 = attlist5 + 1;
  private static final byte attlist7 = attlist6 + 1;
  private static final byte attlist8 = attlist7 + 1;
  private static final byte attlist9 = attlist8 + 1;
  private static final byte element0 = attlist9 + 1;
  private static final byte element1 = element0 + 1;
  private static final byte element2 = element1 + 1;
  private static final byte element3 = element2 + 1;
  private static final byte element4 = element3 + 1;
  private static final byte element5 = element4 + 1;
  private static final byte element6 = element5 + 1;
  private static final byte element7 = element6 + 1;
  private static final byte declClose = element7 + 1;
  private static final byte externalSubset0 = declClose + 1;
  private static final byte externalSubset1 = externalSubset0 + 1;
  private static final byte condSect0 = externalSubset1 + 1;
  private static final byte condSect1 = condSect0 + 1;
  private static final byte condSect2 = condSect1 + 1;

  private byte state;
  private int groupLevel;
  private int includeLevel;
  private byte connector[] = new byte[2];
  private boolean documentEntity;

  public static final byte PROLOG = 0;
  public static final byte EXTERNAL_ENTITY = 1;
  public static final byte INTERNAL_ENTITY = 2;

  public PrologParser(byte type) {
    switch (type) {
    case PROLOG:
      documentEntity = true;
      state = prolog0;
      break;
    case EXTERNAL_ENTITY:
      documentEntity = false;
      state = externalSubset0;
      break;
    case INTERNAL_ENTITY:
      documentEntity = false;
      state = externalSubset1;
      break;
    default:
      throw new IllegalArgumentException();
    }
  }

  public final void end() throws PrologSyntaxException {
    switch (state) {
    case prolog0:
    case prolog1:
    case prolog2:
      break;
    case externalSubset0:
    case externalSubset1:
      if (includeLevel == 0)
	break;
      /* fall through */
    default:
      throw new PrologSyntaxException();
    }
  }

  public int action(int tok, String token) throws PrologSyntaxException {
    switch (state) {
    case prolog0:
      state = prolog1;
      if (tok == Tokenizer.TOK_XML_DECL)
	return ACTION_XML_DECL;
      /* fall through */
    case prolog1:
      if (tok == Tokenizer.TOK_DECL_OPEN
	  && matches(token, 2, "DOCTYPE")) {
	state = doctype0;
	return ACTION_NONE;
      }
      /* fall through */
    case prolog2:
      switch (tok) {
      case Tokenizer.TOK_PI:
	return ACTION_PI;
      case Tokenizer.TOK_COMMENT:
	return ACTION_COMMENT;
      }
      break;
    case doctype0:
      switch (tok) {
      case Tokenizer.TOK_NAME:
      case Tokenizer.TOK_PREFIXED_NAME:
	state = doctype1;
	return ACTION_DOCTYPE_NAME;
      }
      break;
    case doctype1:
      switch (tok) {
      case Tokenizer.TOK_OPEN_BRACKET:
	state = internalSubset;
	return ACTION_DOCTYPE_SUBSET;
      case Tokenizer.TOK_DECL_CLOSE:
	state = prolog2;
	return ACTION_DOCTYPE_CLOSE;
      case Tokenizer.TOK_NAME:
	if (token.equals("SYSTEM")) {
	  state = doctype3;
	  return ACTION_NONE;
	}
	if (token.equals("PUBLIC")) {
	  state = doctype2;
	  return ACTION_NONE;
	}
	break;
      }
      break;
    case doctype2:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = doctype3;
	return ACTION_DOCTYPE_PUBLIC_ID;
      }
      break;
    case doctype3:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = doctype4;
	return ACTION_DOCTYPE_SYSTEM_ID;
      }
      break;
    case doctype4:
      switch (tok) {
      case Tokenizer.TOK_OPEN_BRACKET:
	state = internalSubset;
	return ACTION_DOCTYPE_SUBSET;
      case Tokenizer.TOK_DECL_CLOSE:
	state = prolog2;
	return ACTION_DOCTYPE_CLOSE;
      }
      break;
    case doctype5:
      if (tok == Tokenizer.TOK_DECL_CLOSE) {
	state = prolog2;
	return ACTION_DOCTYPE_CLOSE;
      }
      break;
    case externalSubset0:
      state = externalSubset1;
      if (tok == Tokenizer.TOK_XML_DECL)
	return ACTION_TEXT_DECL;
      /* fall through */
    case externalSubset1:
      switch (tok) {
      case Tokenizer.TOK_COND_SECT_OPEN:
	state = condSect0;
	return ACTION_NONE;
      case Tokenizer.TOK_COND_SECT_CLOSE:
	if (includeLevel == 0)
	  break;
	--includeLevel;
	return ACTION_NONE;
      case Tokenizer.TOK_CLOSE_BRACKET:
	throw new PrologSyntaxException();
      }
      /* fall through */
    case internalSubset:
      switch (tok) {
      case Tokenizer.TOK_DECL_OPEN:
	if (matches(token, 2, "ENTITY")) {
	  state = entity0;
	  return ACTION_NONE;
	}
	if (matches(token, 2, "ATTLIST")) {
	  state = attlist0;
	  return ACTION_NONE;
	}
	if (matches(token, 2, "ELEMENT")) {
	  state = element0;
	  return ACTION_NONE;
	}
	if (matches(token, 2, "NOTATION")) {
	  state = notation0;
	  return ACTION_NONE;
	}
	break;
      case Tokenizer.TOK_PI:
	return ACTION_PI;
      case Tokenizer.TOK_COMMENT:
	return ACTION_COMMENT;
      case Tokenizer.TOK_PARAM_ENTITY_REF:
	return ACTION_OUTER_PARAM_ENTITY_REF;
      case Tokenizer.TOK_CLOSE_BRACKET:
	state = doctype5;
	return ACTION_NONE;
      }
      break;
    case entity0:
      switch (tok) {
      case Tokenizer.TOK_PERCENT:
	state = entity1;
	return ACTION_NONE;
      case Tokenizer.TOK_NAME:
	state = entity2;
	return ACTION_GENERAL_ENTITY_NAME;
      }
      break;
    case entity1:
      if (tok == Tokenizer.TOK_NAME) {
	state = entity7;
	return ACTION_PARAM_ENTITY_NAME;
      }
      break;
    case entity2:
      switch (tok) {
      case Tokenizer.TOK_NAME:
	if (token.equals("SYSTEM")) {
	  state = entity4;
	  return ACTION_NONE;
	}
	if (token.equals("PUBLIC")) {
	  state = entity3;
	  return ACTION_NONE;
	}
	break;
      case Tokenizer.TOK_LITERAL:
	state = declClose;
	return (documentEntity
		? ACTION_ENTITY_VALUE_NO_PEREFS
		: ACTION_ENTITY_VALUE_WITH_PEREFS);
      }
      break;
    case entity3:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = entity4;
	return ACTION_ENTITY_PUBLIC_ID;
      }
      break;
    case entity4:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = entity5;
	return ACTION_ENTITY_SYSTEM_ID;
      }
      break;
    case entity5:
      switch (tok) {
      case Tokenizer.TOK_DECL_CLOSE:
	state = documentEntity ? internalSubset : externalSubset1;
	return ACTION_DECL_CLOSE;
      case Tokenizer.TOK_NAME:
	if (token.equals("NDATA")) {
	  state = entity6;
	  return ACTION_NONE;
	}
	break;
      }
      break;
    case entity6:
      switch (tok) {
      case Tokenizer.TOK_NAME:
	state = declClose;
	return ACTION_ENTITY_NOTATION_NAME;
      }
      break;
    case entity7:
      switch (tok) {
      case Tokenizer.TOK_NAME:
	if (token.equals("SYSTEM")) {
	  state = entity9;
	  return ACTION_NONE;
	}
	if (token.equals("PUBLIC")) {
	  state = entity8;
	  return ACTION_NONE;
	}
	break;
      case Tokenizer.TOK_LITERAL:
	state = declClose;
	return (documentEntity
		? ACTION_ENTITY_VALUE_NO_PEREFS
		: ACTION_ENTITY_VALUE_WITH_PEREFS);
      }
      break;
    case entity8:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = entity9;
	return ACTION_ENTITY_PUBLIC_ID;
      }
      break;
    case entity9:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = declClose;
	return ACTION_ENTITY_SYSTEM_ID;
      }
      break;
    case notation0:
      if (tok == Tokenizer.TOK_NAME) {
	state = notation1;
	return ACTION_NOTATION_NAME;
      }
      break;
    case notation1:
      switch (tok) {
      case Tokenizer.TOK_NAME:
	if (token.equals("SYSTEM")) {
	  state = notation3;
	  return ACTION_NONE;
	}
	if (token.equals("PUBLIC")) {
	  state = notation2;
	  return ACTION_NONE;
	}
	break;
      }
      break;
    case notation2:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = notation4;
	return ACTION_NOTATION_PUBLIC_ID;
      }
      break;
    case notation3:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = declClose;
	return ACTION_NOTATION_SYSTEM_ID;
      }
      break;
    case notation4:
      switch (tok) {
      case Tokenizer.TOK_LITERAL:
	state = declClose;
	return ACTION_NOTATION_SYSTEM_ID;
      case Tokenizer.TOK_DECL_CLOSE:
	state = documentEntity ? internalSubset : externalSubset1;
	return ACTION_DECL_CLOSE;
      }
      break;
    case attlist0:
      switch (tok) {
      case Tokenizer.TOK_NAME:
      case Tokenizer.TOK_PREFIXED_NAME:
	state = attlist1;
	return ACTION_ATTLIST_ELEMENT_NAME;
      }
      break;
    case attlist1:
      switch (tok) {
      case Tokenizer.TOK_DECL_CLOSE:
	state = documentEntity ? internalSubset : externalSubset1;
	return ACTION_NONE;
      case Tokenizer.TOK_NAME:
      case Tokenizer.TOK_PREFIXED_NAME:
	state = attlist2;
	return ACTION_ATTRIBUTE_NAME;
      }
      break;
    case attlist2:
      switch (tok) {
      case Tokenizer.TOK_NAME:
	for (int i = 0; i < attributeTypes.length; i++)
	  if (token.equals(attributeTypes[i])) {
	    state = attlist8;
	    return ACTION_ATTRIBUTE_TYPE_CDATA + i;
	  }
	if (token.equals("NOTATION")) {
	  state = attlist5;
	  return ACTION_ATTRIBUTE_TYPE_NOTATION;
	}
	break;
      case Tokenizer.TOK_OPEN_PAREN:
	groupLevel = 1;
	state = attlist3;
	return ACTION_ENUM_GROUP_OPEN;
      }
      break;
    case attlist3:
      switch (tok) {
      case Tokenizer.TOK_NMTOKEN:
      case Tokenizer.TOK_NAME:
      case Tokenizer.TOK_PREFIXED_NAME:
	state = attlist4;
	return ACTION_ATTRIBUTE_ENUM_VALUE;
      }
      break;
    case attlist4:
      switch (tok) {
      case Tokenizer.TOK_CLOSE_PAREN:
	state = attlist8;
	groupLevel = 0;
	return ACTION_NONE;
      case Tokenizer.TOK_OR:
	state = attlist3;
	return ACTION_NONE;
      }
      break;
    case attlist5:
      if (tok == Tokenizer.TOK_OPEN_PAREN) {
	state = attlist6;
	groupLevel = 1;
	return ACTION_NOTATION_GROUP_OPEN;
      }
      break;
    case attlist6:
      if (tok == Tokenizer.TOK_NAME) {
	state = attlist7;
	return ACTION_ATTRIBUTE_NOTATION_VALUE;
      }
      break;
    case attlist7:
      switch (tok) {
      case Tokenizer.TOK_CLOSE_PAREN:
	groupLevel = 0;
	state = attlist8;
	return ACTION_NONE;
      case Tokenizer.TOK_OR:
	state = attlist6;
	return ACTION_NONE;
      }
      break;
      /* default value */
    case attlist8:
      switch (tok) {
      case Tokenizer.TOK_POUND_NAME:
	if (matches(token, 1, "IMPLIED")) {
	  state = attlist1;
	  return ACTION_IMPLIED_ATTRIBUTE_VALUE;
	}
	if (matches(token, 1, "REQUIRED")) {
	  state = attlist1;
	  return ACTION_REQUIRED_ATTRIBUTE_VALUE;
	}
	if (matches(token, 1, "FIXED")) {
	  state = attlist9;
	  return ACTION_FIXED_ATTRIBUTE_VALUE;
	}
	break;
      case Tokenizer.TOK_LITERAL:
	state = attlist1;
	return ACTION_DEFAULT_ATTRIBUTE_VALUE;
      }
      break;
    case attlist9:
      if (tok == Tokenizer.TOK_LITERAL) {
	state = attlist1;
	return ACTION_DEFAULT_ATTRIBUTE_VALUE;
      }
      break;
    case element0:
      switch (tok) {
      case Tokenizer.TOK_NAME:
      case Tokenizer.TOK_PREFIXED_NAME:
	state = element1;
	return ACTION_ELEMENT_NAME;
      }
      break;
    case element1:
      switch (tok) {
      case Tokenizer.TOK_NAME:
	if (token.equals("EMPTY")) {
	  state = declClose;
	  return ACTION_CONTENT_EMPTY;
	}
	if (token.equals("ANY")) {
	  state = declClose;
	  return ACTION_CONTENT_ANY;
	}
	break;
      case Tokenizer.TOK_OPEN_PAREN:
	state = element2;
	groupLevel = 1;
	connector[0] = (byte)0;
	return ACTION_GROUP_OPEN;
      }
      break;
    case element2:
      switch (tok) {
      case Tokenizer.TOK_POUND_NAME:
	if (matches(token, 1, "PCDATA")) {
	  state = element3;
	  return ACTION_CONTENT_PCDATA;
	}
	break;
      case Tokenizer.TOK_OPEN_PAREN:
	groupLevel = 2;
	connector[1] = (byte)0;
	state = element6;
	return ACTION_GROUP_OPEN;
      case Tokenizer.TOK_NAME:
      case Tokenizer.TOK_PREFIXED_NAME:
	state = element7;
	return ACTION_CONTENT_ELEMENT;
      case Tokenizer.TOK_NAME_QUESTION:
	state = element7;
	return ACTION_CONTENT_ELEMENT_OPT;
      case Tokenizer.TOK_NAME_ASTERISK:
	state = element7;
	return ACTION_CONTENT_ELEMENT_REP;
      case Tokenizer.TOK_NAME_PLUS:
	state = element7;
	return ACTION_CONTENT_ELEMENT_PLUS;
      }
      break;
    case element3:
      switch (tok) {
      case Tokenizer.TOK_CLOSE_PAREN:
      case Tokenizer.TOK_CLOSE_PAREN_ASTERISK:
	groupLevel = 0;
	state = declClose;
	return ACTION_GROUP_CLOSE_REP;
      case Tokenizer.TOK_OR:
	state = element4;
	return ACTION_GROUP_CHOICE;
      }
      break;
    case element4:
      switch (tok) {
      case Tokenizer.TOK_NAME:
      case Tokenizer.TOK_PREFIXED_NAME:
	state = element5;
	return ACTION_CONTENT_ELEMENT;
      }
      break;
    case element5:
      switch (tok) {
      case Tokenizer.TOK_CLOSE_PAREN_ASTERISK:
	groupLevel = 0;
	state = declClose;
	return ACTION_GROUP_CLOSE_REP;
      case Tokenizer.TOK_OR:
	state = element4;
	return ACTION_GROUP_CHOICE;
      }
      break;
    case element6:
      switch (tok) {
      case Tokenizer.TOK_OPEN_PAREN:
	if (groupLevel >= connector.length) {
	  byte[] tem = new byte[connector.length << 1];
	  System.arraycopy(connector, 0, tem, 0, connector.length);
	  connector = tem;
	}
	connector[groupLevel] = (byte)0;
	groupLevel += 1;
	return ACTION_GROUP_OPEN;
      case Tokenizer.TOK_NAME:
      case Tokenizer.TOK_PREFIXED_NAME:
	state = element7;
	return ACTION_CONTENT_ELEMENT;
      case Tokenizer.TOK_NAME_QUESTION:
	state = element7;
	return ACTION_CONTENT_ELEMENT_OPT;
      case Tokenizer.TOK_NAME_ASTERISK:
	state = element7;
	return ACTION_CONTENT_ELEMENT_REP;
      case Tokenizer.TOK_NAME_PLUS:
	state = element7;
	return ACTION_CONTENT_ELEMENT_PLUS;
      }
      break;
    case element7:
      switch (tok) {
      case Tokenizer.TOK_CLOSE_PAREN:
	groupLevel -= 1;
	if (groupLevel == 0)
	  state = declClose;
	return ACTION_GROUP_CLOSE;
      case Tokenizer.TOK_CLOSE_PAREN_ASTERISK:
	groupLevel -= 1;
	if (groupLevel == 0)
	  state = declClose;
	return ACTION_GROUP_CLOSE_REP;
      case Tokenizer.TOK_CLOSE_PAREN_QUESTION:
	groupLevel -= 1;
	if (groupLevel == 0)
	  state = declClose;
	return ACTION_GROUP_CLOSE_OPT;
      case Tokenizer.TOK_CLOSE_PAREN_PLUS:
	groupLevel -= 1;
	if (groupLevel == 0)
	  state = declClose;
	return ACTION_GROUP_CLOSE_PLUS;
      case Tokenizer.TOK_COMMA:
	state = element6;
	if (connector[groupLevel - 1] == (byte)'|')
	  break;
	connector[groupLevel - 1] = (byte)',';
	return ACTION_GROUP_SEQUENCE;
      case Tokenizer.TOK_OR:
	state = element6;
	if (connector[groupLevel - 1] == (byte)',')
	  break;
	connector[groupLevel - 1] = (byte)'|';
	return ACTION_GROUP_CHOICE;
      }
      break;
    case declClose:
      if (tok == Tokenizer.TOK_DECL_CLOSE) {
	state = documentEntity ? internalSubset : externalSubset1;
	return ACTION_DECL_CLOSE;
      }
      break;
    case condSect0:
      if (tok == Tokenizer.TOK_NAME) {
	if (token.equals("INCLUDE")) {
	  state = condSect1;
	  return ACTION_SECTION_STATUS_INCLUDE;
	}
	if (token.equals("IGNORE")) {
	  state = condSect2;
	  return ACTION_SECTION_STATUS_IGNORE;
	}
      }
      break;
    case condSect1:
      if (tok == Tokenizer.TOK_OPEN_BRACKET) {
	state = externalSubset1;
	includeLevel++;
	return ACTION_NONE;
      }
      break;
    case condSect2:
      if (tok == Tokenizer.TOK_OPEN_BRACKET) {
	state = externalSubset1;
	return ACTION_IGNORE_SECT;
      }
      break;
    }
    if (tok == Tokenizer.TOK_PROLOG_S)
      return ACTION_NONE;
    if (tok == Tokenizer.TOK_PARAM_ENTITY_REF && !documentEntity)
      return ACTION_INNER_PARAM_ENTITY_REF;
    throw new PrologSyntaxException();
  }

  public Object clone() {
    try {
      PrologParser copy = (PrologParser)super.clone();
      copy.connector = new byte[connector.length];
      System.arraycopy(connector, 0, copy.connector, 0, groupLevel);
      return copy;
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }

  public boolean isCompatible(PrologParser orig) {
    if (groupLevel > 0
	&& connector[groupLevel - 1] != 0
	&& connector[groupLevel - 1] != orig.connector[groupLevel - 1])
      return false;
    return true;
  }

  public final int getGroupLevel() {
    return groupLevel;
  }

  public boolean expectingAttributeName() {
    return state == attlist1;
  }

  private static boolean matches(String token, int off, String key) {
    int keyLen = key.length();
    if (token.length() - off != keyLen)
      return false;
    return token.regionMatches(off, key, 0, keyLen);
  }

  private static final String[] attributeTypes = {
    "CDATA",
    "ID",
    "IDREF",
    "IDREFS",
    "ENTITY",
    "ENTITIES",
    "NMTOKEN",
    "NMTOKENS",
  };
}
