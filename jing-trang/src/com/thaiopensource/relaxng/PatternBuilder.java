package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import java.util.Hashtable;

import com.thaiopensource.datatype.Datatype;

public class PatternBuilder {
  
  private static final int INIT_SIZE = 256;
  private static final float LOAD_FACTOR = 0.3f;
  private Pattern[] table;
  private int used;
  private int usedLimit;

  private final EmptySequencePattern emptySequence;
  private final EmptyChoicePattern emptyChoice;
  private final AnyStringPattern anyString;

  private final PatternPair emptyPatternPair;
  private final Hashtable eaTable;
  private final Hashtable asrTable;
  private final AnyStringAtom anyStringAtom;
  private final Hashtable ucpTable;
  private final Hashtable rTable = new Hashtable();
  private Atom rAtom = null;

  public PatternBuilder() {
    table = null;
    used = 0;
    usedLimit = 0;
    emptySequence = new EmptySequencePattern();
    emptyChoice = new EmptyChoicePattern();
    anyString = new AnyStringPattern();
    emptyPatternPair = new PatternPair();
    eaTable = new Hashtable();
    asrTable = new Hashtable();
    anyStringAtom = new AnyStringAtom();
    ucpTable = new Hashtable();
  }

  public PatternBuilder(PatternBuilder parent) {
    table = parent.table;
    if (table != null)
      table = (Pattern[])table.clone();
    used = parent.used;
    usedLimit = parent.usedLimit;
    emptySequence = parent.emptySequence;
    emptyChoice = parent.emptyChoice;
    anyString = parent.anyString;
    emptyPatternPair = parent.emptyPatternPair;
    eaTable = (Hashtable)parent.eaTable.clone();
    asrTable = (Hashtable)parent.asrTable.clone();
    anyStringAtom = parent.anyStringAtom;
    ucpTable = (Hashtable)parent.ucpTable.clone();
  }

  Pattern makeEmptySequence() {
    return emptySequence;
  }
  Pattern makeEmptyChoice() {
    return emptyChoice;
  }
  Pattern makeError() {
    return intern(new ErrorPattern());
  }
  Pattern makeSequence(Pattern p1, Pattern p2) {
    if (p1 == emptySequence)
      return p2;
    if (p2 == emptySequence)
      return p1;
    if (p1 == emptyChoice || p2 == emptyChoice)
      return emptyChoice;
    if (p1 instanceof SequencePattern) {
      SequencePattern sp = (SequencePattern)p1;
      return makeSequence(sp.p1, makeSequence(sp.p2, p2));
    }
    return intern(new SequencePattern(p1, p2));
  }
  Pattern makeInterleave(Pattern p1, Pattern p2) {
    if (p1 == emptySequence)
      return p2;
    if (p2 == emptySequence)
      return p1;
    if (p1 == emptyChoice || p2 == emptyChoice)
      return emptyChoice;
    if (p1 instanceof InterleavePattern) {
      InterleavePattern ip = (InterleavePattern)p1;
      return makeInterleave(ip.p1, makeInterleave(ip.p2, p2));
    }
    return intern(new InterleavePattern(p1, p2));
  }
  Pattern makeConcur(Pattern p1, Pattern p2) {
    if (p1 == emptySequence && p2.isNullable())
      return p1;
    if (p2 == emptySequence && p1.isNullable())
      return p2;
    if (p1 == emptyChoice || p2 == emptyChoice)
      return emptyChoice;
    if (p1 instanceof ConcurPattern) {
      ConcurPattern cp = (ConcurPattern)p1;
      return makeConcur(cp.p1, makeConcur(cp.p2, p2));
    }
    if (p1 == p2)
      return p1;
    return intern(new ConcurPattern(p1, p2));
  }
  Pattern makeAnyString() {
    return anyString;
  }
  Pattern makeString(boolean normalizeWhiteSpace, String str, Locator loc) {
    if (normalizeWhiteSpace)
      return intern(new NormalizeStringPattern(str, loc));
    else
      return intern(new PreserveStringPattern(str, loc));
  }
  Pattern makeDatatype(Datatype dt, Locator loc) {
    return intern(new DatatypePattern(dt, loc));
  }

  Pattern makeChoice(Pattern p1, Pattern p2) {
    if (p1 == emptyChoice)
      return p2;
    if (p2 == emptyChoice)
      return p1;
    if (p1 == emptySequence) {
      if (p2.isNullable())
	return p2;
      // Canonicalize position of emptyChoice.
      return makeChoice(p2, p1);
    }
    if (p2 == emptySequence && p1.isNullable())
      return p1;
    if (p1 instanceof ChoicePattern) {
      ChoicePattern cp = (ChoicePattern)p1;
      return makeChoice(cp.p1, makeChoice(cp.p2, p2));
    }
    if (p2.containsChoice(p1))
      return p2;
    return intern(new ChoicePattern(p1, p2));
  }

  Pattern makeOneOrMore(Pattern p) {
    if (p == anyString
	|| p == emptySequence
	|| p == emptyChoice
	|| p instanceof OneOrMorePattern)
      return p;
    return intern(new OneOrMorePattern(p));
  }

  Pattern makeOptional(Pattern p) {
    return makeChoice(p, emptySequence);
  }

  Pattern makeZeroOrMore(Pattern p) {
    return makeOptional(makeOneOrMore(p));
  }

  Pattern makeElement(NameClass nameClass, Pattern content) {
    if (content.isEmptyChoice())
      return content;
    return intern(new ElementPattern(nameClass, content));
  }

  Pattern makeAttribute(NameClass nameClass, Pattern value) {
    if (value.isEmptyChoice())
      return value;
    return intern(new AttributePattern(nameClass, value));
  }

  private Pattern intern(Pattern p) {
    int h;

    if (table == null) {
      table = new Pattern[INIT_SIZE];
      usedLimit = (int)(INIT_SIZE * LOAD_FACTOR);
      h = firstIndex(p);
    }
    else {
      for (h = firstIndex(p); table[h] != null; h = nextIndex(h)) {
	if (p.samePattern(table[h]))
	  return table[h];
      }
    }
    if (used >= usedLimit) {
      // rehash
      Pattern[] oldTable = table;
      table = new Pattern[table.length << 1];
      for (int i = oldTable.length; i > 0;) {
	--i;
	if (oldTable[i] != null) {
	  int j;
	  for (j = firstIndex(oldTable[i]); table[j] != null; j = nextIndex(j))
	    ;
	  table[j] = oldTable[i];
	}
      }
      for (h = firstIndex(p); table[h] != null; h = nextIndex(h))
	;
      usedLimit = (int)(table.length * LOAD_FACTOR);
    }
    used++;
    table[h] = p;
    return p;
  }

  private final int firstIndex(Pattern p) {
    return p.patternHashCode() & (table.length - 1);
  }

  private final int nextIndex(int i) {
    return i == 0 ? table.length - 1 : i - 1;
  }

  static class Key {
    Pattern p;
    String namespaceURI;
    String localName;
    Key(Pattern p, String namespaceURI, String localName) {
      this.p = p;
      this.namespaceURI = namespaceURI;
      this.localName = localName;
    }

    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof Key))
	return false;
      Key other = (Key)obj;
      return (p == other.p
	      && namespaceURI.equals(other.namespaceURI)
	      && localName.equals(other.localName));
    }
    public int hashCode() {
      return p.hashCode() ^ namespaceURI.hashCode() ^ localName.hashCode();
    }
  }

  PatternPair memoizedUnambigContentPattern(Pattern from,
					    String namespaceURI,
					    String localName) {
    Key k = new Key(from, namespaceURI, localName);
    PatternPair tp = (PatternPair)ucpTable.get(k);
    if (tp != null)
      return tp;
    tp = from.unambigContentPattern(this, namespaceURI, localName);
    if (tp == null)
      return null;
    ucpTable.put(k, tp);
    return tp;
  }

  PatternPair makeEmptyPatternPair() {
    return emptyPatternPair;
  }

  Pattern memoizedAnyStringResidual(Pattern p) {
    Pattern r = (Pattern)asrTable.get(p);
    if (r == null) {
      r = memoizedResidual(p, anyStringAtom);
      asrTable.put(p, r);
    }
    return r;
  }

  Pattern memoizedEndAttributes(Pattern p, boolean recovering) {
    if (recovering)
      return p.endAttributes(this, recovering);
    Pattern ea = (Pattern)eaTable.get(p);
    if (ea == null) {
      ea = p.endAttributes(this, false);
      eaTable.put(p, ea);
    }
    return ea;
  }

  Pattern memoizedResidual(Pattern p, Atom a) {
    if (a != rAtom) {
      rTable.clear();
      rAtom = a;
    }
    Pattern r = (Pattern)rTable.get(p);
    if (r == null) {
      r = p.residual(this, a);
      rTable.put(p, r);
    }
    return r;
  }

  void printStats() {
    System.err.println(used + " distinct patterns");
  }
}
