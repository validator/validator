package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.util.Equal;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.ValidationContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class PatternMatcher implements Cloneable, Matcher {

  static private class Shared {
    private final Pattern start;
    private final ValidatorPatternBuilder builder;
    private Map recoverPatternTable;
    Shared(Pattern start, ValidatorPatternBuilder builder) {
      this.start = start;
      this.builder = builder;
    }

    Pattern findElement(Name name) {
      if (recoverPatternTable == null)
        recoverPatternTable = new HashMap();
      Pattern p = (Pattern)recoverPatternTable.get(name);
      if (p == null) {
        p = FindElementFunction.findElement(builder, name, start);
        recoverPatternTable.put(name, p);
      }
      return p;
    }

    PatternMemo fixAfter(PatternMemo p) {
      return builder.getPatternMemo(p.getPattern().applyForPattern(new ApplyAfterFunction(builder) {
        Pattern apply(Pattern p) {
          return builder.makeEmpty();
        }
      }));
    }
  }

  private PatternMemo memo;
  private boolean textTyped;
  private boolean hadError;
  private boolean ignoreNextEndTag;
  private String errorMessage;
  private final Shared shared;

  PatternMatcher(Pattern start, ValidatorPatternBuilder builder) {
    shared = new Shared(start, builder);
    memo = builder.getPatternMemo(start);
  }

  public boolean equals(Object obj) {
    PatternMatcher other = (PatternMatcher)obj;
    if (other == null)
      return false;
    return (memo == other.memo
            && hadError == other.hadError
            && Equal.equal(errorMessage, other.errorMessage)
            && ignoreNextEndTag == other.ignoreNextEndTag
            && textTyped == other.textTyped);
  }

  public int hashCode() {
    return memo.hashCode();
  }

  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new Error("unexpected CloneNotSupportedException");
    }
  }

  public Matcher copy() {
    return (Matcher)clone();
  }

  public boolean matchStartDocument() {
    if (memo.isNotAllowed())
      return error("schema_allows_nothing");
    return true;
  }

  public boolean matchStartTagOpen(Name name) {
    if (setMemo(memo.startTagOpenDeriv(name)))
      return true;
    PatternMemo next = memo.startTagOpenRecoverDeriv(name);
    if (!next.isNotAllowed()) {
      memo = next;
      return error("required_elements_missing");
    }
    ValidatorPatternBuilder builder = shared.builder;
    next = builder.getPatternMemo(builder.makeAfter(shared.findElement(name), memo.getPattern()));
    memo = next;
    return error(next.isNotAllowed() ? "unknown_element" : "out_of_context_element", name);
  }

  public boolean matchAttributeName(Name name) {
    if (setMemo(memo.startAttributeDeriv(name)))
      return true;
    ignoreNextEndTag = true;
    return error("impossible_attribute_ignored", name);
  }

  public boolean matchAttributeValue(Name name, String value, ValidationContext vc) {
    if (ignoreNextEndTag) {
      ignoreNextEndTag = false;
      return true;
    }
    if (setMemo(memo.dataDeriv(value, vc)))
      return true;
    memo = memo.recoverAfter();
    return error("bad_attribute_value", name);
  }

  public boolean matchStartTagClose() {
    boolean ret;
    if (setMemo(memo.endAttributes()))
      ret = true;
    else {
      memo = memo.ignoreMissingAttributes();
      // XXX should specify which attributes
      ret = error("required_attributes_missing");
    }
    textTyped = memo.getPattern().getContentType() == Pattern.DATA_CONTENT_TYPE;
    return ret;
  }

  public boolean matchText(String string, ValidationContext vc, boolean nextTagIsEndTag) {
    if (textTyped && nextTagIsEndTag) {
      ignoreNextEndTag = true;
      return setDataDeriv(string, vc);
    }
    else {
      if (DataDerivFunction.isBlank(string))
        return true;
      return matchUntypedText();
    }
  }

  public boolean matchUntypedText() {
    if (setMemo(memo.mixedTextDeriv()))
      return true;
    return error("text_not_allowed");
  }

  public boolean isTextTyped() {
    return textTyped;
  }

  private boolean setDataDeriv(String string, ValidationContext vc) {
    textTyped = false;
    if (!setMemo(memo.textOnly())) {
      memo = memo.recoverAfter();
      return error("only_text_not_allowed");
    }
    if (setMemo(memo.dataDeriv(string, vc))) {
      ignoreNextEndTag = true;
      return true;
    }
    PatternMemo next = memo.recoverAfter();
    boolean ret = true;
    if (!memo.isNotAllowed()) {
      if (!next.isNotAllowed()
          || shared.fixAfter(memo).dataDeriv(string, vc).isNotAllowed())
        ret = error("string_not_allowed");
    }
    memo = next;
    return ret;
  }

  public boolean matchEndTag(ValidationContext vc) {
    // The tricky thing here is that the derivative that we compute may be notAllowed simply because the parent
    // is notAllowed; we don't want to give an error in this case.
    if (ignoreNextEndTag) {
      ignoreNextEndTag = false;
      return true;
    }
    if (textTyped)
      return setDataDeriv("", vc);
    textTyped = false;
    if (setMemo(memo.endTagDeriv()))
      return true;
    PatternMemo next = memo.recoverAfter();
    if (memo.isNotAllowed()) {
      if (!next.isNotAllowed()
          || shared.fixAfter(memo).endTagDeriv().isNotAllowed()) {
        memo = next;
        return error("unfinished_element");
      }
    }
    memo = next;
    return true;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public boolean isValidSoFar() {
    return !hadError;
  }

  static abstract class PossibleNamesFunction extends AbstractPatternFunction implements NameClassVisitor {
    private Set knownNames;
    private Set possibleNames;

    Set applyTo(Pattern p, Set knownNames) {
      this.knownNames = knownNames;
      this.possibleNames = new HashSet();
      p.apply(this);
      return possibleNames;
    }

    public Object caseAfter(AfterPattern p) {
      return p.getOperand1().apply(this);
    }

    public Object caseBinary(BinaryPattern p) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
      return null;
    }

    public Object caseChoice(ChoicePattern p) {
      return caseBinary(p);
    }

    public Object caseInterleave(InterleavePattern p) {
      return caseBinary(p);
    }

    public Object caseOneOrMore(OneOrMorePattern p) {
      return p.getOperand().apply(this);
    }

    public Object caseOther(Pattern p) {
      return null;
    }

    public void visitChoice(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }

    public void visitNsName(String ns) {
      visitNsNameExcept(ns, null);
    }

    public void visitNsNameExcept(String ns, NameClass nc) {
     if (knownNames == null)
        return;
      boolean addedAll = true;
      for (Iterator iter = knownNames.iterator(); iter.hasNext();) {
        Name name = (Name)iter.next();
        if (!name.getNamespaceUri().equals(ns) || (nc != null && nc.contains(name)))
          addedAll = false;
        else
          possibleNames.add(name);
      }
      if (addedAll)
        knownNames = null;
    }

    public void visitAnyName() {
      if (knownNames == null)
        return;
      possibleNames.addAll(knownNames);
      knownNames = null;
    }

    public void visitAnyNameExcept(NameClass nc) {
      if (knownNames == null)
        return;
      boolean addedAll = true;
      for (Iterator iter = knownNames.iterator(); iter.hasNext();) {
        Name name = (Name)iter.next();
        if (nc.contains(name))
          addedAll = false;
        else
          possibleNames.add(name);
      }
      if (addedAll)
        knownNames = null;
    }

    public void visitName(Name name) {
      possibleNames.add(name);
    }

    public void visitNull() {
    }

    public void visitError() {
    }
  }

  static class PossibleStartTagsFunction extends PossibleNamesFunction {
    public Object caseElement(ElementPattern p) {
      p.getNameClass().accept(this);
      return null;
    }

    public Object caseGroup(GroupPattern p) {
      p.getOperand1().apply(this);
      if (p.getOperand1().isNullable())
        p.getOperand2().apply(this);
      return null;
    }
  }

  static class PossibleAttributesFunction extends PossibleNamesFunction {
    public Object caseAttribute(AttributePattern p) {
      p.getNameClass().accept(this);
      return null;
    }

    public Object caseGroup(GroupPattern p) {
      return caseBinary(p);
    }
  }

  public Set possibleStartTags(Set knownNames) {
    return new PossibleStartTagsFunction().applyTo(memo.getPattern(), knownNames);
  }

  public Set possibleAttributes(Set knownNames) {
    return new PossibleAttributesFunction().applyTo(memo.getPattern(), knownNames);
  }

  private boolean setMemo(PatternMemo m) {
    if (m.isNotAllowed())
      return false;
    else {
      memo = m;
      return true;
    }
  }

  private boolean error(String key) {
    if (hadError && memo.isNotAllowed())
      return true;
    hadError = true;
    errorMessage = SchemaBuilderImpl.localizer.message(key);
    return false;
  }

  private boolean error(String key, Name arg) {
    return error(key, NameFormatter.format(arg));
  }

  private boolean error(String key, String arg) {
    if (hadError && memo.isNotAllowed())
      return true;
    hadError = true;
    errorMessage = SchemaBuilderImpl.localizer.message(key, arg);
    return false;
  }

}
