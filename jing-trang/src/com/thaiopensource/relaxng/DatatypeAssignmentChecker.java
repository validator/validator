package com.thaiopensource.relaxng;

import java.util.Vector;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

public class DatatypeAssignmentChecker {
  private PatternBuilder patternBuilder;
  private Pattern currentPattern;
  private Pattern contentPattern;
  private boolean containsText = false;
  private Object contentClass = null;
  private boolean contentAmbig = false;
  private boolean ambig = false;
  private ErrorHandler eh;
  private Vector patterns = new Vector();
  private Vector patternContexts = new Vector();
  private String currentPatternContext;

  static final byte ELEMENT = 0;
  static final byte ATTRIBUTE = 1;
  static final String OTHER = "\u0001";

  static class SAXExceptionWrapper extends RuntimeException {
    private SAXException e;

    SAXExceptionWrapper(SAXException e) {
      this.e = e;
    }

    SAXException getException() {
      return e;
    }
  }

  class VisitorBase implements PatternVisitor, NameClassVisitor {
    public void visitEmptySequence() {
    }

    public void visitEmptyChoice() {
    }

    public void visitError() {
    }

    public void visitSequence(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }

    public void visitInterleave(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }

    public void visitConcur(Pattern p1, Pattern p2) {
      p1.accept(this);
      // p2.accept(this);
    }

    public void visitChoice(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }

    public void visitOneOrMore(Pattern p) {
      p.accept(this);
    }

    public void visitElement(NameClass nc, Pattern content) {
    }

    public void visitAttribute(NameClass ns, Pattern value) {
    }

    public void visitDatatype(Datatype dt) {
    }

    public void visitString(boolean normalizeWhiteSpace, String str) {
    }

    public void visitText() {
    }

    public void visitChoice(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }

    public void visitDifference(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }

    public void visitNot(NameClass nc) {
      nc.accept(this);
    }

    public void visitNsName(String ns) {
    }

    public void visitAnyName() {
    }

    public void visitName(String ns, String localName) {
    }
  }


  class NamesVisitor extends VisitorBase {
    byte type;

    public void visitElement(NameClass nc, Pattern content) {
      type = ELEMENT;
      nc.accept(this);
    }

    public void visitAttribute(NameClass nc, Pattern content) {
      type = ATTRIBUTE;
      nc.accept(this);
    }

    public void visitNsName(String ns) {
      addName(type, ns, OTHER);
    }

    public void visitAnyName() {
      addName(type, OTHER, OTHER);
    }

    public void visitNot(NameClass nc) {
      visitAnyName();
      nc.accept(this);
    }

    public void visitName(String ns, String localName) {
      addName(type, ns, localName);
    }

    public void visitDatatype(Datatype dt) {
      Object cls = dt.getAssignmentClass();
      if (cls == null)
	addText();
      else
	addClass(cls);
    }

    public void visitString(boolean normalizeWhiteSpace, String str) {
      addText();
    }

    public void visitText() {
      addText();
    }
  }

  class CheckNameVisitor extends VisitorBase {
    private byte elementOrAttribute;
    private String ns;
    private String localName;

    CheckNameVisitor(byte elementOrAttribute, String ns, String localName) {
      this.elementOrAttribute = elementOrAttribute;
      this.ns = ns;
      this.localName = localName;
    }

    public void visitElement(NameClass nc, Pattern content) {
      if (elementOrAttribute == ELEMENT && nc.contains(ns, localName))
	combinePattern(content);
    }

    public void visitAttribute(NameClass nc, Pattern content) {
      if (elementOrAttribute == ATTRIBUTE && nc.contains(ns, localName))
	combinePattern(content);
    }

  }

  public DatatypeAssignmentChecker(PatternBuilder patternBuilder,
				   Pattern p,
				   ErrorHandler eh) {
    this.patternBuilder = patternBuilder;
    this.eh = eh;
    combinePattern(p);
    finishPattern("");
  }

  public boolean isAmbig() throws SAXException {
    try {
      for (int i = 0; i < patterns.size(); i++) {
	currentPattern = (Pattern)patterns.elementAt(i);
	currentPatternContext = (String)patternContexts.elementAt(i);
	currentPattern.accept(new NamesVisitor());
	contentAmbig = false;
	contentClass = null;
	containsText = false;
      }
      return ambig;
    }
    catch (SAXExceptionWrapper e) {
      throw e.getException();
    }
  }

  void combinePattern(Pattern p) {
    if (contentPattern == null)
      contentPattern = p;
    else
      contentPattern = patternBuilder.makeChoice(p, contentPattern);
  }

  private void finishPattern(String context) {
    if (contentPattern != null && !patterns.contains(contentPattern)) {
      patterns.addElement(contentPattern);
      patternContexts.addElement(context);
    }
    contentPattern = null;
  }

  void addName(byte elementOrAttribute, String ns, String localName) {
    currentPattern.accept(new CheckNameVisitor(elementOrAttribute,
					       ns,
					       localName));
    String context = currentPatternContext;
    context += "/";
    if (elementOrAttribute == ATTRIBUTE)
      context += "@";
    if (ns == OTHER)
      context += "*:";
    else if (ns.length() != 0)
      context += "{" + ns + "}";
    if (localName == OTHER)
      context += "*";
    else
      context += localName;
    finishPattern(context);
  }

  void addClass(Object cls) {
    if (containsText)
      setAmbig();
    else if (contentClass == null)
      contentClass = cls;
    else if (!contentClass.equals(cls))
      setAmbig();
  }

  void addText() {
    if (contentClass != null)
      setAmbig();
    containsText = true;
  }

  private void setAmbig() {
    if (contentAmbig)
      return;
    contentAmbig = true;
    ambig = true;
    try {
      eh.warning(new SAXParseException(Localizer.message("datatype_assign_ambig",
							 currentPatternContext),
				       null));
    }
    catch (SAXException e) {
      throw new SAXExceptionWrapper(e);
    }
  }
}
