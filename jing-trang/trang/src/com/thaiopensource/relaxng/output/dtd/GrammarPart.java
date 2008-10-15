package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.Combine;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.output.common.ErrorReporter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GrammarPart implements ComponentVisitor<VoidValue> {
  private final ErrorReporter er;
  private final Map<String, Pattern> defines;
  private final Set<String> attlists;
  private final Set<String> implicitlyCombinedDefines;
  private final Map<String, Combine> combineTypes;
  private final SchemaCollection schemas;
  private final Map<String, GrammarPart> parts;
  // maps name to component that provides it
  private final Map<String, Component> whereProvided = new HashMap<String, Component>();
  private final Set<String> pendingIncludes;

  public static class IncludeLoopException extends RuntimeException {
    private final IncludeComponent include;

    public IncludeLoopException(IncludeComponent include) {
      this.include = include;
    }

    public IncludeComponent getInclude() {
      return include;
    }
  }


  GrammarPart(ErrorReporter er, Map<String, Pattern> defines, Set<String> attlists, SchemaCollection schemas, Map<String, GrammarPart> parts, GrammarPattern p) {
    this.er = er;
    this.defines = defines;
    this.attlists = attlists;
    this.schemas = schemas;
    this.parts = parts;
    this.pendingIncludes = new HashSet<String>();
    this.implicitlyCombinedDefines = new HashSet<String>();
    this.combineTypes = new HashMap<String, Combine>();
    visitContainer(p);
  }

  private GrammarPart(GrammarPart part, GrammarPattern p) {
    er = part.er;
    defines = part.defines;
    schemas = part.schemas;
    parts = part.parts;
    attlists = part.attlists;
    pendingIncludes = part.pendingIncludes;
    implicitlyCombinedDefines = part.implicitlyCombinedDefines;
    combineTypes = part.combineTypes;
    visitContainer(p);
  }

  Set<String> providedSet() {
    return whereProvided.keySet();
  }

  public VoidValue visitContainer(Container c) {
    List<Component> list = c.getComponents();
    for (int i = 0, len = list.size(); i < len; i++)
      (list.get(i)).accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitDiv(DivComponent c) {
    return visitContainer(c);
  }

  public VoidValue visitDefine(DefineComponent c) {
    String name = c.getName();
    Combine combine = c.getCombine();
    if (combine == null) {
      if (implicitlyCombinedDefines.contains(name))
        er.error("multiple_no_combine", name, c.getSourceLocation());
      else
        implicitlyCombinedDefines.add(name);
    }
    else {
      Combine oldCombine = combineTypes.get(name);
      if (oldCombine != null) {
        if (oldCombine != combine)
          er.error("inconsistent_combine", c.getSourceLocation());
      }
      else
        combineTypes.put(name, combine);
    }
    Pattern oldDef = defines.get(name);
    if (oldDef != null) {
      if (combine == Combine.CHOICE)
        er.error("sorry_combine_choice", c.getSourceLocation());
      else if (combine == Combine.INTERLEAVE) {
        InterleavePattern ip = new InterleavePattern();
        ip.getChildren().add(oldDef);
        ip.getChildren().add(c.getBody());
        ip.setSourceLocation(c.getSourceLocation());
        defines.put(name, ip);
        attlists.add(name);
      }
    }
    else {
      defines.put(name, c.getBody());
      whereProvided.put(name, c);
    }
    return VoidValue.VOID;
  }

  public VoidValue visitInclude(IncludeComponent c) {
    String href = c.getHref();
    if (pendingIncludes.contains(href))
      throw new IncludeLoopException(c);
    pendingIncludes.add(href);
    GrammarPattern p = (GrammarPattern)(schemas.getSchemaDocumentMap().get(href)).getPattern();
    GrammarPart part = new GrammarPart(this, p);
    parts.put(href, part);
    for (String name : part.providedSet())
      whereProvided.put(name, c);
    pendingIncludes.remove(href);
    return VoidValue.VOID;
  }

  Component getWhereProvided(String paramEntityName) {
    return whereProvided.get(paramEntityName);
  }
}
