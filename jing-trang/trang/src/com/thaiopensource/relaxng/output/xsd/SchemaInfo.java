package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Combine;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.output.common.ErrorReporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;

class SchemaInfo {
  private final SchemaCollection sc;
  private final GrammarPattern grammar;
  private final ErrorReporter er;
  private final Map childTypeMap = new HashMap();
  private final Map defineMap = new HashMap();
  private final Set ignoredDefines = new HashSet();
  private final PatternVisitor childTypeVisitor = new ChildTypeVisitor();

  private static final int DEFINE_KEEP = 0;
  private static final int DEFINE_IGNORE = 1;
  private static final int DEFINE_REQUIRE = 2;


  static private class Define {
    int status = DEFINE_KEEP;
    boolean hadImplicit;
    Combine combine;
    Pattern pattern;
    CompositePattern wrapper;
    DefineComponent head;
  }

  abstract class PatternAnalysisVisitor extends AbstractVisitor {
    abstract Object get(Pattern p);
    abstract Object choice(Object o1, Object o2);
    abstract Object group(Object o1, Object o2);
    Object interleave(Object o1, Object o2) {
      return group(o1, o2);
    }
    Object ref(Object obj) {
      return obj;
    }
    Object oneOrMore(Object obj) {
      return group(obj, obj);
    }
    abstract Object empty();
    abstract Object text();
    abstract Object data();
    abstract Object notAllowed();
    Object list(Object obj) {
      return data();
    }

    public Object visitChoice(ChoicePattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = choice(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitGroup(GroupPattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = group(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitInterleave(InterleavePattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = interleave(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return choice(empty(), oneOrMore(get(p.getChild())));
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return oneOrMore(get(p.getChild()));
    }

    public Object visitOptional(OptionalPattern p) {
      return choice(empty(), get(p.getChild()));
    }

    public Object visitEmpty(EmptyPattern p) {
      return empty();
    }

    public Object visitRef(RefPattern p) {
      return ref(get(getBody(p)));
    }

    public Object visitMixed(MixedPattern p) {
      return interleave(text(), get(p.getChild()));
    }

    public Object visitText(TextPattern p) {
      return text();
    }

    public Object visitData(DataPattern p) {
      return data();
    }

    public Object visitValue(ValuePattern p) {
      return data();
    }

    public Object visitList(ListPattern p) {
      return list(get(p.getChild()));
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      return notAllowed();
    }
  }

  class ChildTypeVisitor extends PatternAnalysisVisitor {
    Object get(Pattern p) {
      return getChildType(p);
    }

    Object empty() {
      return ChildType.EMPTY;
    }

    Object text() {
      return ChildType.choice(ChildType.TEXT, ChildType.EMPTY);
    }

    Object data() {
      return ChildType.DATA;
    }

    Object notAllowed() {
      return ChildType.NOT_ALLOWED;
    }

    Object list(Object obj) {
      if (obj.equals(ChildType.NOT_ALLOWED))
        return obj;
      return data();
    }

    Object choice(Object o1, Object o2) {
      return ChildType.choice((ChildType)o1, (ChildType)o2);
    }

    Object group(Object o1, Object o2) {
      return ChildType.group((ChildType)o1, (ChildType)o2);
    }

    public Object visitElement(ElementPattern p) {
      return ChildType.ELEMENT;
    }

    public Object visitAttribute(AttributePattern p) {
      if (getChildType(p.getChild()).equals(ChildType.NOT_ALLOWED))
        return ChildType.NOT_ALLOWED;
      return ChildType.choice(ChildType.ATTRIBUTE, ChildType.EMPTY);
    }
  }

  static class Override {
    int status;
    final Define define;
    final String name;

    Override(Define define, String name) {
      this.define = define;
      this.name = name;
    }
  }

  class GrammarVisitor implements ComponentVisitor {
    private final Set openIncludes = new HashSet();
    private final Set allIncludes = new HashSet();
    private List overrides = null;

    public Object visitDefine(DefineComponent c) {
      Define define = lookupDefine(c.getName());
      if (overrides != null)
        overrides.add(new Override(define, c.getName()));
      if (define.status != DEFINE_KEEP) {
        ignoredDefines.add(c);
        define.status = DEFINE_IGNORE;
        return null;
      }
      if (c.getCombine() == null) {
        if (define.hadImplicit) {
          er.error("multiple_define", c.getName(), c.getSourceLocation());
          return null;
        }
        define.hadImplicit = true;
      }
      else if (define.combine == null) {
        define.combine = c.getCombine();
        if (define.combine == Combine.CHOICE)
          define.wrapper = new ChoicePattern();
        else
          define.wrapper = new InterleavePattern();
        define.wrapper.setSourceLocation(c.getSourceLocation());
      }
      else if (define.combine != c.getCombine()) {
        er.error("inconsistent_combine", c.getName(), c.getSourceLocation());
        return null;
      }
      if (define.pattern == null) {
        define.pattern = c.getBody();
        define.head = c;
      }
      else {
        if (define.pattern != define.wrapper)
          define.wrapper.getChildren().add(define.pattern);
        define.wrapper.getChildren().add(c.getBody());
        define.pattern = define.wrapper;
      }
      return null;
    }

    public Object visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      List overrides = new Vector();
      List savedOverrides = this.overrides;
      this.overrides = overrides;
      c.componentsAccept(this);
      this.overrides = savedOverrides;
      String href = c.getHref();
      if (openIncludes.contains(href))
        er.error("include_loop", href, c.getSourceLocation());
      else if (allIncludes.contains(href))
        er.error("multiple_include", href, c.getSourceLocation());
      else {
        for (Iterator iter = overrides.iterator(); iter.hasNext();) {
          Override or = (Override)iter.next();
          or.status = or.define.status;
          or.define.status = DEFINE_REQUIRE;
        }
        allIncludes.add(href);
        openIncludes.add(href);
        getSchema(href).componentsAccept(this);
        openIncludes.remove(href);
        for (Iterator iter = overrides.iterator(); iter.hasNext();) {
          Override or = (Override)iter.next();
          if (or.define.status == DEFINE_REQUIRE) {
            if (or.name == DefineComponent.START)
              er.error("missing_start_replacement", c.getSourceLocation());
            else
              er.error("missing_define_replacement", or.name, c.getSourceLocation());
          }
          or.define.status = or.status;
        }
      }
      return null;
    }
  }

  SchemaInfo(SchemaCollection sc, ErrorReporter er) {
    this.sc = sc;
    this.er = er;
    forceGrammar();
    grammar = getSchema(sc.getMainUri());
    grammar.componentsAccept(new GrammarVisitor());
  }

  private void forceGrammar() {
    SchemaDocument sd = (SchemaDocument)sc.getSchemaDocumentMap().get(sc.getMainUri());
    sd.setPattern(convertToGrammar(sd.getPattern()));
    // TODO convert other schemas
  }


  private static GrammarPattern convertToGrammar(Pattern p) {
    if (p instanceof GrammarPattern)
      return (GrammarPattern)p;
    GrammarPattern g = new GrammarPattern();
    g.setSourceLocation(p.getSourceLocation());
    g.setContext(p.getContext());
    DefineComponent dc = new DefineComponent(DefineComponent.START, p);
    dc.setSourceLocation(p.getSourceLocation());
    g.getComponents().add(dc);
    return g;
  }

  GrammarPattern getGrammar() {
    return grammar;
  }

  String getMainUri() {
    return sc.getMainUri();
  }

  GrammarPattern getSchema(String sourceUri) {
    return (GrammarPattern)((SchemaDocument)sc.getSchemaDocumentMap().get(sourceUri)).getPattern();
  }

  String getEncoding(String sourceUri) {
    return ((SchemaDocument)sc.getSchemaDocumentMap().get(sourceUri)).getEncoding();
  }

  ChildType getChildType(Pattern p) {
    ChildType ct = (ChildType)childTypeMap.get(p);
    if (ct == null) {
      ct = (ChildType)p.accept(childTypeVisitor);
      childTypeMap.put(p, ct);
    }
    return ct;
  }

  Pattern getStart() {
    return lookupDefine(DefineComponent.START).pattern;
  }

  Pattern getBody(RefPattern p) {
    return lookupDefine(p.getName()).pattern;
  }

  Pattern getBody(DefineComponent c) {
    Define def = lookupDefine(c.getName());
    if (def == null || def.head != c)
      return null;
    return def.pattern;
  }

  boolean isIgnored(DefineComponent c) {
    return ignoredDefines.contains(c);
  }

  private Define lookupDefine(String name) {
    Define define = (Define)defineMap.get(name);
    if (define == null) {
      define = new Define();
      defineMap.put(name, define);
    }
    return define;
  }

}