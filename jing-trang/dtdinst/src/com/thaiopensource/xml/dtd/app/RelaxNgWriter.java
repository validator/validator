package com.thaiopensource.xml.dtd.app;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

import com.thaiopensource.xml.dtd.om.*;
import com.thaiopensource.xml.out.XmlWriter;
import com.thaiopensource.xml.em.ExternalId;
import com.thaiopensource.util.Localizer;

public class RelaxNgWriter {
  static Localizer localizer = new Localizer(RelaxNgWriter.class);
  private XmlOutputCollection outCollection;
  private XmlWriter w;
  private XmlOutputMember outMember;
  private boolean hadAny = false;
  private boolean hadDefaultValue = false;
  private Hashtable elementNameTable = new Hashtable();
  private Hashtable defTable = new Hashtable();
  private Hashtable prefixTable = new Hashtable();
  private ErrorMessageHandler errorMessageHandler = null;
  private String initialComment = null;

  private Hashtable duplicateAttributeTable = new Hashtable();
  private Hashtable currentDuplicateAttributeTable = null;
  private String defaultNamespace = null;
  private String annotationPrefix = null;

  private Output groupOutput = new GroupOutput();
  private Output choiceOutput = new ChoiceOutput();
  private Output explicitOutput = new Output();

  // These variables control the names use for definitions.
  private String colonReplacement = null;
  private String elementDeclPattern;
  private String attlistDeclPattern;
  private String anyName;

  private static final int ELEMENT_DECL = 01;
  private static final int ATTLIST_DECL = 02;
  private static final int ELEMENT_REF = 04;

  private static final String SEPARATORS = ".-_";

  private static final String COMPATIBILITY_ANNOTATIONS_URI
    = "http://relaxng.org/ns/compatibility/annotations/0.9";

  // # is the category; % is the name in the category

  private static final String DEFAULT_PATTERN = "#.%";

  private String[] ELEMENT_KEYWORDS = {
    "element", "elem", "e"
  };

  private String[] ATTLIST_KEYWORDS = {
    "attlist", "attributes", "attribs", "atts", "a"
  };

  private String[] ANY_KEYWORDS = {
    "any", "ANY", "anyElement"
  };

  private static abstract class VisitorBase implements TopLevelVisitor {
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
    public void externalIdRef(String name, ExternalId externalId,
			      String uri, String encoding, TopLevel[] contents)
      throws Exception {
      for (int i = 0; i < contents.length; i++)
	contents[i].accept(this);
    }
    public void paramDef(String name, String value) throws Exception { }
    public void attributeDefaultDef(String name, AttributeDefault ad) throws Exception { }
  }


  private class Analyzer extends VisitorBase implements ModelGroupVisitor,
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


  private class Output extends VisitorBase implements ModelGroupVisitor,
						      AttributeGroupVisitor,
						      DatatypeVisitor,
						      EnumGroupVisitor {
    public void elementDecl(NameSpec nameSpec, ModelGroup modelGroup)
      throws Exception {
      w.startElement("define");
      w.attribute("name", elementDeclName(nameSpec.getValue()));
      w.startElement("element");
      w.attribute("name", nameSpec.getValue());
      ref(attlistDeclName(nameSpec.getValue()));
      modelGroup.accept(groupOutput);
      w.endElement();
      w.endElement();
      if ((nameFlags(nameSpec.getValue()) & ATTLIST_DECL) == 0) {
	w.startElement("define");
	w.attribute("name", attlistDeclName(nameSpec.getValue()));
	w.attribute("combine", "interleave");
	w.startElement("empty");
	w.endElement();
	w.endElement();
      }
      if (anyName != null) {
	w.startElement("define");
	w.attribute("name", anyName);
	w.attribute("combine", "choice");
	ref(elementDeclName(nameSpec.getValue()));
	w.endElement();
      }
    }

    public void attlistDecl(NameSpec nameSpec, AttributeGroup attributeGroup)
      throws Exception {
      String name = nameSpec.getValue();
      currentDuplicateAttributeTable
	= (Hashtable)duplicateAttributeTable.get(name);
      if (currentDuplicateAttributeTable == null) {
	currentDuplicateAttributeTable = new Hashtable();
	duplicateAttributeTable.put(name, currentDuplicateAttributeTable);
      }
      w.startElement("define");
      w.attribute("name", attlistDeclName(name));
      w.attribute("combine", "interleave");
      attributeGroup.accept(this);
      w.endElement();
    }

    public void modelGroupDef(String name, ModelGroup modelGroup)
      throws Exception {
      w.startElement("define");
      w.attribute("name", name);
      modelGroup.accept(groupOutput);
      w.endElement();
    }

    public void attributeGroupDef(String name, AttributeGroup attributeGroup)
      throws Exception {
      // This takes care of duplicates within the group
      currentDuplicateAttributeTable = new Hashtable();
      w.startElement("define");
      w.attribute("name", name);
      AttributeGroupMember[] members = attributeGroup.getMembers();
      if (members.length == 0) {
	w.startElement("empty");
	w.endElement();
      }
      else {
	for (int i = 0; i < members.length; i++)
	  members[i].accept(this);
      }
      w.endElement();
    }

    public void enumGroupDef(String name, EnumGroup enumGroup) throws Exception {
      w.startElement("define");
      w.attribute("name", name);
      enumDatatype(enumGroup);
      w.endElement();
    }

    public void datatypeDef(String name, Datatype datatype) throws Exception {
      w.startElement("define");
      w.attribute("name", name);
      datatype.accept(this);
      w.endElement();
    }

    public void choice(ModelGroup[] members) throws Exception {
      if (members.length == 0) {
	w.startElement("notAllowed");
	w.endElement();
      }
      else if (members.length == 1)
	members[0].accept(this);
      else {
	w.startElement("choice");
	for (int i = 0; i < members.length; i++)
	  members[i].accept(choiceOutput);
	w.endElement();
      }
    }

    public void sequence(ModelGroup[] members) throws Exception {
      if (members.length == 0) {
	w.startElement("empty");
	w.endElement();
      }
      else if (members.length == 1)
	members[0].accept(this);
      else {
	w.startElement("group");
	for (int i = 0; i < members.length; i++)
	  members[i].accept(groupOutput);
	w.endElement();
      }
    }

    public void oneOrMore(ModelGroup member) throws Exception {
      w.startElement("oneOrMore");
      member.accept(groupOutput);
      w.endElement();
    }

    public void zeroOrMore(ModelGroup member) throws Exception {
      w.startElement("zeroOrMore");
      member.accept(groupOutput);
      w.endElement();
    }

    public void optional(ModelGroup member) throws Exception {
      w.startElement("optional");
      member.accept(groupOutput);
      w.endElement();
    }

    public void modelGroupRef(String name, ModelGroup modelGroup)
      throws IOException {
      ref(name);
    }

    public void elementRef(NameSpec name) throws IOException {
      ref(elementDeclName(name.getValue()));
    }

    public void pcdata() throws IOException {
      w.startElement("text");
      w.endElement();
    }

    public void any() throws IOException {
      ref(anyName);
    }

    public void attribute(NameSpec nameSpec,
			  Datatype datatype,
			  AttributeDefault attributeDefault) throws Exception {
      String name = nameSpec.getValue();
      if (currentDuplicateAttributeTable.get(name) != null)
	return;
      currentDuplicateAttributeTable.put(name, name);
      if (name.equals("xmlns") || name.startsWith("xmlns:")) {
	w.startElement("empty");
	w.endElement();
	return;
      }
      if (!attributeDefault.isRequired())
	w.startElement("optional");
      w.startElement("attribute");
      w.attribute("name", name);
      String dv = attributeDefault.getDefaultValue();
      if (dv != null)
	w.attribute(annotationPrefix + ":defaultValue", dv);
      String fv = attributeDefault.getFixedValue();
      if (fv != null) {
	w.startElement("value");
	String typeName = valueType(datatype);
	if (typeName != null)
	  w.attribute("type", typeName);
	w.characters(fv);
	w.endElement();
      }
      else if (datatype.getType() != Datatype.CDATA)
	datatype.accept(explicitOutput);
      w.endElement();
      if (!attributeDefault.isRequired())
	w.endElement();
    }

    public void attributeGroupRef(String name, AttributeGroup attributeGroup)
      throws Exception {
      DuplicateAttributeDetector detector = new DuplicateAttributeDetector();
      attributeGroup.accept(detector);
      if (detector.containsDuplicate)
	attributeGroup.accept(this);
      else
	ref(name);
    }

    public void cdataDatatype() throws IOException {
      w.startElement("data");
      w.attribute("type", "string");
      w.endElement();
    }

    public void tokenizedDatatype(String typeName) throws IOException {
      w.startElement("data");
      w.attribute("type", typeName);
      w.endElement();
    }
    
    public void enumDatatype(EnumGroup enumGroup) throws Exception {
      if (enumGroup.getMembers().length == 0) {
	w.startElement("notAllowed");
	w.endElement();
      }
      else {
	w.startElement("choice");
	enumGroup.accept(this);
	w.endElement();
      }
    }

    public void notationDatatype(EnumGroup enumGroup) throws Exception {
      enumDatatype(enumGroup);
    }

    public void datatypeRef(String name, Datatype datatype) throws IOException {
      ref(name);
    }

    public void enumValue(String value) throws IOException {
      w.startElement("value");
      w.characters(value);
      w.endElement();
    }

    public void enumGroupRef(String name, EnumGroup enumGroup)
      throws IOException {
      ref(name);
    }

    public void comment(String value) throws IOException {
      w.comment(value);
    }

    public void processingInstruction(String target, String value) throws IOException {
      w.processingInstruction(target, value);
    }

    public void externalIdRef(String name, ExternalId externalId,
			      String uri, String encoding, TopLevel[] contents)
      throws Exception {
      if (uri == null) {
	super.externalIdRef(name, externalId, uri, encoding, contents);
	return;
      }
      SignificanceDetector sd = new SignificanceDetector();
      try {
	sd.externalIdRef(name, externalId, uri, encoding, contents);
	if (!sd.significant)
	  return;
      }
      catch (Exception e) {
	throw (RuntimeException)e;
      }
      XmlOutputMember outMemberSave = outMember;
      XmlWriter wSave = w;
      outMember = outCollection.mapUri(uri);
      w = outMember.open(encoding);
      String systemId = outMember.getSystemId(outMemberSave);
      w.writeXmlDecl(encoding);
      startGrammar();
      super.externalIdRef(name, externalId, uri, encoding, contents);
      endGrammar();
      w.close();
      w = wSave;
      outMember = outMemberSave;
      w.startElement("include");
      w.attribute("href", systemId);
      w.endElement();
    }

  }

  private class GroupOutput extends Output {
    public void sequence(ModelGroup[] members) throws Exception {
      if (members.length == 0)
	super.sequence(members);
      else {
	for (int i = 0; i < members.length; i++)
	  members[i].accept(this);
      }
    }
  }

  private class ChoiceOutput extends Output {
    public void choice(ModelGroup[] members) throws Exception {
      if (members.length == 0)
	super.choice(members);
      else {
	for (int i = 0; i < members.length; i++)
	  members[i].accept(this);
      }
    }
  }

  private class DuplicateAttributeDetector implements AttributeGroupVisitor {
    private boolean containsDuplicate = false;
    
    public void attribute(NameSpec nameSpec,
			  Datatype datatype,
			  AttributeDefault attributeDefault) {
      if (currentDuplicateAttributeTable.get(nameSpec.getValue()) != null)
	containsDuplicate = true;
    }

    public void attributeGroupRef(String name, AttributeGroup attributeGroup) throws Exception {
      attributeGroup.accept(this);
    }

  }

  private class SignificanceDetector extends VisitorBase {
    boolean significant = false;
    public void elementDecl(NameSpec nameSpec, ModelGroup modelGroup)
      throws Exception {
      significant = true;
    }

    public void attlistDecl(NameSpec nameSpec, AttributeGroup attributeGroup)
      throws Exception {
      significant = true;
    }

    public void modelGroupDef(String name, ModelGroup modelGroup)
      throws Exception {
      significant = true;
    }

    public void attributeGroupDef(String name, AttributeGroup attributeGroup)
      throws Exception {
      significant = true;
    }

    public void enumGroupDef(String name, EnumGroup enumGroup) {
      significant = true;
    }

    public void datatypeDef(String name, Datatype datatype) {
      significant = true;
    }

  }

  public RelaxNgWriter(XmlOutputCollection outCollection) {
    this.outCollection = outCollection;
  }

  public void setErrorMessageHandler(ErrorMessageHandler handler) {
    errorMessageHandler = handler;
  }

  public void setInitialComment(String str) {
    initialComment = str;
  }

  public void writeDtd(Dtd dtd) throws IOException {
    try {
      dtd.accept(new Analyzer());
    }
    catch (Exception e) {
      throw (RuntimeException)e;
    }
    chooseNames();
    outMember = outCollection.getMain();
    w = outMember.open(dtd.getEncoding());
    w.writeXmlDecl(dtd.getEncoding());
    if (initialComment != null)
      w.comment(initialComment);
    startGrammar();
    try {
      dtd.accept(explicitOutput);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw (IOException)e;
    }
    outputUndefinedElements();
    outputStart();
    endGrammar();
    w.close();
  }

  private void chooseNames() {
    chooseAny();
    chooseColonReplacement();
    chooseDeclPatterns();
    chooseAnnotationPrefix();
  }

  private void chooseAny() {
    if (!hadAny)
      return;
    for (int n = 0;; n++) {
      for (int i = 0; i < ANY_KEYWORDS.length; i++) {
	anyName = repeatChar('_', n) + ANY_KEYWORDS[i];
	if (defTable.get(anyName) == null) {
	  defTable.put(anyName, anyName);
	  return;
	}
      }
    }
  }

  private void chooseAnnotationPrefix() {
    if (!hadDefaultValue)
      return;
    for (int n = 0;; n++) {
      annotationPrefix = repeatChar('_', n) + "a";
      if (prefixTable.get(annotationPrefix) == null)
	return;
    }
  }

  private void chooseColonReplacement() {
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
  
  private boolean colonReplacementOk() {
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

  private void chooseDeclPatterns() {
    // XXX Try to match length and case of best prefix
    String pattern = namingPattern();
    if (patternOk("%"))
      elementDeclPattern = "%";
    else
      elementDeclPattern = choosePattern(pattern, ELEMENT_KEYWORDS);
    attlistDeclPattern = choosePattern(pattern, ATTLIST_KEYWORDS);
  }

  private String choosePattern(String metaPattern, String[] keywords) {
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

  private String namingPattern() {
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

  private static void inc(Hashtable table, String str) {
    Integer n = (Integer)table.get(str);
    if (n == null)
      table.put(str, new Integer(1));
    else
      table.put(str, new Integer(n.intValue() + 1));
  }

  private boolean patternOk(String pattern) {
    for (Enumeration e = elementNameTable.keys();
	 e.hasMoreElements();) {
      String name = mungeQName((String)e.nextElement());
      if (defTable.get(substitute(pattern, '%', name)) != null)
	return false;
    }
    return true;
  }

  private void noteDef(String name) {
    defTable.put(name, name);
  }

  private void noteElementName(String name, int flags) {
    Integer n = (Integer)elementNameTable.get(name);
    if (n != null) {
      flags |= n.intValue();
      if (n.intValue() == flags)
	return;
    }
    else
      noteNamePrefix(name);
    elementNameTable.put(name, new Integer(flags));
  }

  private void noteAttribute(String name, String defaultValue) {
    if (name.equals("xmlns")) {
      if (defaultValue != null) {
	if (defaultNamespace != null
	    && !defaultNamespace.equals(defaultValue))
	  error("INCONSISTENT_DEFAULT_NAMESPACE");
	else
	  defaultNamespace = defaultValue;
      }
    }
    else if (name.startsWith("xmlns:")) {
      if (defaultValue != null) {
	String prefix = name.substring(6);
	String ns = (String)prefixTable.get(prefix);
	if (ns != null
	    && !ns.equals("")
	    && !ns.equals(defaultValue))
	  error("INCONSISTENT_PREFIX", prefix);
	else if (!prefix.equals("xml"))
	  prefixTable.put(prefix, defaultValue);
      }
    }
    else {
      if (defaultValue != null)
	hadDefaultValue = true;
      noteNamePrefix(name);
    }
  }

  private void noteNamePrefix(String name) {
    int i = name.indexOf(':');
    if (i < 0)
      return;
    String prefix = name.substring(0, i);
    if (prefixTable.get(prefix) == null && !prefix.equals("xml"))
      prefixTable.put(prefix, "");
  }

  private int nameFlags(String name) {
    Integer n = (Integer)elementNameTable.get(name);
    if (n == null)
      return 0;
    return n.intValue();
  }

  private String elementDeclName(String name) {
    return substitute(elementDeclPattern, '%', mungeQName(name));
  }

  private String attlistDeclName(String name) {
    return substitute(attlistDeclPattern, '%', mungeQName(name));
  }

  private String mungeQName(String name) {
    if (colonReplacement == null) {
      int i = name.indexOf(':');
      if (i < 0)
	return name;
      return name.substring(i + 1);
    }
    return substitute(name, ':', colonReplacement);
  }

  private static String repeatChar(char c, int n) {
    char[] buf = new char[n];
    for (int i = 0; i < n; i++)
      buf[i] = c;
    return new String(buf);
  }

  /* Replace the first occurrence of ch in pattern by value. */

  private static String substitute(String pattern, char ch, String value) {
    int i = pattern.indexOf(ch);
    if (i < 0)
      return pattern;
    StringBuffer buf = new StringBuffer();
    buf.append(pattern.substring(0, i));
    buf.append(value);
    buf.append(pattern.substring(i + 1));
    return buf.toString();
  }

  private void outputNamespaces() throws IOException {
    for (Enumeration e = prefixTable.keys();
	 e.hasMoreElements();) {
      String prefix = (String)e.nextElement();
      String ns = (String)prefixTable.get(prefix);
      if (ns.length() != 0)
	w.attribute("xmlns:" + prefix, ns);
      else
	error("UNDECLARED_PREFIX", prefix);
    }
    if (defaultNamespace != null)
      w.attribute("ns", defaultNamespace);
  }

  private void outputStart() throws IOException {
    w.startElement("start");
    w.startElement("choice");
    // Use the defined but unreferenced elements.
    // If there aren't any, use all defined elements.
    int mask = ELEMENT_REF|ELEMENT_DECL;
    for (;;) {
      boolean gotOne = false;
      for (Enumeration e = elementNameTable.keys();
	   e.hasMoreElements();) {
	String name = (String)e.nextElement();
	if ((((Integer)elementNameTable.get(name)).intValue() & mask)
	    == ELEMENT_DECL) {
	  gotOne = true;
	  ref(elementDeclName(name));
	}
      }
      if (gotOne)
	break;
      if (mask == ELEMENT_DECL)
	break;
      mask = ELEMENT_DECL;
    }
    w.endElement();
    w.endElement();
    if (anyName != null) {
      w.startElement("define");
      w.attribute("name", anyName);
      w.attribute("combine", "choice");
      w.startElement("text");
      w.endElement();
      w.endElement();
    }
  }

  
  private void outputUndefinedElements() throws IOException {
    for (Enumeration e = elementNameTable.keys();
	 e.hasMoreElements();) {
      String name = (String)e.nextElement();
      if ((((Integer)elementNameTable.get(name)).intValue() & ELEMENT_DECL)
	  == 0) {
	w.startElement("define");
	w.attribute("name", elementDeclName(name));
	w.attribute("combine", "choice");
	w.startElement("notAllowed");
	w.endElement();
	w.endElement();
      }
    }
  }

  private void ref(String name) throws IOException {
    w.startElement("ref");
    w.attribute("name", name);
    w.endElement();
  }

  private void startGrammar() throws IOException {
    w.startElement("grammar");
    w.attribute("datatypeLibrary",
		"http://www.w3.org/2001/XMLSchema-datatypes");
    w.attribute("xmlns",
		"http://relaxng.org/ns/structure/0.9");
    if (annotationPrefix != null)
      w.attribute("xmlns:" + annotationPrefix,
		  COMPATIBILITY_ANNOTATIONS_URI);
    outputNamespaces();
  }
  
  private void endGrammar() throws IOException {
    w.endElement();
  }

  private void error(String key) {
    reportError(localizer.message(key));
  }

  private void error(String key, String arg) {
    reportError(localizer.message(key, arg));
  }

  private void warning(String key) {
    reportWarning(localizer.message(key));
  }

  private void warning(String key, String arg) {
    reportWarning(localizer.message(key, arg));
  }

  private void reportError(String message) {
    report(new ErrorMessage(ErrorMessage.ERROR, message));
  }

  private void reportWarning(String message) {
    report(new ErrorMessage(ErrorMessage.WARNING, message));
  }

  private void report(ErrorMessage em) {
    if (errorMessageHandler != null)
      errorMessageHandler.message(em);
  }

  private static String valueType(Datatype datatype) {
    datatype = datatype.deref();
    switch (datatype.getType()) {
    case Datatype.CDATA:
      return "string";
    case Datatype.TOKENIZED:
      return ((TokenizedDatatype)datatype).getTypeName();
    }
    return null;
  }
}
