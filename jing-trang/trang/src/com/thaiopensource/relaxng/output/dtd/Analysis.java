package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.NameClassVisitor;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.common.NameClassSplitter;
import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.xml.util.Naming;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;

class Analysis {
  private final NamespaceManager nsm = new NamespaceManager();
  private final AttlistMapper am = new AttlistMapper();
  private final ErrorReporter er;
  private final Map contentTypes = new HashMap();
  private final Map attributeTypes = new HashMap();
  private final Map attributeAlphabets = new HashMap();
  private final Map attributeNamespaces = new HashMap();
  private Map defines = null;
  private final Set attlists = new HashSet();
  private final Map parts = new HashMap();
  private final Map seenTable = new HashMap();
  private final Map elementDecls = new HashMap();
  private ContentType startType = ContentType.ERROR;
  private GrammarPart mainPart;
  private final SchemaCollection schemas;
  private GrammarPattern grammarPattern;
  private final AttributeTyper attributeTyper = new AttributeTyper();
  private final AttributeAlphabetComputer attributeAlphabetComputer = new AttributeAlphabetComputer();
  private final AttributeNamespacesComputer attributeNamespacesComputer = new AttributeNamespacesComputer();
  private final IncludeContentChecker includeContentChecker = new IncludeContentChecker();

  private class Analyzer implements PatternVisitor, ComponentVisitor, NameClassVisitor {
    private ElementPattern ancestorPattern;
    private final Set pendingRefs;

    public Analyzer() {
      pendingRefs = new HashSet();
    }

    private Analyzer(ElementPattern ancestorPattern) {
      this.ancestorPattern = ancestorPattern;
      pendingRefs = new HashSet();
    }

    private Analyzer(Set pendingRefs) {
      this.pendingRefs = pendingRefs;
    }

    public Object visitEmpty(EmptyPattern p) {
      return ContentType.EMPTY;
    }

    public Object visitData(DataPattern p) {
      return ContentType.SIMPLE_TYPE;
    }

    public Object visitValue(ValuePattern p) {
      Datatypes.Info info = Datatypes.getInfo(p.getDatatypeLibrary(), p.getType());
      if (info.usesTokenEquality() && Naming.isNmtoken(p.getValue()))
        return ContentType.ENUM;
      if (info.usesCdataEquality())
        return ContentType.VALUE;
      return ContentType.SIMPLE_TYPE;
    }

    public Object visitElement(ElementPattern p) {
      int len;
      if (seen(p))
        len = NameClassSplitter.split(p.getNameClass()).size();
      else {
        new Analyzer(p).analyzeContentType(p.getChild());
        List names = noteNames(p.getNameClass(), true);
        len = names.size();
        for (int i = 0; i < len; i++) {
          NameNameClass nnc = (NameNameClass)names.get(i);
          String ns = nnc.getNamespaceUri();
          if (ns == NameClass.INHERIT_NS)
            ns = "";
          Name name = new Name(ns, nnc.getLocalName());
          ElementPattern prev = (ElementPattern)elementDecls.get(name);
          if (prev != null) {
            er.error("sorry_multiple_element", ns, name.getLocalName(), p.getSourceLocation());
            er.error("other_element", prev.getSourceLocation());
          }
          else
            elementDecls.put(name, p);
        }
      }
      return len == 1 ? ContentType.DIRECT_SINGLE_ELEMENT : ContentType.ELEMENT_CLASS;
    }

    public Object visitAttribute(AttributePattern p) {
      noteNames(p.getNameClass(), false);
      ContentType t = analyzeContentType(p.getChild());
      if (t.isA(ContentType.MODEL_GROUP) || t == ContentType.MIXED_ELEMENT_CLASS || t == ContentType.MIXED_MODEL)
        er.error("bad_attribute_type", p.getSourceLocation());
      if (ancestorPattern != null)
        am.noteAttribute(ancestorPattern);
      return ContentType.EMPTY;
    }

    private List noteNames(NameClass nc, boolean defaultable) {
      nc.accept(this);
      List names = NameClassSplitter.split(nc);
      int len = names.size();
      for (int i = 0; i < len; i++)
        nsm.noteName((NameNameClass)names.get(i), defaultable);
      return names;
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      return ContentType.NOT_ALLOWED;
    }

    public Object visitText(TextPattern p) {
      return ContentType.TEXT;
    }

    public Object visitList(ListPattern p) {
      return ContentType.SIMPLE_TYPE;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return checkContentType("sorry_one_or_more", ContentType.oneOrMore(analyzeContentTypeNullAncestorPattern(p.getChild())), p);
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return checkContentType("sorry_zero_or_more", ContentType.zeroOrMore(analyzeContentTypeNullAncestorPattern(p.getChild())), p);
    }

    public Object visitChoice(ChoicePattern p) {
      List children = p.getChildren();
      ContentType tem = analyzeContentType((Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkContentType("sorry_choice", ContentType.choice(tem, analyzeContentType((Pattern)children.get(i))), p);
      if (getAttributeType(p) == AttributeType.MULTI) {
        Set attributeNames = new HashSet();
        for (int i = 0, len = children.size(); i < len; i++) {
          Set childAttributeNames = getAttributeAlphabet((Pattern)children.get(i));
          for (Iterator iter = childAttributeNames.iterator(); iter.hasNext();) {
            Object name = iter.next();
            if (attributeNames.contains(name))
              er.error("sorry_choice_attribute_name",
                       ((Name)name).getNamespaceUri(),
                       ((Name)name).getLocalName(),
                       p.getSourceLocation());
            else
              attributeNames.add(name);
          }
        }
      }
      return tem;
    }

    public Object visitInterleave(InterleavePattern p) {
      List children = p.getChildren();
      ContentType tem = analyzeContentType((Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkContentType("sorry_interleave", ContentType.interleave(tem, analyzeContentType((Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitGroup(GroupPattern p) {
      List children = p.getChildren();
      ContentType tem = analyzeContentType((Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkContentType("sorry_group", ContentType.group(tem, analyzeContentType((Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitRef(RefPattern p) {
      String name = p.getName();
      Pattern def = getBody(name);
      if (def == null) {
        er.error("undefined_ref", p.getSourceLocation());
        return ContentType.ERROR;
      }
      if (pendingRefs.contains(name)) {
        er.error("ref_loop", p.getSourceLocation());
        return ContentType.ERROR;
      }
      pendingRefs.add(name);
      ContentType t = ContentType.ref(new Analyzer(pendingRefs).analyzeContentType(def));
      pendingRefs.remove(name);
      if (t.isA(ContentType.EMPTY))
        am.noteAttributeGroupRef(ancestorPattern, p.getName());
      return ContentType.ref(t);
    }

    public Object visitParentRef(ParentRefPattern p) {
      er.error("sorry_parent_ref", p.getSourceLocation());
      return null;
    }

    public Object visitGrammar(GrammarPattern p) {
      if (defines != null) {
        er.error("sorry_nested_grammar", p.getSourceLocation());
        return ContentType.ERROR;
      }
      defines = new HashMap();
      try {
        mainPart = new GrammarPart(er, defines, attlists, schemas, parts, p);
      }
      catch (GrammarPart.IncludeLoopException e) {
        er.error("include_loop", e.getInclude().getSourceLocation());
        return ContentType.ERROR;
      }
      grammarPattern = p;
      visitContainer(p);
      return startType;
    }

    public Object visitExternalRef(ExternalRefPattern p) {
      er.error("sorry_external_ref", p.getSourceLocation());
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      return checkContentType("sorry_mixed", ContentType.mixed(analyzeContentType(p.getChild())), p);
    }

    public Object visitOptional(OptionalPattern p) {
      return checkContentType("sorry_optional", ContentType.optional(analyzeContentTypeNullAncestorPattern(p.getChild())), p);
    }

    public Object visitContainer(Container c) {
      List list = c.getComponents();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Component)list.get(i)).accept(this);
      return null;
    }

    public Object visitDiv(DivComponent c) {
      return visitContainer(c);
    }

    public Object visitDefine(DefineComponent c) {
      if (c.getName() == DefineComponent.START)
        startType = analyzeContentType(c.getBody());
      else
        new Analyzer().analyzeContentType(c.getBody());
      if (attlists.contains(c.getName()) && getContentType(c.getBody()) != ContentType.EMPTY) {
        er.error("not_attlist", c.getName(), c.getSourceLocation());
        attlists.remove(c.getName());
      }
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      includeContentChecker.visitContainer(c);
      visitContainer((GrammarPattern)((SchemaDocument)schemas.getSchemaDocumentMap().get(c.getHref())).getPattern());
      return null;
    }

    public Object visitChoice(ChoiceNameClass nc) {
      List list = nc.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((NameClass)list.get(i)).accept(this);
      return ContentType.ELEMENT_CLASS;
    }

    public Object visitAnyName(AnyNameNameClass nc) {
      er.error("sorry_wildcard", nc.getSourceLocation());
      return null;
    }

    public Object visitNsName(NsNameNameClass nc) {
      er.error("sorry_wildcard", nc.getSourceLocation());
      return null;
    }

    public Object visitName(NameNameClass nc) {
      nsm.noteName(nc, true);
      return ContentType.DIRECT_SINGLE_ELEMENT;
    }

    ContentType checkContentType(String key, ContentType t, Pattern p) {
      if (t != null)
        return t;
      er.error(key, p.getSourceLocation());
      return ContentType.ERROR;
    }

    ContentType analyzeContentType(Pattern p) {
      ContentType t = (ContentType)contentTypes.get(p);
      if (t == null) {
        t = (ContentType)p.accept(this);
        contentTypes.put(p, t);
      }
      return t;
    }

    ContentType analyzeContentTypeNullAncestorPattern(Pattern p) {
      return (ancestorPattern == null ? this : new Analyzer(pendingRefs)).analyzeContentType(p);
    }

  }

  class IncludeContentChecker extends AbstractVisitor {
    public Object visitContainer(Container c) {
      List list = c.getComponents();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Component)list.get(i)).accept(this);
      return null;
    }

    public Object visitDefine(DefineComponent c) {
      er.error("sorry_include_override", c.getSourceLocation());
      return null;
    }

    public Object visitDiv(DivComponent c) {
      return visitContainer(c);
    }
  }

  class AttributeTyper extends AbstractVisitor {
    public Object visitPattern(Pattern p) {
      return AttributeType.EMPTY;
    }

    public Object visitMixed(MixedPattern p) {
      return getAttributeType(p.getChild());
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return getAttributeType(p.getChild());
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return getAttributeType(p.getChild());
    }

    public Object visitOptional(OptionalPattern p) {
      return getAttributeType(p.getChild());
    }

    public Object visitComposite(CompositePattern p) {
      List list = p.getChildren();
      AttributeType at = getAttributeType((Pattern)list.get(0));
      for (int i = 1, len = list.size(); i < len; i++)
        at = AttributeType.group(at, getAttributeType((Pattern)list.get(i)));
      return at;
    }

    public Object visitAttribute(AttributePattern p) {
      return AttributeType.SINGLE;
    }

    public Object visitEmpty(EmptyPattern p) {
      return AttributeType.MULTI;
    }

    public Object visitRef(RefPattern p) {
      return getAttributeType(getBody(p.getName()));
    }
  }


  class AttributeAlphabetComputer extends AbstractVisitor {
    public Object visitPattern(Pattern p) {
      return Collections.EMPTY_SET;
    }

    public Object visitMixed(MixedPattern p) {
      return getAttributeAlphabet(p.getChild());
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return getAttributeAlphabet(p.getChild());
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return getAttributeAlphabet(p.getChild());
    }

    public Object visitOptional(OptionalPattern p) {
      return getAttributeAlphabet(p.getChild());
    }

    public Object visitComposite(CompositePattern p) {
      List list = p.getChildren();
      Set result = new HashSet();
      for (int i = 0, len = list.size(); i < len; i++)
        result.addAll(getAttributeAlphabet((Pattern)list.get(i)));
      return result;
    }

    public Object visitAttribute(AttributePattern p) {
      Set result = new HashSet();
      List names = NameClassSplitter.split(p.getNameClass());
      for (int i = 0, len = names.size(); i < len; i++) {
        NameNameClass nnc = (NameNameClass)names.get(i);
        String ns = nnc.getNamespaceUri();
        if (ns == NameClass.INHERIT_NS)
          ns = "";
        result.add(new Name(ns, nnc.getLocalName()));
      }
      return result;
    }

    public Object visitRef(RefPattern p) {
      return getAttributeAlphabet(getBody(p.getName()));
    }
  }

  class AttributeNamespacesComputer extends AbstractVisitor {
    public Object visitPattern(Pattern p) {
      return Collections.EMPTY_SET;
    }

    public Object visitMixed(MixedPattern p) {
      return getAttributeNamespaces(p.getChild());
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return getAttributeNamespaces(p.getChild());
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return getAttributeNamespaces(p.getChild());
    }

    public Object visitOptional(OptionalPattern p) {
      return getAttributeNamespaces(p.getChild());
    }

    public Object visitComposite(CompositePattern p) {
      List list = p.getChildren();
      Set result = Collections.EMPTY_SET;
      boolean newResult = false;
      for (int i = 0, len = list.size(); i < len; i++) {
        Set tem = getAttributeNamespaces((Pattern)list.get(i));
        if (tem != Collections.EMPTY_SET && !result.containsAll(tem)) {
          if (result == Collections.EMPTY_SET)
            result = tem;
          else {
            if (!newResult) {
              result = new HashSet(result);
              newResult = true;
            }
            result.addAll(tem);
          }
        }
      }
      if (newResult)
        result = Collections.unmodifiableSet(result);
      return result;
    }

    public Object visitAttribute(AttributePattern p) {
      Set result = null;
      List names = NameClassSplitter.split(p.getNameClass());
      for (int i = 0, len = names.size(); i < len; i++) {
        String ns = ((NameNameClass)names.get(i)).getNamespaceUri();
        if (ns.length() != 0 && ns != NameClass.INHERIT_NS && !ns.equals(WellKnownNamespaces.XML)) {
          if (result == null)
            result = new HashSet();
          result.add(ns);
        }
      }
      if (result == null)
        return Collections.EMPTY_SET;
      return Collections.unmodifiableSet(result);
    }

    public Object visitRef(RefPattern p) {
      return getAttributeNamespaces(getBody(p.getName()));
    }
  }

  private boolean seen(Pattern p) {
    if (seenTable.get(p) != null)
      return true;
    seenTable.put(p, p);
    return false;
  }

  Analysis(SchemaCollection schemas, ErrorReporter er) {
    this.schemas = schemas;
    this.er = er;
    new Analyzer().analyzeContentType(getPattern());
    checkAttlists();
    if (!er.getHadError())
      nsm.assignPrefixes();
  }

  private void checkAttlists() {
    for (Iterator iter = attlists.iterator(); iter.hasNext();) {
      String name = (String)iter.next();
      if (getParamEntityElementName(name) == null)
        er.error("not_attlist", name, getBody(name).getSourceLocation());
    }
  }

  Pattern getPattern() {
    return ((SchemaDocument)schemas.getSchemaDocumentMap().get(schemas.getMainUri())).getPattern();
  }

  String getPrefixForNamespaceUri(String ns) {
    return nsm.getPrefixForNamespaceUri(ns);
  }

  String getDefaultNamespaceUri() {
    return nsm.getDefaultNamespaceUri();
  }

  String getParamEntityElementName(String name) {
    return am.getParamEntityElementName(name);
  }

  ContentType getContentType(Pattern p) {
    return (ContentType)contentTypes.get(p);
  }

  AttributeType getAttributeType(Pattern p) {
    AttributeType at = (AttributeType)attributeTypes.get(p);
    if (at == null) {
      at = (AttributeType)p.accept(attributeTyper);
      attributeTypes.put(p, at);
    }
    return at;
  }

  Set getAttributeAlphabet(Pattern p) {
    Set aa = (Set)attributeAlphabets.get(p);
    if (aa == null) {
      aa = Collections.unmodifiableSet((Set)p.accept(attributeAlphabetComputer));
      attributeAlphabets.put(p, aa);
    }
    return aa;
  }


  Set getAttributeNamespaces(Pattern p) {
    Set aa = (Set)attributeNamespaces.get(p);
    if (aa == null) {
      aa = (Set)p.accept(attributeNamespacesComputer);
      attributeNamespaces.put(p, aa);
    }
    return aa;
  }

  Pattern getBody(String name) {
    return (Pattern)defines.get(name);
  }

  GrammarPattern getGrammarPattern() {
    return grammarPattern;
  }

  String getMainUri() {
    return schemas.getMainUri();
  }

  GrammarPart getGrammarPart(String sourceUri) {
    if (sourceUri.equals(schemas.getMainUri()))
      return mainPart;
    else
      return (GrammarPart)parts.get(sourceUri);
  }

  Pattern getSchema(String sourceUri) {
    return ((SchemaDocument)schemas.getSchemaDocumentMap().get(sourceUri)).getPattern();
  }

  String getEncoding(String sourceUri) {
    return ((SchemaDocument)schemas.getSchemaDocumentMap().get(sourceUri)).getEncoding();
  }
}
