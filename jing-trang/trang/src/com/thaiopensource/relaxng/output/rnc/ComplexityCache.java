package com.thaiopensource.relaxng.output.rnc;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.NameClassedPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

class ComplexityCache {
  private final ComplexityVisitor complexityVisitor = new ComplexityVisitor();
  private final Map cache = new HashMap();

  static private class Complexity {
    static private final int MAX_BRACE = 0;
    static private final int MAX_PAREN = 2;
    static final Object SIMPLE = new Integer(0);
    static final Object VERY_COMPLICATED = new Integer(MAX_BRACE + 1);
    static Object max(Object c1, Object c2) {
      int n1 = ((Integer)c1).intValue();
      int n2 = ((Integer)c2).intValue();
      if (n1 > 0)
        return n1 > n2 ? c1 : c2;
      if (n2 > 0)
        return c2;
      return n1 < n2 ? c1 : c2;
    }
    static Object brace(Object c) {
      int n = ((Integer)c).intValue();
      return new Integer(n <= 0 ? 1 : n + 1);
    }
    static Object paren(Object c) {
      int n = ((Integer)c).intValue();
      return new Integer(n > 0 ? n : n - 1);
    }
    static boolean isComplex(Object c) {
      int n = ((Integer)c).intValue();
      return n > MAX_BRACE || n < -MAX_PAREN;
    }
  }

  private class ComplexityVisitor extends AbstractVisitor {
    Object visit(Pattern p) {
      Object obj = cache.get(p);
      if (obj == null) {
        obj = p.accept(this);
        cache.put(p, obj);
      }
      return obj;
    }

    public Object visitGrammar(GrammarPattern p) {
      return Complexity.VERY_COMPLICATED;
    }

    public Object visitNameClassed(NameClassedPattern p) {
      return brace(p);
    }

    public Object visitList(ListPattern p) {
      return brace(p);
    }

    public Object visitMixed(MixedPattern p) {
      return brace(p);
    }

    private Object brace(UnaryPattern p) {
      return Complexity.brace(visit(p.getChild()));
    }

    public Object visitUnary(UnaryPattern p) {
      return visit(p.getChild());
    }

    public Object visitData(DataPattern p) {
      Object ret = Complexity.SIMPLE;
      if (p.getParams().size() > 0)
        ret = Complexity.brace(ret);
      if (p.getExcept() != null)
        ret = Complexity.max(ret, visit(p.getExcept()));
      return ret;
    }

    public Object visitComposite(CompositePattern p) {
      Object ret = Complexity.SIMPLE;
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();)
        ret = Complexity.max(ret, visit((Pattern)iter.next()));
      return Complexity.paren(ret);
    }

    public Object visitPattern(Pattern p) {
      return Complexity.SIMPLE;
    }
  }


  public boolean isComplex(Pattern p) {
    return Complexity.isComplex(complexityVisitor.visit(p));
  }
}
