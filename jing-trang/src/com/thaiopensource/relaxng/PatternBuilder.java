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
  private final NotAllowedPattern notAllowed;
  private final TextPattern text;

  private final PatternPair emptyPatternPair;
  private final Hashtable eaTable;
  private final Hashtable textTable;
  private final TextAtom textAtom;
  private final Hashtable ucpTable;
  private final Hashtable rTable = new Hashtable();
  private Atom rAtom = null;

  public PatternBuilder() {
    table = null;
    used = 0;
    usedLimit = 0;
    emptySequence = new EmptySequencePattern();
    emptyChoice = new EmptyChoicePattern();
    notAllowed = new NotAllowedPattern();
    text = new TextPattern();
    emptyPatternPair = new PatternPair();
    eaTable = new Hashtable();
    textTable = new Hashtable();
    textAtom = new TextAtom();
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
    notAllowed = parent.notAllowed;
    text = parent.text;
    emptyPatternPair = parent.emptyPatternPair;
    eaTable = (Hashtable)parent.eaTable.clone();
    textTable = (Hashtable)parent.textTable.clone();
    textAtom = parent.textAtom;
    ucpTable = (Hashtable)parent.ucpTable.clone();
  }

  Pattern makeEmptySequence() {
    return emptySequence;
  }
  Pattern makeEmptyChoice() {
    return emptyChoice;
  }
  Pattern makeNotAllowed() {
    return notAllowed;
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
  Pattern makeText() {
    return text;
  }
  Pattern makeValue(Datatype dt, Object obj) {
    return intern(new ValuePattern(dt, obj));
  }

  Pattern makeData(Datatype dt) {
    return intern(new DataPattern(dt));
  }

  Pattern makeDataExcept(Datatype dt, Pattern except, Locator loc) {
    return intern(new DataExceptPattern(dt, except, loc));
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
    if (p == text
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

  Pattern makeList(Pattern p, Locator loc) {
    if (p == emptyChoice)
      return p;
    return intern(new ListPattern(p, loc));
  }

  Pattern makeElement(NameClass nameClass, Pattern content, Locator loc) {
    return intern(new ElementPattern(nameClass, content, loc));
  }

  Pattern makeAttribute(NameClass nameClass, Pattern value, Locator loc) {
    if (value.isEmptyChoice())
      return value;
    return intern(new AttributePattern(nameClass, value, loc));
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

  Pattern memoizedTextResidual(Pattern p) {
    Pattern r = (Pattern)textTable.get(p);
    if (r == null) {
      r = memoizedResidual(p, textAtom);
      textTable.put(p, r);
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
