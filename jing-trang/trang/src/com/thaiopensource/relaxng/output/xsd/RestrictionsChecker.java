package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.output.common.ErrorReporter;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class RestrictionsChecker {
  private final SchemaInfo si;
  private final ErrorReporter er;
  private final Set checkedPatterns = new HashSet();

  private static final int DISALLOW_ELEMENT = 0x1;
  private static final int DISALLOW_ATTRIBUTE = 0x2;
  private static final int DISALLOW_LIST = 0x4;
  private static final int DISALLOW_TEXT = 0x8;
  private static final int DISALLOW_EMPTY = 0x10;
  private static final int DISALLOW_DATA = 0x20;
  private static final int DISALLOW_GROUP = 0x40;
  private static final int DISALLOW_INTERLEAVE = 0x80;
  private static final int DISALLOW_ONE_OR_MORE = 0x100;

  private static final int START_DISALLOW =
          DISALLOW_ATTRIBUTE|DISALLOW_LIST|DISALLOW_TEXT|DISALLOW_DATA|DISALLOW_EMPTY|DISALLOW_GROUP|DISALLOW_INTERLEAVE|DISALLOW_ONE_OR_MORE;
  private static final int LIST_DISALLOW =
          DISALLOW_ATTRIBUTE|DISALLOW_ELEMENT|DISALLOW_TEXT|DISALLOW_LIST|DISALLOW_INTERLEAVE;
  private static final int DATA_EXCEPT_DISALLOW =
          DISALLOW_ATTRIBUTE|DISALLOW_ELEMENT|DISALLOW_LIST|DISALLOW_EMPTY|DISALLOW_TEXT|DISALLOW_GROUP|DISALLOW_INTERLEAVE|DISALLOW_ONE_OR_MORE;
  private static final int ATTRIBUTE_DISALLOW =
          DISALLOW_ATTRIBUTE|DISALLOW_ELEMENT;

  private final PatternVisitor startVisitor = new Visitor("start", START_DISALLOW);
  private final PatternVisitor topLevelVisitor = new ListVisitor(null, 0);
  private final PatternVisitor elementVisitor = new ElementVisitor();
  private final PatternVisitor elementRepeatVisitor = new ElementRepeatVisitor();
  private final PatternVisitor elementRepeatGroupVisitor = new Visitor("element_repeat_group", DISALLOW_ATTRIBUTE);
  private final PatternVisitor elementRepeatInterleaveVisitor  = new Visitor("element_repeat_interleave", DISALLOW_ATTRIBUTE);
  private final PatternVisitor attributeVisitor = new Visitor("attribute", ATTRIBUTE_DISALLOW);
  private final PatternVisitor listVisitor = new ListVisitor("list", LIST_DISALLOW);
  private final PatternVisitor dataExceptVisitor = new Visitor("data_except", DATA_EXCEPT_DISALLOW);

  class Visitor extends AbstractVisitor {
    private final String contextKey;
    private final int flags;

    Visitor(String contextKey, int flags) {
      this.contextKey = contextKey;
      this.flags = flags;
    }

    private boolean checkContext(int flag, String patternName, Pattern p) {
      if ((flags & flag) != 0) {
        er.error("illegal_contains", er.getLocalizer().message(contextKey), patternName, p.getSourceLocation());
        return false;
      }
      else
        return true;
    }

    public Object visitGroup(GroupPattern p) {
      if (checkContext(DISALLOW_GROUP, "group", p)) {
        checkGroup(p);
        super.visitGroup(p);
      }
      return null;
    }

    public Object visitInterleave(InterleavePattern p) {
      if (checkContext(DISALLOW_INTERLEAVE, "interleave", p)) {
        checkGroup(p);
        super.visitInterleave(p);
      }
      return null;
    }

    public Object visitElement(ElementPattern p) {
      if (checkContext(DISALLOW_ELEMENT, "element", p)  && !alreadyChecked(p))
        p.getChild().accept(elementVisitor);
      return null;
    }

    public Object visitAttribute(AttributePattern p) {
      if (checkContext(DISALLOW_ATTRIBUTE, "attribute", p) && !alreadyChecked(p))
        p.getChild().accept(attributeVisitor);
      return null;
    }

    public Object visitData(DataPattern p) {
      if (checkContext(DISALLOW_DATA, "data", p) && !alreadyChecked(p)) {
        Pattern except = p.getExcept();
        if (except != null)
          except.accept(dataExceptVisitor);
      }
      return null;
    }

    public Object visitValue(ValuePattern p) {
      checkContext(DISALLOW_DATA, "value", p);
      return null;
    }

    public Object visitList(ListPattern p) {
     if (checkContext(DISALLOW_LIST, "list", p) && !alreadyChecked(p))
        p.getChild().accept(listVisitor);
      return null;
    }

    public Object visitEmpty(EmptyPattern p) {
      checkContext(DISALLOW_EMPTY, "empty", p);
      return null;
    }

    public Object visitOptional(OptionalPattern p) {
      if (checkContext(DISALLOW_EMPTY, "optional", p))
        super.visitOptional(p);
      return null;
    }

    public Object visitText(TextPattern p) {
      checkContext(DISALLOW_TEXT, "text", p);
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      if (checkContext(DISALLOW_TEXT, "mixed", p)) {
        if (si.getChildType(p.getChild()).contains(ChildType.DATA))
          er.error("mixed_data", p.getSourceLocation());
        super.visitMixed(p);
      }
      return null;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      if (checkContext(DISALLOW_ONE_OR_MORE, "oneOrMore", p)) {
        checkNoDataUnlessInList(p, "oneOrMore");
        super.visitOneOrMore(p);
      }
      return null;
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      if (checkContext(DISALLOW_ONE_OR_MORE, "zeroOrMore", p)) {
        checkNoDataUnlessInList(p, "zeroOrMore");
        super.visitZeroOrMore(p);
      }
      return null;
    }

    public Object visitRef(RefPattern p) {
      return si.getBody(p).accept(this);
    }

    void checkNoDataUnlessInList(UnaryPattern p, String patternName) {
      if (!inList() && si.getChildType(p.getChild()).contains(ChildType.DATA))
        er.error("not_in_list", patternName, p.getSourceLocation());
    }


    void checkGroup(CompositePattern p) {
      int simpleCount = 0;
      boolean hadComplex = false;
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        ChildType ct = si.getChildType((Pattern)iter.next());
        boolean simple = ct.contains(ChildType.DATA);
        boolean complex = ct.contains(ChildType.TEXT) || ct.contains(ChildType.ELEMENT);
        if ((complex && simpleCount > 0) || (simple && hadComplex)) {
          er.error("group_data_other_children",
                   p instanceof GroupPattern ? "group" : "interleave",
                   p.getSourceLocation());
          return;
        }
        if (simple)
          simpleCount++;
        if (complex)
          hadComplex = true;
      }
      if (simpleCount > 1) {
        if (p instanceof InterleavePattern)
          er.error("interleave_data", p.getSourceLocation());
        else if (!inList())
          er.error("group_data", p.getSourceLocation());
      }
    }

    boolean inList() {
      return false;
    }
  }

  class ListVisitor extends Visitor {
    public ListVisitor(String contextKey, int flags) {
      super(contextKey, flags);
    }

    boolean inList() {
      return true;
    }
  }


  class ElementVisitor extends Visitor {
    ElementVisitor() {
      super(null, 0);
    }

    public Object visitAttribute(AttributePattern p) {
      p.getNameClass().accept(this);
      return super.visitAttribute(p);
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return elementRepeatVisitor.visitZeroOrMore(p);
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return elementRepeatVisitor.visitOneOrMore(p);
    }

    public Object visitAnyName(AnyNameNameClass nc) {
      er.error("any_name_attribute_not_repeated", nc.getSourceLocation());
      return null;
    }

    public Object visitNsName(NsNameNameClass nc) {
      er.error("ns_name_attribute_not_repeated", nc.getSourceLocation());
      return null;
    }
  }

  class ElementRepeatVisitor extends Visitor {
    ElementRepeatVisitor() {
      super(null, 0);
    }

    public Object visitGroup(GroupPattern p) {
      return elementRepeatGroupVisitor.visitGroup(p);
    }

    public Object visitInterleave(InterleavePattern p) {
      return elementRepeatInterleaveVisitor.visitInterleave(p);
    }
  }

  class GrammarVisitor implements ComponentVisitor {
    public Object visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return null;
    }

    public Object visitDefine(DefineComponent c) {
      if (c.getName() != DefineComponent.START)
        c.getBody().accept(topLevelVisitor);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      si.getSchema(c.getHref()).componentsAccept(this);
      return null;
    }
  }

  private RestrictionsChecker(SchemaInfo si, ErrorReporter er) {
    this.si = si;
    this.er = er;
    Pattern start = si.getStart();
    if (start != null)
      start.accept(startVisitor);
    si.getGrammar().componentsAccept(new GrammarVisitor());
  }

  static void check(SchemaInfo si, ErrorReporter er) {
    new RestrictionsChecker(si, er);
  }

  private boolean alreadyChecked(Pattern p) {
    if (checkedPatterns.contains(p))
      return true;
    else {
      checkedPatterns.add(p);
      return false;
    }
  }
}
