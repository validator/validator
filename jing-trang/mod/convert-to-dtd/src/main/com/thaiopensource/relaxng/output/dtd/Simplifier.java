package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.AbstractPatternVisitor;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;

import java.util.Iterator;
import java.util.List;

class Simplifier extends AbstractPatternVisitor<Pattern> implements ComponentVisitor<VoidValue> {
  public static void simplify(SchemaCollection sc) {
    Simplifier simplifier = new Simplifier();
    for (SchemaDocument sd : sc.getSchemaDocumentMap().values())
      sd.setPattern(sd.getPattern().accept(simplifier));
  }

  private Simplifier() {
  }

  public Pattern visitGrammar(GrammarPattern p) {
    visitContainer(p);
    return p;
  }

  public VoidValue visitContainer(Container c) {
    for (Component component : c.getComponents())
      component.accept(this);
    return VoidValue.VOID;
  }


  public VoidValue visitInclude(IncludeComponent c) {
    return visitContainer(c);
  }

  public VoidValue visitDiv(DivComponent c) {
    return visitContainer(c);
  }

  public VoidValue visitDefine(DefineComponent c) {
    c.setBody(c.getBody().accept(this));
    return VoidValue.VOID;
  }

  public Pattern visitChoice(ChoicePattern p) {
    boolean hadEmpty = false;
    List<Pattern> list = p.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      list.set(i, list.get(i).accept(this));
    for (Iterator<Pattern> iter = list.iterator(); iter.hasNext();) {
      Pattern child = iter.next();
      if (child instanceof NotAllowedPattern)
        iter.remove();
      else if (child instanceof EmptyPattern) {
        hadEmpty = true;
        iter.remove();
      }
    }
    if (list.size() == 0)
      return copy(new NotAllowedPattern(), p);
    Pattern tem;
    if (list.size() == 1)
      tem = list.get(0);
    else
      tem = p;
    if (hadEmpty && !(tem instanceof OptionalPattern) && !(tem instanceof ZeroOrMorePattern)) {
      if (tem instanceof OneOrMorePattern)
        tem = new ZeroOrMorePattern(((OneOrMorePattern)tem).getChild());
      else
        tem = new OptionalPattern(tem);
      copy(tem, p);
    }
    return tem;
  }

  public Pattern visitComposite(CompositePattern p) {
    List<Pattern> list = p.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      list.set(i, list.get(i).accept(this));
    for (Iterator<Pattern> iter = list.iterator(); iter.hasNext();) {
      Pattern child = iter.next();
      if (child instanceof EmptyPattern)
        iter.remove();
    }
    if (list.size() == 0)
      return copy(new EmptyPattern(), p);
    if (list.size() == 1)
      return p.getChildren().get(0);
    return p;
  }


  public Pattern visitInterleave(InterleavePattern p) {
    boolean hadText = false;
    for (Iterator<Pattern> iter = p.getChildren().iterator(); iter.hasNext();) {
      Pattern child = iter.next();
      if (child instanceof TextPattern) {
        iter.remove();
        hadText = true;
      }
    }
    if (!hadText)
      return visitComposite(p);
    return copy(new MixedPattern(visitComposite(p)), p);
  }

  public Pattern visitUnary(UnaryPattern p) {
    p.setChild(p.getChild().accept(this));
    return p;
  }

  private static <T extends Annotated> T copy(T to, T from) {
    to.setSourceLocation(from.getSourceLocation());
    return to;
  }

  public Pattern visitPattern(Pattern p) {
    return p;
  }
}
