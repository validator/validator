package com.thaiopensource.relaxng.input.dtd;

import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Combine;
import com.thaiopensource.relaxng.edit.Comment;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.input.CommentTrimmer;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.xml.dtd.om.AttributeDefault;
import com.thaiopensource.xml.dtd.om.AttributeGroup;
import com.thaiopensource.xml.dtd.om.AttributeGroupMember;
import com.thaiopensource.xml.dtd.om.AttributeGroupVisitor;
import com.thaiopensource.xml.dtd.om.Datatype;
import com.thaiopensource.xml.dtd.om.DatatypeVisitor;
import com.thaiopensource.xml.dtd.om.Def;
import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.om.EnumGroup;
import com.thaiopensource.xml.dtd.om.EnumGroupVisitor;
import com.thaiopensource.xml.dtd.om.Flag;
import com.thaiopensource.xml.dtd.om.ModelGroup;
import com.thaiopensource.xml.dtd.om.ModelGroupVisitor;
import com.thaiopensource.xml.dtd.om.NameSpec;
import com.thaiopensource.xml.dtd.om.TokenizedDatatype;
import com.thaiopensource.xml.dtd.om.TopLevel;
import com.thaiopensource.xml.dtd.om.TopLevelVisitor;
import com.thaiopensource.xml.em.ExternalId;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Converter {
  static class Options {
    boolean inlineAttlistDecls;
    boolean generateStart = true;
    boolean strictAny;
    String elementDeclPattern;
    String attlistDeclPattern;
    String colonReplacement;
    String anyName;
    String annotationPrefix;
    String defaultNamespace;
    final Map prefixMap = new HashMap();
  }

  private final Dtd dtd;
  private final ErrorReporter er;
  private final SchemaCollection sc = new SchemaCollection();
  private final Options options;
  /**
   * true if any uses of ANY have been encountered in the DTD
   */
  private boolean hadAny = false;
  /**
   * true if any default values have been encountered in the DTD
   */
  private boolean hadDefaultValue = false;
  /**
   * Maps each element name to an Integer containing a set of flags.
   */
  private final Map elementNameTable = new Hashtable();
  /**
   * Maps each element name to a List of attribute groups of each attlist declaration.
   */
  private final Map attlistDeclTable = new Hashtable();
  /**
   * Set of strings representing names for which there are definitions in the DTD.
   */
  private final Set definedNames = new HashSet();
  /**
   * Maps prefixes to namespace URIs.
   */
  private final Map prefixTable = new Hashtable();

  /**
   * Maps a string representing an element name to the set of names of attributes
   * that have been declated for that element.
   */
  private final Map attributeNamesTable = new Hashtable();
  /**
   * Contains the set of attribute names that have already been output in the current scope.
   */
  private Set attributeNames = null;
  private String defaultNamespace = null;
  private String annotationPrefix = null;

  // These variables control the names use for definitions.
  private String colonReplacement = null;
  private String elementDeclPattern = null;
  private String attlistDeclPattern = null;
  private String anyName = null;

  /**
   * Flags for element names used in elementDeclTable.
   */
  private static final int ELEMENT_DECL = 01;
  private static final int ATTLIST_DECL = 02;
  private static final int ELEMENT_REF = 04;

  /**
   * Characters that will be considered for use as a replacement for colon in
   * a QName.  Also used as separators in constructing names of definitions
   * corresponding to element declarations and attlist declarations,
   */
  private static final String SEPARATORS = ".-_";

  // # is the category; % is the name in the category

  private static final String DEFAULT_PATTERN = "#.%";

  private final String[] ELEMENT_KEYWORDS = {
    "element", "elem", "e"
  };

  private final String[] ATTLIST_KEYWORDS = {
    "attlist", "attributes", "attribs", "atts", "a"
  };

  private final String[] ANY_KEYWORDS = {
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
      noteAttlist(nameSpec.getValue(), attributeGroup);
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


  private class ComponentOutput extends VisitorBase {
    private final List components;
    private final Annotated grammar;
    private List comments = null;

    ComponentOutput(GrammarPattern grammar) {
      components = grammar.getComponents();
      this.grammar = grammar;
    }

    void finish() {
      if (comments != null)
        grammar.getFollowingElementAnnotations().addAll(comments);
    }

    private void addComponent(Component c) {
      if (comments != null) {
        if (components.isEmpty())
          grammar.getLeadingComments().addAll(comments);
        else
          c.getLeadingComments().addAll(comments);
        comments = null;
      }
      components.add(c);
    }

    public void elementDecl(NameSpec nameSpec, ModelGroup modelGroup) throws Exception {
      GroupPattern gp = new GroupPattern();
      if (options.inlineAttlistDecls) {
        List groups = (List)attlistDeclTable.get(nameSpec.getValue());
        if (groups != null) {
          attributeNames = new HashSet();
          AttributeGroupVisitor agv = new AttributeGroupOutput(gp);
          for (Iterator iter = groups.iterator(); iter.hasNext();)
            ((AttributeGroup)iter.next()).accept(agv);
        }
      }
      else
        gp.getChildren().add(ref(attlistDeclName(nameSpec.getValue())));
      Pattern pattern = convert(modelGroup);
      if (gp.getChildren().size() > 0) {
        if (pattern instanceof GroupPattern)
          gp.getChildren().addAll(((GroupPattern)pattern).getChildren());
        else
          gp.getChildren().add(pattern);
        pattern = gp;
      }
      addComponent(new DefineComponent(elementDeclName(nameSpec.getValue()),
                                       new ElementPattern(convertQName(nameSpec.getValue(), true),
                                                          pattern)));
      if (!options.inlineAttlistDecls && (nameFlags(nameSpec.getValue()) & ATTLIST_DECL) == 0) {
        DefineComponent dc = new DefineComponent(attlistDeclName(nameSpec.getValue()), new EmptyPattern());
        dc.setCombine(Combine.INTERLEAVE);
        addComponent(dc);
      }
      if (anyName != null && options.strictAny) {
        DefineComponent dc = new DefineComponent(anyName, ref(elementDeclName(nameSpec.getValue())));
        dc.setCombine(Combine.CHOICE);
        addComponent(dc);
      }
    }

    public void attlistDecl(NameSpec nameSpec, AttributeGroup attributeGroup) throws Exception {
      if (options.inlineAttlistDecls)
        return;
      String name = nameSpec.getValue();
      attributeNames
	= (Set)attributeNamesTable.get(name);
      if (attributeNames == null) {
	attributeNames = new HashSet();
	attributeNamesTable.put(name, attributeNames);
      }
      Pattern pattern = convert(attributeGroup);
      if (pattern instanceof EmptyPattern) {
        // Only keep an empty definition if this is the first attlist for this element,
        // and all attlists are also empty.  In this case, if we didn't keep the
        // definition, we would have no definition for the attlist.
        List decls = (List)attlistDeclTable.get(name);
        if (decls.get(0) != attributeGroup)
          return;
        attributeNames = new HashSet();
        for (int i = 1, len = decls.size(); i < len; i++)
          if (!(convert((AttributeGroup)decls.get(i)) instanceof EmptyPattern))
            return;
      }
      DefineComponent dc = new DefineComponent(attlistDeclName(name), pattern);
      dc.setCombine(Combine.INTERLEAVE);
      addComponent(dc);
    }

    public void modelGroupDef(String name, ModelGroup modelGroup)
      throws Exception {
      addComponent(new DefineComponent(name, convert(modelGroup)));
    }

    public void attributeGroupDef(String name, AttributeGroup attributeGroup)
            throws Exception {
      // This takes care of duplicates within the group
      attributeNames = new HashSet();
      Pattern pattern;
      AttributeGroupMember[] members = attributeGroup.getMembers();
      GroupPattern group = new GroupPattern();
      AttributeGroupVisitor agv = new AttributeGroupOutput(group);
      for (int i = 0; i < members.length; i++)
        members[i].accept(agv);
      switch (group.getChildren().size()) {
      case 0:
        pattern = new EmptyPattern();
        break;
      case 1:
        pattern = (Pattern)group.getChildren().get(0);
        break;
      default:
        pattern = group;
        break;
      }
      addComponent(new DefineComponent(name, pattern));
    }

    public void enumGroupDef(String name, EnumGroup enumGroup) throws Exception {
      ChoicePattern choice = new ChoicePattern();
      enumGroup.accept(new EnumGroupOutput(choice));
      Pattern pattern;
      switch (choice.getChildren().size()) {
      case 0:
        pattern = new NotAllowedPattern();
        break;
      case 1:
        pattern = (Pattern)choice.getChildren().get(0);
        break;
      default:
        pattern = choice;
        break;
      }
      addComponent(new DefineComponent(name, pattern));
    }

    public void datatypeDef(String name, Datatype datatype) throws Exception {
      addComponent(new DefineComponent(name, convert(datatype)));
    }

    public void comment(String value) {
      if (comments == null)
        comments = new Vector();
      comments.add(new Comment(CommentTrimmer.trimComment(value)));
    }

    public void externalIdRef(String name, ExternalId externalId,
			      String uri, String encoding, TopLevel[] contents)
      throws Exception {
      if (uri == null) {
        // I don't think this can happen
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
      if (sc.getSchemaDocumentMap().get(uri) != null) {
        // I don't think this can happen because the second and subsequent inclusions
        // will never pass the SignificanceDetector, but just in case
        super.externalIdRef(name, externalId, uri, encoding, contents);
        return;
      }
      IncludeComponent ic = new IncludeComponent(uri);
      ic.setNs(defaultNamespace);
      addComponent(ic);
      GrammarPattern included = new GrammarPattern();
      ComponentOutput co = new ComponentOutput(included);
      for (int i = 0; i < contents.length; i++)
        contents[i].accept(co);
      co.finish();
      sc.getSchemaDocumentMap().put(uri, new SchemaDocument(included, encoding));
    }

  }

  private class AttributeGroupOutput implements AttributeGroupVisitor {
    final List group;

    AttributeGroupOutput(GroupPattern gp) {
      group = gp.getChildren();
    }

    public void attribute(NameSpec nameSpec,
                          Datatype datatype,
                          AttributeDefault attributeDefault) throws Exception {
      String name = nameSpec.getValue();
      if (attributeNames.contains(name))
        return;
      attributeNames.add(name);
      if (name.equals("xmlns") || name.startsWith("xmlns:"))
        return;
      String dv = attributeDefault.getDefaultValue();
      String fv = attributeDefault.getFixedValue();
      Pattern dt;
      if (fv != null) {
        String[] typeName = valueType(datatype);
        dt = new ValuePattern(typeName[0], typeName[1], fv);
      }
      else if (datatype.getType() != Datatype.CDATA)
        dt = convert(datatype);
      else
        dt = new TextPattern();
      AttributePattern pattern = new AttributePattern(convertQName(name, false), dt);
      if (dv != null) {
        AttributeAnnotation anno = new AttributeAnnotation(WellKnownNamespaces.RELAX_NG_COMPATIBILITY_ANNOTATIONS, "defaultValue", dv);
        anno.setPrefix(annotationPrefix);
        pattern.getAttributeAnnotations().add(anno);
      }
      if (!attributeDefault.isRequired())
        group.add(new OptionalPattern(pattern));
      else
        group.add(pattern);
    }

    public void attributeGroupRef(String name, AttributeGroup attributeGroup)
            throws Exception {
      DuplicateAttributeDetector detector = new DuplicateAttributeDetector();
      attributeGroup.accept(detector);
      if (detector.containsDuplicate)
        attributeGroup.accept(this);
      else {
        group.add(ref(name));
        attributeNames.addAll(detector.names);
      }
    }


   }

  private class DatatypeOutput implements DatatypeVisitor {
    Pattern pattern;

    public void cdataDatatype() {
      pattern = new DataPattern("", "string");
    }

    public void tokenizedDatatype(String typeName) {
      pattern = new DataPattern(WellKnownNamespaces.XML_SCHEMA_DATATYPES, typeName);
    }

    public void enumDatatype(EnumGroup enumGroup) throws Exception {
      if (enumGroup.getMembers().length == 0)
        pattern = new NotAllowedPattern();
      else {
        ChoicePattern tem = new ChoicePattern();
        pattern = tem;
        enumGroup.accept(new EnumGroupOutput(tem));
      }
    }

    public void notationDatatype(EnumGroup enumGroup) throws Exception {
      enumDatatype(enumGroup);
    }

    public void datatypeRef(String name, Datatype datatype) {
      pattern = ref(name);
    }
  }

  private class EnumGroupOutput implements EnumGroupVisitor {
    final private List list;

    EnumGroupOutput(ChoicePattern choice) {
      list = choice.getChildren();
    }

    public void enumValue(String value) {
      list.add(new ValuePattern("", "token", value));
     }

     public void enumGroupRef(String name, EnumGroup enumGroup) {
       list.add(ref(name));
     }
  }

  private class ModelGroupOutput implements ModelGroupVisitor {
    private Pattern pattern;

    public void choice(ModelGroup[] members) throws Exception {
      if (members.length == 0)
        pattern = new NotAllowedPattern();
      else if (members.length == 1)
	members[0].accept(this);
      else {
        ChoicePattern tem = new ChoicePattern();
        pattern = tem;
        List children = tem.getChildren();
	for (int i = 0; i < members.length; i++)
          children.add(convert(members[i]));
      }
    }

    public void sequence(ModelGroup[] members) throws Exception {
      if (members.length == 0)
        pattern = new EmptyPattern();
      else if (members.length == 1)
	members[0].accept(this);
      else {
        GroupPattern tem = new GroupPattern();
        pattern = tem;
        List children = tem.getChildren();
	for (int i = 0; i < members.length; i++)
	  children.add(convert(members[i]));
      }
    }

    public void oneOrMore(ModelGroup member) throws Exception {
      pattern = new OneOrMorePattern(convert(member));
    }

    public void zeroOrMore(ModelGroup member) throws Exception {
      pattern = new ZeroOrMorePattern(convert(member));
    }

    public void optional(ModelGroup member) throws Exception {
      pattern = new OptionalPattern(convert(member));
    }

    public void modelGroupRef(String name, ModelGroup modelGroup) {
      pattern = ref(name);
    }

    public void elementRef(NameSpec name) {
      pattern = ref(elementDeclName(name.getValue()));
    }

    public void pcdata() {
      pattern = new TextPattern();
    }

    public void any() {
      pattern = ref(anyName);
      if (options.strictAny)
        pattern = new ZeroOrMorePattern(pattern);
    }

  }


  private class DuplicateAttributeDetector implements AttributeGroupVisitor {
    private boolean containsDuplicate = false;
    private final List names = new Vector();

    public void attribute(NameSpec nameSpec,
			  Datatype datatype,
			  AttributeDefault attributeDefault) {
      String name = nameSpec.getValue();
      if (attributeNames.contains(name))
	containsDuplicate = true;
      names.add(name);
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

  public Converter(Dtd dtd, ErrorReporter er, Options options) {
    this.dtd = dtd;
    this.er = er;
    this.options = options;
  }

  public SchemaCollection convert() {
    try {
      dtd.accept(new Analyzer());
      chooseNames();
      GrammarPattern grammar = new GrammarPattern();
      sc.setMainUri(dtd.getUri());
      sc.getSchemaDocumentMap().put(dtd.getUri(),
                                    new SchemaDocument(grammar, dtd.getEncoding()));
      ComponentOutput co = new ComponentOutput(grammar);
      dtd.accept(co);
      outputUndefinedElements(grammar.getComponents());
      if (options.generateStart)
        outputStart(grammar.getComponents());
      outputAny(grammar.getComponents());
      co.finish();
      return sc;
    }
    catch (Exception e) {
      throw (RuntimeException)e;
    }
  }

  private void chooseNames() {
    chooseAny();
    chooseColonReplacement();
    chooseDeclPatterns();
    choosePrefixes();
    chooseAnnotationPrefix();
  }

  private void chooseAny() {
    if (!hadAny)
      return;
    if (options.anyName != null) {
      if (!definedNames.contains(options.anyName)) {
        anyName = options.anyName;
        definedNames.add(anyName);
        return;
      }
      warning("cannot_use_any_name");
    }
    for (int n = 0;; n++) {
      for (int i = 0; i < ANY_KEYWORDS.length; i++) {
	anyName = repeatChar('_', n) + ANY_KEYWORDS[i];
	if (!definedNames.contains(anyName)) {
	  definedNames.add(anyName);
	  return;
	}
      }
    }
  }

  private void choosePrefixes() {
    if (options.defaultNamespace != null) {
      if (defaultNamespace != null && !defaultNamespace.equals(options.defaultNamespace))
        warning("default_namespace_conflict");
      defaultNamespace = options.defaultNamespace;
    }
    else if (defaultNamespace == null)
      defaultNamespace = SchemaBuilder.INHERIT_NS;
    for (Iterator iter = options.prefixMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      String prefix = (String)entry.getKey();
      String ns = (String)entry.getValue();
      String s = (String)prefixTable.get(prefix);
      if (s == null)
        warning("irrelevant_prefix", prefix);
      else {
        if (!s.equals("") && !s.equals(ns))
          warning("prefix_conflict", prefix);
        prefixTable.put(prefix, ns);
      }
    }
  }

  private void chooseAnnotationPrefix() {
    if (!hadDefaultValue)
      return;
    if (options.annotationPrefix != null) {
      if (prefixTable.get(options.annotationPrefix) == null) {
        annotationPrefix = options.annotationPrefix;
        return;
      }
      warning("cannot_use_annotation_prefix");
    }
    for (int n = 0;; n++) {
      annotationPrefix = repeatChar('_', n) + "a";
      if (prefixTable.get(annotationPrefix) == null)
	return;
    }
  }

  private void chooseColonReplacement() {
    if (options.colonReplacement != null) {
      colonReplacement = options.colonReplacement;
      if (colonReplacementOk())
        return;
      warning("cannot_use_colon_replacement");
      colonReplacement = null;
    }
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
    Set names = new HashSet();
    for (Iterator iter = elementNameTable.keySet().iterator(); iter.hasNext();) {
      String name = mungeQName((String)iter.next());
      if (names.contains(name))
	return false;
      names.add(name);
    }
    return true;
  }

  private void chooseDeclPatterns() {
    if (options.elementDeclPattern != null) {
      if (patternOk(options.elementDeclPattern, null))
        elementDeclPattern = options.elementDeclPattern;
      else
        warning("cannot_use_element_decl_pattern");
    }
    if (options.attlistDeclPattern != null) {
      if (patternOk(options.attlistDeclPattern, elementDeclPattern))
        attlistDeclPattern = options.attlistDeclPattern;
      else
        warning("cannot_use_attlist_decl_pattern");
    }
    if (elementDeclPattern != null && attlistDeclPattern != null)
      return;
    // XXX Try to match length and case of best prefix
    String pattern = namingPattern();
    if (elementDeclPattern == null) {
      if (patternOk("%", attlistDeclPattern))
        elementDeclPattern = "%";
      else
        elementDeclPattern = choosePattern(pattern, ELEMENT_KEYWORDS, attlistDeclPattern);
    }
    if (attlistDeclPattern == null)
      attlistDeclPattern = choosePattern(pattern, ATTLIST_KEYWORDS, elementDeclPattern);
  }

  private String choosePattern(String metaPattern, String[] keywords, String otherPattern) {
    for (;;) {
      for (int i = 0; i < keywords.length; i++) {
	String pattern = substitute(metaPattern, '#', keywords[i]);
	if (patternOk(pattern, otherPattern))
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
    Map patternTable = new Hashtable();
    for (Iterator iter = definedNames.iterator(); iter.hasNext();) {
      String name = (String)iter.next();
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
    for (Iterator iter = patternTable.entrySet().iterator();
	 iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      int count = ((Integer)entry.getValue()).intValue();
      if (bestPattern == null || count > bestCount) {
	bestCount = count;
	bestPattern = (String)entry.getKey();
      }
    }
    if (bestPattern == null)
      return DEFAULT_PATTERN;
    if (bestPattern.charAt(0) == '%')
      return bestPattern.substring(0, 2) + "#";
    else
      return "#" + bestPattern.substring(bestPattern.length() - 2);
  }

  private static void inc(Map table, String str) {
    Integer n = (Integer)table.get(str);
    if (n == null)
      table.put(str, new Integer(1));
    else
      table.put(str, new Integer(n.intValue() + 1));
  }

  private boolean patternOk(String pattern, String otherPattern) {
    Set usedNames = new HashSet();
    for (Iterator iter = elementNameTable.keySet().iterator();
	 iter.hasNext();) {
      String name = mungeQName((String)iter.next());
      String declName = substitute(pattern, '%', name);
      if (definedNames.contains(declName))
	return false;
      if (otherPattern != null) {
        String otherDeclName = substitute(otherPattern, '%', name);
        if (usedNames.contains(declName)
            || usedNames.contains(otherDeclName)
            || declName.equals(otherDeclName))
          return false;
        usedNames.add(declName);
        usedNames.add(otherDeclName);
      }
    }
    return true;
  }

  private void noteDef(String name) {
    definedNames.add(name);
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

  private void noteAttlist(String name, AttributeGroup group) {
    List groups = (List)attlistDeclTable.get(name);
    if (groups == null) {
      groups = new Vector();
      attlistDeclTable.put(name, groups);
    }
    groups.add(group);
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

  private void outputStart(List components) {
    ChoicePattern choice = new ChoicePattern();
    // Use the defined but unreferenced elements.
    // If there aren't any, use all defined elements.
    int mask = ELEMENT_REF|ELEMENT_DECL;
    for (;;) {
      boolean gotOne = false;
      for (Iterator iter = elementNameTable.entrySet().iterator();
	   iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
	if ((((Integer)entry.getValue()).intValue() & mask) == ELEMENT_DECL) {
	  gotOne = true;
	  choice.getChildren().add(ref(elementDeclName((String)entry.getKey())));
	}
      }
      if (gotOne)
	break;
      if (mask == ELEMENT_DECL)
        return;
      mask = ELEMENT_DECL;
    }
    components.add(new DefineComponent(DefineComponent.START, choice));
  }

  private void outputAny(List components) {
    if (!hadAny)
      return;
    if (options.strictAny) {
      DefineComponent dc = new DefineComponent(anyName, new TextPattern());
      dc.setCombine(Combine.CHOICE);
      components.add(dc);
    }
    else {
      // any = (element * { attribute * { text }*, any } | text)*
      CompositePattern group = new GroupPattern();
      group.getChildren().add(new ZeroOrMorePattern(new AttributePattern(new AnyNameNameClass(),
                                                                         new TextPattern())));
      group.getChildren().add(ref(anyName));
      CompositePattern choice = new ChoicePattern();
      choice.getChildren().add(new ElementPattern(new AnyNameNameClass(), group));
      choice.getChildren().add(new TextPattern());
      components.add(new DefineComponent(anyName, new ZeroOrMorePattern(choice)));
    }
  }

  private void outputUndefinedElements(List components) {
    List elementNames = new Vector();
    elementNames.addAll(elementNameTable.keySet());
    Collections.sort(elementNames);
    for (Iterator iter = elementNames.iterator(); iter.hasNext();) {
      String elementName = (String)iter.next();
      if ((((Integer)elementNameTable.get(elementName)).intValue() & ELEMENT_DECL)
	  == 0) {
        DefineComponent dc = new DefineComponent(elementDeclName(elementName), new NotAllowedPattern());
        dc.setCombine(Combine.CHOICE);
        components.add(dc);
      }
    }
  }

  static private Pattern ref(String name) {
    return new RefPattern(name);
  }

  private void error(String key) {
    er.error(key, null);
  }

  private void error(String key, String arg) {
    er.error(key, arg, null);
  }

  private void warning(String key) {
    er.warning(key, null);
  }

  private void warning(String key, String arg) {
    er.warning(key, arg, null);
  }

  private static String[] valueType(Datatype datatype) {
    datatype = datatype.deref();
    switch (datatype.getType()) {
    case Datatype.CDATA:
      return new String[] { "", "string" };
    case Datatype.TOKENIZED:
      return new String[] { WellKnownNamespaces.XML_SCHEMA_DATATYPES, ((TokenizedDatatype)datatype).getTypeName() };
    }
    return new String[] { "", "token" };
  }

  private Pattern convert(ModelGroup mg) throws Exception {
    ModelGroupOutput mgo = new ModelGroupOutput();
    mg.accept(mgo);
    return mgo.pattern;
  }

  private Pattern convert(Datatype dt) throws Exception {
    DatatypeOutput dto = new DatatypeOutput();
    dt.accept(dto);
    return dto.pattern;
  }

  private Pattern convert(AttributeGroup ag) throws Exception {
    GroupPattern group = new GroupPattern();
    ag.accept(new AttributeGroupOutput(group));
    switch (group.getChildren().size()) {
    case 0:
      return new EmptyPattern();
    case 1:
      return (Pattern)group.getChildren().get(0);
    }
    return group;
  }

  private NameClass convertQName(String name, boolean useDefault) {
    int i = name.indexOf(':');
    if (i < 0)
      return new NameNameClass(useDefault ? defaultNamespace : "", name);
    String prefix = name.substring(0, i);
    String localName = name.substring(i + 1);
    String ns;
    if (prefix.equals("xml"))
      ns = WellKnownNamespaces.XML;
    else {
      ns = (String)prefixTable.get(prefix);
      if (ns.equals("")) {
        error("UNDECLARED_PREFIX", prefix);
        ns = "##" + prefix;
        prefixTable.put(prefix, ns);
      }
    }
    NameNameClass nnc = new NameNameClass(ns, localName);
    nnc.setPrefix(prefix);
    return nnc;
  }
}

