package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;

import java.util.Vector;
import java.util.Hashtable;

import org.relaxng.datatype.ValidationContext;

public class PatternMatcher implements Cloneable {

  static private class Shared {
    private final Pattern start;
    private final ValidatorPatternBuilder builder;
    private Hashtable recoverPatternTable;
    Shared(Pattern start, ValidatorPatternBuilder builder) {
      this.start = start;
      this.builder = builder;
    }

    Pattern findElement(Name name) {
      if (recoverPatternTable == null)
        recoverPatternTable = new Hashtable();
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
  private boolean allNonWhitespaceTextEquivalent;
  private boolean hadError;
  private boolean ignoreNextEndTag;
  private String errorMessage;
  private final Shared shared;

  PatternMatcher(Pattern start, ValidatorPatternBuilder builder) {
    shared = new Shared(start, builder);
    memo = builder.getPatternMemo(start);
  }

  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new Error("unexpected CloneNotSupportedException");
    }
  }

  public PatternMatcher copy() {
    return (PatternMatcher)clone();
  }

  public boolean checkStartDocument() {
    if (memo.isNotAllowed())
      return error("schema_allows_nothing");
    return true;
  }

  public boolean checkStartTagOpen(Name name) {
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

  public boolean checkAttributeName(Name name) {
    if (setMemo(memo.startAttributeDeriv(name)))
      return true;
    ignoreNextEndTag = true;
    return error("impossible_attribute_ignored", name);
  }

  public boolean checkAttributeValue(String value, ValidationContext vc) {
    if (ignoreNextEndTag) {
      ignoreNextEndTag = false;
      return true;
    }
    if (setMemo(memo.dataDeriv(value, vc)))
      return true;
    memo = memo.recoverAfter();
    return error("bad_attribute_value_no_name"); // XXX add to catalog
  }

  public boolean checkStartTagClose() {
    boolean ret;
    if (setMemo(memo.endAttributes()))
      ret = true;
    else {
      memo = memo.ignoreMissingAttributes();
      // XXX should specify which attributes
      ret = error("required_attributes_missing");
    }
    allNonWhitespaceTextEquivalent
            = memo.getPattern().getContentType() != Pattern.DATA_CONTENT_TYPE;
    return ret;
  }


  /**
   * null string means one or more non-whitespace characters; null legal either when mixed
   * is true or when previous call to isAllNonWhitespaceTextEquivalent returned true
   *
   * @param string
   * @param vc
   * @param mixed
   * @return true iff no error
   */
  public boolean checkText(String string, ValidationContext vc, boolean mixed) {
    if (allNonWhitespaceTextEquivalent || mixed) {
      if (string != null && DataDerivFunction.isBlank(string))
        return true;
      if (setMemo(memo.mixedTextDeriv()))
        return true;
      return error("text_not_allowed");
    }
    else {
      if (string == null)
        throw new IllegalStateException();
      ignoreNextEndTag = true;
      return setDataDeriv(string, vc);
    }
  }

  /**
   * Legal when checkText() is legal.
   * @return
   */
  public boolean isAllNonWhitespaceTextEquivalent() {
    return allNonWhitespaceTextEquivalent;
  }

  private boolean setDataDeriv(String string, ValidationContext vc) {
    allNonWhitespaceTextEquivalent = true;
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

  public boolean checkEndTag(ValidationContext vc) {
    // The tricky thing here is that the derivative that we compute may be notAllowed simply because the parent
    // is notAllowed; we don't want to give an error in this case.
    if (ignoreNextEndTag)
      return true;
    if (!allNonWhitespaceTextEquivalent)
      return setDataDeriv("", vc);
    allNonWhitespaceTextEquivalent = true;
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

  // members are of type Name
  public Vector possibleStartTags() {
    // XXX
    return null;
  }

  public Vector possibleAttributes() {
    // XXX
    return null;
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
