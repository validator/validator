package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.output.common.ErrorReporter;

import java.util.HashSet;
import java.util.Set;

public class RestrictionsChecker {
  private final SchemaInfo si;
  private final ErrorReporter er;
  private final Set<Pattern> checkedPatterns = new HashSet<Pattern>();

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

  private final PatternVisitor<VoidValue> startVisitor = new Visitor("start", START_DISALLOW);
  private final PatternVisitor<VoidValue> topLevelVisitor = new ListVisitor(null, 0);
  private final PatternVisitor<VoidValue> elementVisitor = new ElementVisitor();
  private final PatternVisitor<VoidValue> elementRepeatVisitor = new ElementRepeatVisitor();
  private final PatternVisitor<VoidValue> elementRepeatGroupVisitor = new Visitor("element_repeat_group", DISALLOW_ATTRIBUTE);
  private final PatternVisitor<VoidValue> elementRepeatInterleaveVisitor  = new Visitor("element_repeat_interleave", DISALLOW_ATTRIBUTE);
  private final PatternVisitor<VoidValue> attributeVisitor = new Visitor("attribute", ATTRIBUTE_DISALLOW);
  private final PatternVisitor<VoidValue> listVisitor = new ListVisitor("list", LIST_DISALLOW);
  private final PatternVisitor<VoidValue> dataExceptVisitor = new Visitor("data_except", DATA_EXCEPT_DISALLOW);

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

    public VoidValue visitGroup(GroupPattern p) {
      if (checkContext(DISALLOW_GROUP, "group", p)) {
        checkGroup(p);
        super.visitGroup(p);
      }
      return VoidValue.VOID;
    }

    public VoidValue visitInterleave(InterleavePattern p) {
      if (checkContext(DISALLOW_INTERLEAVE, "interleave", p)) {
        checkGroup(p);
        super.visitInterleave(p);
      }
      return VoidValue.VOID;
    }

    public VoidValue visitElement(ElementPattern p) {
      if (checkContext(DISALLOW_ELEMENT, "element", p)  && !alreadyChecked(p))
        p.getChild().accept(elementVisitor);
      return VoidValue.VOID;
    }

    public VoidValue visitAttribute(AttributePattern p) {
      if (checkContext(DISALLOW_ATTRIBUTE, "attribute", p) && !alreadyChecked(p))
        p.getChild().accept(attributeVisitor);
      return VoidValue.VOID;
    }

    public VoidValue visitData(DataPattern p) {
      if (checkContext(DISALLOW_DATA, "data", p) && !alreadyChecked(p)) {
        Pattern except = p.getExcept();
        if (except != null)
          except.accept(dataExceptVisitor);
      }
      return VoidValue.VOID;
    }

    public VoidValue visitValue(ValuePattern p) {
      checkContext(DISALLOW_DATA, "value", p);
      return VoidValue.VOID;
    }

    public VoidValue visitList(ListPattern p) {
     if (checkContext(DISALLOW_LIST, "list", p) && !alreadyChecked(p))
        p.getChild().accept(listVisitor);
      return VoidValue.VOID;
    }

    public VoidValue visitEmpty(EmptyPattern p) {
      checkContext(DISALLOW_EMPTY, "empty", p);
      return VoidValue.VOID;
    }

    public VoidValue visitOptional(OptionalPattern p) {
      if (checkContext(DISALLOW_EMPTY, "optional", p))
        super.visitOptional(p);
      return VoidValue.VOID;
    }

    public VoidValue visitText(TextPattern p) {
      checkContext(DISALLOW_TEXT, "text", p);
      return VoidValue.VOID;
    }

    public VoidValue visitMixed(MixedPattern p) {
      if (checkContext(DISALLOW_TEXT, "mixed", p)) {
        if (si.getChildType(p.getChild()).contains(ChildType.DATA))
          er.error("mixed_data", p.getSourceLocation());
        super.visitMixed(p);
      }
      return VoidValue.VOID;
    }

    public VoidValue visitOneOrMore(OneOrMorePattern p) {
      if (checkContext(DISALLOW_ONE_OR_MORE, "oneOrMore", p)) {
        checkNoDataUnlessInList(p, "oneOrMore");
        super.visitOneOrMore(p);
      }
      return VoidValue.VOID;
    }

    public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
      if (checkContext(DISALLOW_ONE_OR_MORE, "zeroOrMore", p)) {
        checkNoDataUnlessInList(p, "zeroOrMore");
        super.visitZeroOrMore(p);
      }
      return VoidValue.VOID;
    }

    public VoidValue visitRef(RefPattern p) {
      return si.getBody(p).accept(this);
    }

    void checkNoDataUnlessInList(UnaryPattern p, String patternName) {
      if (!inList() && si.getChildType(p.getChild()).contains(ChildType.DATA))
        er.error("not_in_list", patternName, p.getSourceLocation());
    }


    void checkGroup(CompositePattern p) {
      int simpleCount = 0;
      boolean hadComplex = false;
      for (Pattern child : p.getChildren()) {
        ChildType ct = si.getChildType(child);
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

    public VoidValue visitAttribute(AttributePattern p) {
      p.getNameClass().accept(this);
      return super.visitAttribute(p);
    }

    public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
      return elementRepeatVisitor.visitZeroOrMore(p);
    }

    public VoidValue visitOneOrMore(OneOrMorePattern p) {
      return elementRepeatVisitor.visitOneOrMore(p);
    }

    public VoidValue visitAnyName(AnyNameNameClass nc) {
      er.error("any_name_attribute_not_repeated", nc.getSourceLocation());
      return VoidValue.VOID;
    }

    public VoidValue visitNsName(NsNameNameClass nc) {
      er.error("ns_name_attribute_not_repeated", nc.getSourceLocation());
      return VoidValue.VOID;
    }
  }

  class ElementRepeatVisitor extends Visitor {
    ElementRepeatVisitor() {
      super(null, 0);
    }

    public VoidValue visitGroup(GroupPattern p) {
      return elementRepeatGroupVisitor.visitGroup(p);
    }

    public VoidValue visitInterleave(InterleavePattern p) {
      return elementRepeatInterleaveVisitor.visitInterleave(p);
    }
  }

  class GrammarVisitor implements ComponentVisitor<VoidValue> {
    public VoidValue visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return VoidValue.VOID;
    }

    public VoidValue visitDefine(DefineComponent c) {
      if (c.getName() != DefineComponent.START)
        c.getBody().accept(topLevelVisitor);
      return VoidValue.VOID;
    }

    public VoidValue visitInclude(IncludeComponent c) {
      si.getSchema(c.getHref()).componentsAccept(this);
      return VoidValue.VOID;
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
