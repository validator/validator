package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.output.common.ErrorReporter;

import java.util.HashMap;
import java.util.Map;

class RefChecker extends AbstractVisitor {
  private final SchemaInfo schema;
  private final ErrorReporter er;
  private final Map<String, Ref> refMap = new HashMap<String, Ref>();
  private int currentDepth = 0;

  static private class Ref {
    int checkRecursionDepth;

    Ref(int checkRecursionDepth) {
      this.checkRecursionDepth = checkRecursionDepth;
    }
  }

  private RefChecker(SchemaInfo schema, ErrorReporter er) {
    this.schema = schema;
    this.er = er;
  }

  static void check(SchemaInfo schema, ErrorReporter er) {
    schema.getGrammar().componentsAccept(new RefChecker(schema, er));
  }

  public VoidValue visitDiv(DivComponent c) {
    c.componentsAccept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitDefine(DefineComponent c) {
    String name = c.getName();
    if (name == DefineComponent.START || refMap.get(name) == null)
      c.getBody().accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitInclude(IncludeComponent c) {
    schema.getSchema(c.getHref()).componentsAccept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitElement(ElementPattern p) {
    currentDepth++;
    p.getChild().accept(this);
    currentDepth--;
    return VoidValue.VOID;
  }

  public VoidValue visitUnary(UnaryPattern p) {
    return p.getChild().accept(this);
  }

  public VoidValue visitComposite(CompositePattern p) {
    p.childrenAccept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitRef(RefPattern p) {
    Ref ref = refMap.get(p.getName());
    if (ref == null) {
      ref = new Ref(currentDepth);
      refMap.put(p.getName(), ref);
      Pattern body = schema.getBody(p);
      if (body == null)
        er.error("undefined_reference", p.getName(), p.getSourceLocation());
      else
        schema.getBody(p).accept(this);
      ref.checkRecursionDepth = -1;
    }
    else if (currentDepth == ref.checkRecursionDepth)
      er.error("recursive_reference", p.getName(), p.getSourceLocation());
    return VoidValue.VOID;
  }

  public VoidValue visitExternalRef(ExternalRefPattern p) {
    er.error("external_ref_not_supported", p.getSourceLocation());
    return VoidValue.VOID;
  }

  public VoidValue visitGrammar(GrammarPattern p) {
    er.error("nested_grammar_not_supported", p.getSourceLocation());
    return VoidValue.VOID;
  }

  public VoidValue visitParentRef(ParentRefPattern p) {
    er.error("parent_ref_no_grammar", p.getSourceLocation());
    return VoidValue.VOID;
  }
}
