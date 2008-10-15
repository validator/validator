package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractPatternVisitor;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Combine;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
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
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.output.common.ErrorReporter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

class SchemaInfo {
  private final SchemaCollection sc;
  private final GrammarPattern grammar;
  private final ErrorReporter er;
  private final Map<Pattern, ChildType> childTypeMap = new HashMap<Pattern, ChildType>();
  private final Map<String, Define> defineMap = new HashMap<String, Define>();
  private final Set<DefineComponent> ignoredDefines = new HashSet<DefineComponent>();
  private final PatternVisitor<ChildType> childTypeVisitor = new ChildTypeVisitor();

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

  abstract class PatternAnalysisVisitor<T> extends AbstractPatternVisitor<T> {
    abstract T get(Pattern p);
    abstract T choice(T o1, T o2);
    abstract T group(T o1, T o2);
    T interleave(T o1, T o2) {
      return group(o1, o2);
    }
    T ref(T obj) {
      return obj;
    }
    T oneOrMore(T obj) {
      return group(obj, obj);
    }
    abstract T empty();
    abstract T text();
    abstract T data();
    abstract T notAllowed();
    T list(T obj) {
      return data();
    }

    public T visitChoice(ChoicePattern p) {
      List<Pattern> list = p.getChildren();
      T obj = get(list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = choice(obj, get(list.get(i)));
      return obj;
    }

    public T visitGroup(GroupPattern p) {
      List<Pattern> list = p.getChildren();
      T obj = get(list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = group(obj, get(list.get(i)));
      return obj;
    }

    public T visitInterleave(InterleavePattern p) {
      List<Pattern> list = p.getChildren();
      T obj = get(list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = interleave(obj, get(list.get(i)));
      return obj;
    }

    public T visitZeroOrMore(ZeroOrMorePattern p) {
      return choice(empty(), oneOrMore(get(p.getChild())));
    }

    public T visitOneOrMore(OneOrMorePattern p) {
      return oneOrMore(get(p.getChild()));
    }

    public T visitOptional(OptionalPattern p) {
      return choice(empty(), get(p.getChild()));
    }

    public T visitEmpty(EmptyPattern p) {
      return empty();
    }

    public T visitRef(RefPattern p) {
      return ref(get(getBody(p)));
    }

    public T visitMixed(MixedPattern p) {
      return interleave(text(), get(p.getChild()));
    }

    public T visitText(TextPattern p) {
      return text();
    }

    public T visitData(DataPattern p) {
      return data();
    }

    public T visitValue(ValuePattern p) {
      return data();
    }

    public T visitList(ListPattern p) {
      return list(get(p.getChild()));
    }

    public T visitNotAllowed(NotAllowedPattern p) {
      return notAllowed();
    }

    public T visitPattern(Pattern p) {
      return null;
    }
  }

  class ChildTypeVisitor extends PatternAnalysisVisitor<ChildType> {
    ChildType get(Pattern p) {
      return getChildType(p);
    }

    ChildType empty() {
      return ChildType.EMPTY;
    }

    ChildType text() {
      return ChildType.choice(ChildType.TEXT, ChildType.EMPTY);
    }

    ChildType data() {
      return ChildType.DATA;
    }

    ChildType notAllowed() {
      return ChildType.NOT_ALLOWED;
    }

    ChildType list(ChildType t) {
      if (t == ChildType.NOT_ALLOWED)
        return t;
      return data();
    }

    ChildType choice(ChildType t1, ChildType t2) {
      return ChildType.choice(t1, t2);
    }

    ChildType group(ChildType t1, ChildType t2) {
      return ChildType.group(t1, t2);
    }

    public ChildType visitElement(ElementPattern p) {
      return ChildType.ELEMENT;
    }

    public ChildType visitAttribute(AttributePattern p) {
      if (getChildType(p.getChild()) == ChildType.NOT_ALLOWED)
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

  class GrammarVisitor implements ComponentVisitor<VoidValue> {
    private final Set<String> openIncludes = new HashSet<String>();
    private final Set<String> allIncludes = new HashSet<String>();
    private List<Override> overrides = null;

    public VoidValue visitDefine(DefineComponent c) {
      Define define = lookupDefine(c.getName());
      if (overrides != null)
        overrides.add(new Override(define, c.getName()));
      if (define.status != DEFINE_KEEP) {
        ignoredDefines.add(c);
        define.status = DEFINE_IGNORE;
        return VoidValue.VOID;
      }
      if (c.getCombine() == null) {
        if (define.hadImplicit) {
          er.error("multiple_define", c.getName(), c.getSourceLocation());
          return VoidValue.VOID;
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
        return VoidValue.VOID;
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
      return VoidValue.VOID;
    }

    public VoidValue visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return VoidValue.VOID;
    }

    public VoidValue visitInclude(IncludeComponent c) {
      List<Override> overrides = new Vector<Override>();
      List<Override> savedOverrides = this.overrides;
      this.overrides = overrides;
      c.componentsAccept(this);
      this.overrides = savedOverrides;
      String href = c.getHref();
      if (openIncludes.contains(href))
        er.error("include_loop", href, c.getSourceLocation());
      else if (allIncludes.contains(href))
        er.error("multiple_include", href, c.getSourceLocation());
      else {
        for (Override or : overrides) {
          or.status = or.define.status;
          or.define.status = DEFINE_REQUIRE;
        }
        allIncludes.add(href);
        openIncludes.add(href);
        getSchema(href).componentsAccept(this);
        openIncludes.remove(href);
        for (Override or : overrides) {
          if (or.define.status == DEFINE_REQUIRE) {
            if (or.name == DefineComponent.START)
              er.error("missing_start_replacement", c.getSourceLocation());
            else
              er.error("missing_define_replacement", or.name, c.getSourceLocation());
          }
          or.define.status = or.status;
        }
      }
      return VoidValue.VOID;
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
    SchemaDocument sd = sc.getSchemaDocumentMap().get(sc.getMainUri());
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
    return (GrammarPattern)(sc.getSchemaDocumentMap().get(sourceUri)).getPattern();
  }

  String getEncoding(String sourceUri) {
    return (sc.getSchemaDocumentMap().get(sourceUri)).getEncoding();
  }

  ChildType getChildType(Pattern p) {
    ChildType ct = childTypeMap.get(p);
    if (ct == null) {
      ct = p.accept(childTypeVisitor);
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
    Define define = defineMap.get(name);
    if (define == null) {
      define = new Define();
      defineMap.put(name, define);
    }
    return define;
  }

}