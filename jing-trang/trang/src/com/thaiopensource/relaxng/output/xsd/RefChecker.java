package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.output.common.ErrorReporter;

import java.util.Map;
import java.util.HashMap;

class RefChecker extends AbstractVisitor {
  private final SchemaInfo schema;
  private final ErrorReporter er;
  private final Map refMap = new HashMap();
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

  public Object visitDiv(DivComponent c) {
    c.componentsAccept(this);
    return null;
  }

  public Object visitDefine(DefineComponent c) {
    String name = c.getName();
    if (name == DefineComponent.START || refMap.get(name) == null)
      c.getBody().accept(this);
    return null;
  }

  public Object visitInclude(IncludeComponent c) {
    schema.getSchema(c.getHref()).componentsAccept(this);
    return null;
  }

  public Object visitElement(ElementPattern p) {
    currentDepth++;
    p.getChild().accept(this);
    currentDepth--;
    return null;
  }

  public Object visitUnary(UnaryPattern p) {
    return p.getChild().accept(this);
  }

  public Object visitComposite(CompositePattern p) {
    p.childrenAccept(this);
    return null;
  }

  public Object visitRef(RefPattern p) {
    Ref ref = (Ref)refMap.get(p.getName());
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
    return null;
  }

  public Object visitExternalRef(ExternalRefPattern p) {
    er.error("external_ref_not_supported", p.getSourceLocation());
    return null;
  }

  public Object visitGrammar(GrammarPattern p) {
    er.error("nested_grammar_not_supported", p.getSourceLocation());
    return null;
  }

  public Object visitParentRef(ParentRefPattern p) {
    er.error("parent_ref_no_grammar", p.getSourceLocation());
    return null;
  }
}
