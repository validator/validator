package com.thaiopensource.xml.dtd.app;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

import com.thaiopensource.xml.dtd.om.*;
import com.thaiopensource.xml.out.XmlWriter;
import com.thaiopensource.xml.em.ExternalId;

public class RelaxNgWriter {
  private XmlWriter w;
  private boolean hadAny = false;
  private Hashtable elementNameTable = new Hashtable();
  private Hashtable defTable = new Hashtable();

  // These variables control the names use for definitions.
  private String colonReplacement = null;
  private String elementDeclPattern;
  private String attlistDeclPattern;
  private String anyName;

  static final int ELEMENT_DECL = 01;
  static final int ATTLIST_DECL = 02;
  static final int ELEMENT_REF = 04;

  // # is the category; % is the name in the category

  String DEFAULT_PATTERN = "#.%";

  static abstract class VisitorBase implements TopLevelVisitor {
    public void processingInstruction(String target, String value) throws Exception { }
    public void comment(String value) throws Exception { }
    public void flagDef(String name, Flag flag) throws Exception { }
    public void includedSection(Flag flag, TopLevel[] contents)
      throws Exception {
      for (int i = 0; i < contents.length; i++)
	contents[i].accept(this);
    }

    public void ignoredSection(Flag flag, String contents) throws Exception { }
    public void internalEntityDecl(String name, String value) throws Exception { }
    public void externalEntityDecl(String name, ExternalId externalId) throws Exception { }
    public void notationDecl(String name, ExternalId externalId) throws Exception { }
    public void nameSpecDef(String name, NameSpec nameSpec) throws Exception { }
    public void overriddenDef(Def def, boolean isDuplicate) throws Exception { }
    public void externalIdDef(String name, ExternalId externalId) throws Exception { }
    public void externalIdRef(String name, ExternalId externalId, TopLevel[] contents)
      throws Exception {
      for (int i = 0; i < contents.length; i++)
	contents[i].accept(this);
    }
    public void paramDef(String name, String value) throws Exception { }
    public void attributeDefaultDef(String name, AttributeDefault ad) throws Exception { }
  }


  class Analyzer extends VisitorBase implements ModelGroupVisitor,
                                                AttributeGroupVisitor {
    public void elementDecl(NameSpec nameSpec, ModelGroup modelGroup)
      throws Exception {
      noteElementName(nameSpec.getValue(), ELEMENT_DECL);
      modelGroup.accept(this);
    }

    public void attlistDecl(NameSpec nameSpec, AttributeGroup attributeGroup)
      throws Exception {
      noteElementName(nameSpec.getValue(), ATTLIST_DECL);
      attributeGroup.accept(this);
    }

    public void modelGroupDef(String name, ModelGroup modelGroup)
      throws Exception {
      noteDef(name);
      modelGroup.accept(this);
    }

    public void attributeGroupDef(String name, AttributeGroup attributeGroup)
      throws Exception {
      noteDef(name);
      attributeGroup.accept(this);
    }

    public void enumGroupDef(String name, EnumGroup enumGroup) {
      noteDef(name);
    }

    public void datatypeDef(String name, Datatype datatype) {
      noteDef(name);
    }

    public void choice(ModelGroup[] members) throws Exception {
      for (int i = 0; i < members.length; i++)
	members[i].accept(this);
    }

    public void sequence(ModelGroup[] members) throws Exception {
      for (int i = 0; i < members.length; i++)
	members[i].accept(this);
    }

    public void oneOrMore(ModelGroup member) throws Exception {
      member.accept(this);
    }

    public void zeroOrMore(ModelGroup member) throws Exception {
      member.accept(this);
    }

    public void optional(ModelGroup member) throws Exception {
      member.accept(this);
    }

    public void modelGroupRef(String name, ModelGroup modelGroup) {
    }

    public void elementRef(NameSpec name) {
      noteElementName(name.getValue(), ELEMENT_REF);
    }

    public void pcdata() {
    }

    public void any() {
      hadAny = true;
    }

    public void attribute(NameSpec nameSpec,
			  Datatype datatype,
			  AttributeDefault attributeDefault) {
      noteAttribute(nameSpec.getValue(), attributeDefault.getDefaultValue());
    }

    public void attributeGroupRef(String name, AttributeGroup attributeGroup) {
    }

  }

  public RelaxNgWriter(XmlWriter w) {
    this.w = w;
  }

  public void writeDtd(Dtd dtd) throws IOException {
    try {
      dtd.accept(new Analyzer());
    }
    catch (Exception e) {
      throw (RuntimeException)e;
    }
    chooseNames();
    if (colonReplacement != null)
      System.err.println("colonReplacement: " + colonReplacement);
    System.err.println("elementDecl: " + elementDeclPattern);
    System.err.println("attlistDecl: " + attlistDeclPattern);
  }

  void chooseNames() {
    chooseColonReplacement();
    chooseDeclPatterns();
  }

  static final String SEPARATORS = ".-_";

  void chooseColonReplacement() {
    if (colonReplacementOk())
      return;
    for (int n = 1;; n++) {
      for (int i = 0; i < SEPARATORS.length(); i++) {
	colonReplacement = repeatChar(SEPARATORS.charAt(i), n);
	if (colonReplacementOk())
	  return;
      }
    }
  }
  
  boolean colonReplacementOk() {
    Hashtable table = new Hashtable();
    for (Enumeration e = elementNameTable.keys();
	 e.hasMoreElements();) {
      String name = mungeQName((String)e.nextElement());
      if (table.get(name) != null)
	return false;
      table.put(name, name);
    }
    return true;
  }

  String[] ELEMENT_KEYWORDS = {
    "element", "elem", "e"
  };
  String[] ATTLIST_KEYWORDS = {
    "attlist", "attributes", "attribs", "atts", "a"
  };

  void chooseDeclPatterns() {
    // XXX Try to match length and case of best prefix
    String pattern = namingPattern();
    if (patternOk("%"))
      elementDeclPattern = "%";
    else
      elementDeclPattern = choosePattern(pattern, ELEMENT_KEYWORDS);
    attlistDeclPattern = choosePattern(pattern, ATTLIST_KEYWORDS);
  }

  String choosePattern(String metaPattern, String[] keywords) {
    for (;;) {
      for (int i = 0; i < keywords.length; i++) {
	String pattern = substitute(metaPattern, '#', keywords[i]);
	if (patternOk(pattern))
	  return pattern;
      }
      // add another separator
      metaPattern = (metaPattern.substring(0, 1)
		     + metaPattern.substring(1, 2)
		     + metaPattern.substring(1, 2)
		     + metaPattern.substring(2));
    }
  }

  String namingPattern() {
    Hashtable patternTable = new Hashtable();
    for (Enumeration e = defTable.keys();
	 e.hasMoreElements();) {
      String name = (String)e.nextElement();
      for (int i = 0; i < SEPARATORS.length(); i++) {
	char sep = SEPARATORS.charAt(i);
	int k = name.indexOf(sep);
	if (k > 0)
	  inc(patternTable, name.substring(0, k + 1) + "%");
	k = name.lastIndexOf(sep);
	if (k >= 0 && k < name.length() - 1)
	  inc(patternTable, "%" + name.substring(k));
      }
    }
    String bestPattern = null;
    int bestCount = 0;
    for (Enumeration e = patternTable.keys();
	 e.hasMoreElements();) {
      String pattern = (String)e.nextElement();
      int count = ((Integer)patternTable.get(pattern)).intValue();
      if (bestPattern == null || count > bestCount) {
	bestCount = count;
	bestPattern = pattern;
      }
    }
    if (bestPattern == null)
      return DEFAULT_PATTERN;
    if (bestPattern.charAt(0) == '%')
      return bestPattern.substring(0, 2) + "#";
    else
      return "#" + bestPattern.substring(bestPattern.length() - 2);
  }

  static void inc(Hashtable table, String str) {
    Integer n = (Integer)table.get(str);
    if (n == null)
      table.put(str, new Integer(1));
    else
      table.put(str, new Integer(n.intValue() + 1));
  }

  boolean patternOk(String pattern) {
    for (Enumeration e = elementNameTable.keys();
	 e.hasMoreElements();) {
      String name = mungeQName((String)e.nextElement());
      if (defTable.get(substitute(pattern, '%', name)) != null)
	return false;
    }
    return true;
  }

  void noteDef(String name) {
    defTable.put(name, name);
  }

  void noteElementName(String name, int flags) {
    Integer n = (Integer)elementNameTable.get(name);
    if (n != null) {
      flags |= n.intValue();
      if (n.intValue() == flags)
	return;
    }
    elementNameTable.put(name, new Integer(flags));
  }

  void noteAttribute(String name, String defaultValue) {
  }

  int nameFlags(String name) {
    Integer n = (Integer)elementNameTable.get(name);
    if (n == null)
      return 0;
    return n.intValue();
  }

  String elementDeclName(String name) {
    return substitute(elementDeclPattern, '%', mungeQName(name));
  }

  String attlistDeclName(String name) {
    return substitute(attlistDeclPattern, '%', mungeQName(name));
  }

  String mungeQName(String name) {
    if (colonReplacement == null)
      return name;
    return substitute(name, ':', colonReplacement);
  }

  static String repeatChar(char c, int n) {
    char[] buf = new char[n];
    for (int i = 0; i < n; i++)
      buf[i] = c;
    return new String(buf);
  }

  /* Replace the first occurrence of ch in pattern by value. */

  static String substitute(String pattern, char ch, String value) {
    int i = pattern.indexOf(ch);
    if (i < 0)
      return pattern;
    StringBuffer buf = new StringBuffer();
    buf.append(pattern.substring(0, i));
    buf.append(value);
    buf.append(pattern.substring(i + 1));
    return buf.toString();
  }

}
