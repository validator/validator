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
  private boolean textMaybeTyped;
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

  public boolean matchAttributeValue(String value, ValidationContext vc) {
    if (ignoreNextEndTag) {
      ignoreNextEndTag = false;
      return true;
    }
    if (setMemo(memo.dataDeriv(value, vc)))
      return true;
    memo = memo.recoverAfter();
    return error("bad_attribute_value_no_name"); // XXX add to catalog
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
    textMaybeTyped = memo.getPattern().getContentType() == Pattern.DATA_CONTENT_TYPE;
    return ret;
  }

  public boolean matchText(String string, ValidationContext vc, boolean nextTagIsEndTag) {
    if (textMaybeTyped && nextTagIsEndTag) {
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

  /**
   * Legal when matchText() is legal.
   * @return
   */
  public boolean isTextMaybeTyped() {
    return textMaybeTyped;
  }

  private boolean setDataDeriv(String string, ValidationContext vc) {
    textMaybeTyped = false;
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
    if (textMaybeTyped)
      return setDataDeriv("", vc);
    textMaybeTyped = false;
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
