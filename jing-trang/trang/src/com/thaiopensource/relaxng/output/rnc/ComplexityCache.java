package com.thaiopensource.relaxng.output.rnc;

import com.thaiopensource.relaxng.edit.AbstractPatternVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NameClassedPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;

import java.util.HashMap;
import java.util.Map;

class ComplexityCache {
  private final ComplexityVisitor complexityVisitor = new ComplexityVisitor();
  private final Map<Pattern, Complexity> cache = new HashMap<Pattern, Complexity>();

  static private class Complexity {
    private int value;
    private Complexity(int value) {
      this.value = value;
    }
    static private final int MAX_BRACE = 0;
    static private final int MAX_PAREN = 2;
    static final Complexity SIMPLE = new Complexity(0);
    static final Complexity VERY_COMPLICATED = new Complexity(MAX_BRACE + 1);
    static Complexity max(Complexity c1, Complexity c2) {
      int n1 = c1.value;
      int n2 = c2.value;
      if (n1 > 0)
        return n1 > n2 ? c1 : c2;
      if (n2 > 0)
        return c2;
      return n1 < n2 ? c1 : c2;
    }
    static Complexity brace(Complexity c) {
      int n = c.value;
      return new Complexity(n <= 0 ? 1 : n + 1);
    }
    static Complexity paren(Complexity c) {
      int n = c.value;
      return n > 0 ? c : new Complexity(n - 1);
    }
    static boolean isComplex(Complexity c) {
      int n = c.value;
      return n > MAX_BRACE || n < -MAX_PAREN;
    }
  }

  private class ComplexityVisitor extends AbstractPatternVisitor<Complexity> {
    Complexity visit(Pattern p) {
      Complexity c = cache.get(p);
      if (c == null) {
        c = p.accept(this);
        cache.put(p, c);
      }
      return c;
    }

    public Complexity visitGrammar(GrammarPattern p) {
      return Complexity.VERY_COMPLICATED;
    }

    public Complexity visitNameClassed(NameClassedPattern p) {
      return brace(p);
    }

    public Complexity visitList(ListPattern p) {
      return brace(p);
    }

    public Complexity visitMixed(MixedPattern p) {
      return brace(p);
    }

    private Complexity brace(UnaryPattern p) {
      return Complexity.brace(visit(p.getChild()));
    }

    public Complexity visitUnary(UnaryPattern p) {
      return visit(p.getChild());
    }

    public Complexity visitData(DataPattern p) {
      Complexity ret = Complexity.SIMPLE;
      if (p.getParams().size() > 0)
        ret = Complexity.brace(ret);
      if (p.getExcept() != null)
        ret = Complexity.max(ret, visit(p.getExcept()));
      return ret;
    }

    public Complexity visitComposite(CompositePattern p) {
      Complexity ret = Complexity.SIMPLE;
      for (Pattern child : p.getChildren())
        ret = Complexity.max(ret, visit(child));
      return Complexity.paren(ret);
    }

    public Complexity visitPattern(Pattern p) {
      return Complexity.SIMPLE;
    }
  }


  public boolean isComplex(Pattern p) {
    return Complexity.isComplex(complexityVisitor.visit(p));
  }
}
