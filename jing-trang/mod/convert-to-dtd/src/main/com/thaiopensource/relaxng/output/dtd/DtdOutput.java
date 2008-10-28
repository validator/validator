package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.AbstractPatternVisitor;
import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Comment;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.common.NameClassSplitter;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.out.CharRepertoire;
import com.thaiopensource.xml.util.Naming;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

class DtdOutput {
  private final boolean warnDatatypes;
  private final String sourceUri;
  private Writer writer;
  private String encoding;
  private CharRepertoire charRepertoire;
  private final int indent;
  private final int lineLength;
  private final String lineSep;
  private final StringBuffer buf = new StringBuffer();
  private final List<ElementPattern> elementQueue = new Vector<ElementPattern>();
  private final List<String> requiredParamEntities = new Vector<String>();
  private final List<String> externallyRequiredParamEntities = new Vector<String>();
  private final Set<String> doneParamEntities = new HashSet<String>();
  private final Set<String> doneIncludes = new HashSet<String>();
  private final Set<String> pendingIncludes = new HashSet<String>();
  private final Analysis analysis;
  private final GrammarPart part;
  private final OutputDirectory od;
  private final ErrorReporter er;
  private final Set<String> reservedEntityNames;

  private final PatternVisitor<VoidValue> topLevelContentModelOutput = new TopLevelContentModelOutput();
  private final AbstractVisitor nestedContentModelOutput = new ContentModelOutput();
  private final PatternVisitor<VoidValue> expandedContentModelOutput = new ExpandedContentModelOutput();
  private final PatternVisitor<VoidValue> groupContentModelOutput = new GroupContentModelOutput();
  private final PatternVisitor<VoidValue> choiceContentModelOutput = new ChoiceContentModelOutput();
  private final PatternVisitor<VoidValue> occurContentModelOutput = new ParenthesizedContentModelOutput();
  private final PatternVisitor<VoidValue> innerElementClassOutput = new InnerElementClassOutput();
  private final PatternVisitor<VoidValue> expandedInnerElementClassOutput = new ExpandedInnerElementClassOutput();
  private final AttributeOutput attributeOutput = new AttributeOutput();
  private final AttributeOutput optionalAttributeOutput = new OptionalAttributeOutput();
  private final PatternVisitor<VoidValue> topLevelSimpleTypeOutput = new TopLevelSimpleTypeOutput();
  private final PatternVisitor<VoidValue> nestedSimpleTypeOutput = new SimpleTypeOutput();
  private final PatternVisitor<VoidValue> valueOutput = new ValueOutput();
  private final GrammarOutput grammarOutput = new GrammarOutput();

  static private final String DTD_URI = "http://www.thaiopensource.com/ns/relaxng/dtd";

  private DtdOutput(boolean warnDatatypes, String sourceUri, Analysis analysis, Set<String> reservedEntityNames, OutputDirectory od, ErrorReporter er) {
    this.warnDatatypes = warnDatatypes;
    this.sourceUri = sourceUri;
    this.analysis = analysis;
    this.reservedEntityNames = reservedEntityNames;
    this.od = od;
    this.er = er;
    this.part = analysis.getGrammarPart(sourceUri);
    try {
      OutputDirectory.Stream stream = od.open(sourceUri, analysis.getEncoding(sourceUri));
      this.encoding = stream.getEncoding();
      this.writer = stream.getWriter();
      this.charRepertoire = stream.getCharRepertoire();
    }
    catch (IOException e) {
      throw new WrappedIOException(e);
    }
    this.lineSep = od.getLineSeparator();
    this.indent = od.getIndent();
    this.lineLength = od.getLineLength();
  }

  class ParenthesizedContentModelOutput extends AbstractPatternVisitor<VoidValue> {
    public VoidValue visitPattern(Pattern p) {
      buf.append('(');
      p.accept(nestedContentModelOutput);
      buf.append(')');
      return VoidValue.VOID;
    }

    public VoidValue visitRef(RefPattern p) {
      Pattern def = getBody(p.getName());
      if (getContentType(def) == ContentType.DIRECT_SINGLE_ELEMENT)
        ((ElementPattern)def).getNameClass().accept(nestedContentModelOutput);
      else
        visitPattern(p);
      return VoidValue.VOID;
    }

    public VoidValue visitElement(ElementPattern p) {
      if (getContentType(p) == ContentType.DIRECT_SINGLE_ELEMENT) {
        p.getNameClass().accept(nestedContentModelOutput);
        elementQueue.add(p);
      }
      else
        visitPattern(p);
      return VoidValue.VOID;
    }
  }

  class ChoiceContentModelOutput extends ParenthesizedContentModelOutput {
    public VoidValue visitOptional(OptionalPattern p) {
      p.accept(nestedContentModelOutput);
      return VoidValue.VOID;
    }

    public VoidValue visitOneOrMore(OneOrMorePattern p) {
      p.accept(nestedContentModelOutput);
      return VoidValue.VOID;
    }

    public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
      p.accept(nestedContentModelOutput);
      return VoidValue.VOID;
    }

  }

  private class GroupContentModelOutput extends ChoiceContentModelOutput {
    public VoidValue visitGroup(GroupPattern p) {
      p.accept(nestedContentModelOutput);
      return VoidValue.VOID;
    }
  }

  class ContentModelOutput extends AbstractVisitor {
    public VoidValue visitName(NameNameClass nc) {
      String ns = nc.getNamespaceUri();
      if (!ns.equals("") && !ns.equals(analysis.getDefaultNamespaceUri()) && ns != NameClass.INHERIT_NS)
        buf.append(analysis.getPrefixForNamespaceUri(ns)).append(':');
      buf.append(nc.getLocalName());
      return VoidValue.VOID;
    }

    public VoidValue visitChoice(ChoiceNameClass nc) {
      List<NameClass> list = nc.getChildren();
      boolean needSep = false;
      for (int i = 0, len = list.size(); i < len; i++) {
        if (needSep)
          buf.append('|');
        else
          needSep = true;
        list.get(i).accept(this);
      }
      return VoidValue.VOID;
    }

    public VoidValue visitElement(ElementPattern p) {
      p.getNameClass().accept(this);
      elementQueue.add(p);
      return VoidValue.VOID;
    }

    public VoidValue visitRef(RefPattern p) {
      Pattern def = getBody(p.getName());
      if (getContentType(def) == ContentType.DIRECT_SINGLE_ELEMENT)
        ((ElementPattern)def).getNameClass().accept(this);
      else
        paramEntityRef(p);
      return VoidValue.VOID;
    }

    public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
      p.getChild().accept(occurContentModelOutput);
      buf.append('*');
      return VoidValue.VOID;
    }

    public VoidValue visitOneOrMore(OneOrMorePattern p) {
      p.getChild().accept(occurContentModelOutput);
      buf.append('+');
      return VoidValue.VOID;
    }

    public VoidValue visitOptional(OptionalPattern p) {
      p.getChild().accept(occurContentModelOutput);
      buf.append('?');
      return VoidValue.VOID;
    }

    public VoidValue visitText(TextPattern p) {
      buf.append("#PCDATA");
      return VoidValue.VOID;
    }

    public VoidValue visitMixed(MixedPattern p) {
      buf.append("#PCDATA");
      return VoidValue.VOID;
    }

    public VoidValue visitGroup(GroupPattern p) {
      List<Pattern> list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      for (int i = 0; i < len; i++) {
        Pattern member = list.get(i);
        ContentType t = getContentType(member);
        if (!t.isA(ContentType.EMPTY)) {
          if (needSep)
            buf.append(',');
          else
            needSep = true;
          member.accept(groupContentModelOutput);
        }
      }
      return VoidValue.VOID;
    }

    public VoidValue visitInterleave(InterleavePattern p) {
      ContentType ct = getContentType(p);
      if (ct == ContentType.INTERLEAVE_ZERO_OR_MORE_ELEMENT_CLASS || ct == ContentType.INTERLEAVE_MIXED_MODEL) {
        buf.append('(');
        p.accept(innerElementClassOutput);
        buf.append(')');
        buf.append('*');
      }
      else {
        final List<Pattern> list = p.getChildren();
        for (int i = 0, len = list.size(); i < len; i++) {
          Pattern member = list.get(i);
          ContentType t = getContentType(member);
          if (!t.isA(ContentType.EMPTY))
            member.accept(this);
        }
      }
      return VoidValue.VOID;
    }

    public VoidValue visitChoice(ChoicePattern p) {
      List<Pattern> list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      if (getContentType(p).isA(ContentType.MIXED_ELEMENT_CLASS)) {
        for (int i = 0; i < len; i++) {
          Pattern member = list.get(i);
          if (getContentType(member).isA(ContentType.MIXED_ELEMENT_CLASS)) {
            member.accept(nestedContentModelOutput);
            needSep = true;
            break;
          }
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = list.get(i);
        ContentType t = getContentType(member);
        if (t != ContentType.NOT_ALLOWED && t != ContentType.EMPTY && !t.isA(ContentType.MIXED_ELEMENT_CLASS)) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          member.accept(!t.isA(ContentType.ELEMENT_CLASS) ? choiceContentModelOutput : nestedContentModelOutput);
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = list.get(i);
        ContentType t = getContentType(member);
        if (t == ContentType.NOT_ALLOWED) {
          if (needSep)
            buf.append(' ');
          else
            needSep = true;
          member.accept(nestedContentModelOutput);
        }
      }
      return VoidValue.VOID;
    }

    public VoidValue visitGrammar(GrammarPattern p) {
      return getBody(DefineComponent.START).accept(this);
    }
  }

  class TopLevelContentModelOutput extends ContentModelOutput {
    public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
      buf.append('(');
      p.getChild().accept(nestedContentModelOutput);
      buf.append(')');
      buf.append('*');
      return VoidValue.VOID;
    }

    public VoidValue visitOneOrMore(OneOrMorePattern p) {
      buf.append('(');
      p.getChild().accept(nestedContentModelOutput);
      buf.append(')');
      buf.append('+');
      return VoidValue.VOID;
    }

    public VoidValue visitOptional(OptionalPattern p) {
      buf.append('(');
      p.getChild().accept(nestedContentModelOutput);
      buf.append(')');
      buf.append('?');
      return VoidValue.VOID;
    }

    public VoidValue visitElement(ElementPattern p) {
      buf.append('(');
      super.visitElement(p);
      buf.append(')');
      return VoidValue.VOID;
    }

    public VoidValue visitRef(RefPattern p) {
      ContentType t = getContentType(p);
      if (t.isA(ContentType.MIXED_MODEL))
        super.visitRef(p);
      else {
        buf.append('(');
        super.visitRef(p);
        buf.append(')');
      }
      return VoidValue.VOID;
    }

    public VoidValue visitChoice(ChoicePattern p) {
      buf.append('(');
      p.accept(nestedContentModelOutput);
      buf.append(')');
      return VoidValue.VOID;
    }

    public VoidValue visitText(TextPattern p) {
      buf.append('(');
      p.accept(nestedContentModelOutput);
      buf.append(')');
      return VoidValue.VOID;
    }

    public VoidValue visitMixed(MixedPattern p) {
      buf.append('(');
      if (getContentType(p.getChild()) == ContentType.EMPTY)
        buf.append("#PCDATA)");
      else {
        buf.append("#PCDATA|");
        p.getChild().accept(innerElementClassOutput);
        buf.append(')');
        buf.append('*');
      }
      return VoidValue.VOID;
    }

    public VoidValue visitGroup(GroupPattern p) {
      List<Pattern> list = p.getChildren();
      Pattern main = null;
      for (int i = 0, len = list.size(); i < len; i++) {
        Pattern member = list.get(i);
        if (!getContentType(member).isA(ContentType.EMPTY)) {
          if (main == null)
            main = member;
          else {
            buf.append('(');
            nestedContentModelOutput.visitGroup(p);
            buf.append(')');
            return VoidValue.VOID;
          }
        }
      }
      if (main != null)
        main.accept(this);
      return VoidValue.VOID;
    }
  }

  class ExpandedContentModelOutput extends ContentModelOutput {
    public VoidValue visitElement(ElementPattern p) {
      p.getNameClass().accept(this);
      return VoidValue.VOID;
    }
  }

  class PatternOutput extends AbstractPatternVisitor<VoidValue> {
    public VoidValue visitPattern(Pattern p) {
      return VoidValue.VOID;
    }
  }

  class InnerElementClassOutput extends PatternOutput {
    public VoidValue visitRef(RefPattern p) {
      getBody(p.getName()).accept(expandedInnerElementClassOutput);
      return VoidValue.VOID;
    }

    public VoidValue visitComposite(CompositePattern p) {
      List<Pattern> list = p.getChildren();
      boolean needSep = false;
      int doneIndex = -1;
      for (int i = 0, len = list.size(); i < len; i++) {
        Pattern member = list.get(i);
        ContentType ct = getContentType(member);
        if (ct.isA(ContentType.MIXED_MODEL) || ct == ContentType.TEXT) {
          member.accept(this);
          needSep = true;
          doneIndex = i;
          break;
        }
      }
      for (int i = 0, len = list.size(); i < len; i++) {
        if (i != doneIndex) {
          Pattern member = list.get(i);
          if (getContentType(member) != ContentType.EMPTY) {
            if (needSep)
              buf.append('|');
            else
              needSep = true;
            member.accept(this);
          }
        }
      }
      return VoidValue.VOID;
    }

    public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
      p.getChild().accept(nestedContentModelOutput);
      return VoidValue.VOID;
    }

    public VoidValue visitMixed(MixedPattern p) {
      if (getContentType(p.getChild()) == ContentType.EMPTY)
        buf.append("#PCDATA");
      else {
        buf.append("#PCDATA|");
        p.getChild().accept(this);
      }
      return VoidValue.VOID;
    }

    public VoidValue visitText(TextPattern p) {
      buf.append("#PCDATA");
      return VoidValue.VOID;
    }
  }

  class ExpandedInnerElementClassOutput extends InnerElementClassOutput {
    public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
      p.getChild().accept(expandedContentModelOutput);
      return VoidValue.VOID;
    }
  }

  class AttributeOutput extends PatternOutput {
    void output(Pattern p) {
      if (getAttributeType(p) != AttributeType.EMPTY)
        p.accept(this);
    }

    void newlineIndent() {
      buf.append(lineSep);
      for (int i = 0; i < indent; i++)
        buf.append(' ');
    }

    public VoidValue visitComposite(CompositePattern p) {
      List<Pattern> list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        output(list.get(i));
      return VoidValue.VOID;
    }

    public VoidValue visitMixed(MixedPattern p) {
      output(p.getChild());
      return VoidValue.VOID;
    }

    public VoidValue visitOneOrMore(OneOrMorePattern p) {
      output(p.getChild());
      return VoidValue.VOID;
    }

    public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
      if (getAttributeType(p) != AttributeType.SINGLE)
        er.warning("attribute_occur_approx", p.getSourceLocation());
      optionalAttributeOutput.output(p.getChild());
      return VoidValue.VOID;
    }

    public VoidValue visitRef(RefPattern p) {
      ContentType t = getContentType(p);
      if (t.isA(ContentType.EMPTY) && isRequired()) {
        if (analysis.getParamEntityElementName(p.getName()) == null) {
          newlineIndent();
          paramEntityRef(p);
        }
      }
      else
        output(getBody(p.getName()));
      return VoidValue.VOID;
    }

    public VoidValue visitAttribute(AttributePattern p) {
      ContentType ct = getContentType(p.getChild());
      if (ct == ContentType.NOT_ALLOWED)
        return VoidValue.VOID;
      List<NameNameClass> names = NameClassSplitter.split(p.getNameClass());
      int len = names.size();
      if (len > 1)
        er.warning("attribute_occur_approx", p.getSourceLocation());
      for (int i = 0; i < len; i++) {
        int start = buf.length();
        newlineIndent();
        NameNameClass nnc = names.get(i);
        String ns = nnc.getNamespaceUri();

        if (!ns.equals("") && ns != NameClass.INHERIT_NS) {
          String prefix = analysis.getPrefixForNamespaceUri(ns);
          buf.append(prefix);
          buf.append(':');
        }
        buf.append(nnc.getLocalName());
        buf.append(' ');
        if (ct == ContentType.VALUE)
          p.getChild().accept(valueOutput);
        else {
          int typeStart = buf.length();
          if (ct.isA(ContentType.SIMPLE_TYPE) || ct == ContentType.TEXT)
            p.getChild().accept(topLevelSimpleTypeOutput);
          else if (ct == ContentType.EMPTY) {
            er.warning("empty_attribute_approx", p.getSourceLocation());
            buf.append("CDATA");
          }
          int typeEnd = buf.length();
          if (isRequired() && len == 1)
            buf.append(" #REQUIRED");
          else {
            String dv = getDefaultValue(p);
            if (dv == null)
              buf.append(" #IMPLIED");
            else {
              buf.append(' ');
              attributeValueLiteral(dv);
            }
          }
          int lineStart = start + lineSep.length();
          if (buf.length() - lineStart > lineLength && ct.isA(ContentType.ENUM)) {
            ModelBreaker breaker = new ModelBreaker(buf.substring(lineStart, typeStart),
                                                    buf.substring(typeStart, typeEnd),
                                                    buf.substring(typeEnd),
                                                    lineLength);
            buf.setLength(start);
            while (breaker.hasNextLine()) {
              buf.append(lineSep);
              buf.append(breaker.nextLine());
            }
          }
        }
      }
      return VoidValue.VOID;
    }

    boolean isRequired() {
      return true;
    }

    public VoidValue visitChoice(ChoicePattern p) {
      if (getAttributeType(p) != AttributeType.EMPTY)
        er.warning("attribute_occur_approx", p.getSourceLocation());
      optionalAttributeOutput.visitComposite(p);
      return VoidValue.VOID;
    }

    public VoidValue visitOptional(OptionalPattern p) {
      if (getAttributeType(p) != AttributeType.SINGLE)
        er.warning("attribute_occur_approx", p.getSourceLocation());
      optionalAttributeOutput.output(p.getChild());
      return VoidValue.VOID;
    }
  }

  class OptionalAttributeOutput extends AttributeOutput {
    boolean isRequired() {
      return false;
    }
  }

  class SimpleTypeOutput extends PatternOutput {
    public VoidValue visitText(TextPattern p) {
      buf.append("CDATA");
      return VoidValue.VOID;
    }

    public VoidValue visitValue(ValuePattern p) {
      buf.append(p.getValue());
      return VoidValue.VOID;
    }

    public VoidValue visitRef(RefPattern p) {
      paramEntityRef(p);
      return VoidValue.VOID;
    }

    public VoidValue visitData(DataPattern p) {
      Datatypes.Info info = Datatypes.getInfo(p.getDatatypeLibrary(), p.getType());
      if (info == null) {
        er.warning("unrecognized_datatype", p.getSourceLocation());
        buf.append("CDATA");
      }
      else {
        if (warnDatatypes) {
          if (!info.isExact())
            er.warning("datatype_approx", p.getType(), info.closestType(), p.getSourceLocation());
          else {
            for (Param param : p.getParams())
              er.warning("ignore_param", param.getName(), p.getType(), p.getSourceLocation());
            if (p.getExcept() != null)
              er.warning("ignore_except", p.getType(), p.getSourceLocation());
          }
        }
        buf.append(info.closestType());
      }
      return VoidValue.VOID;
    }

    public VoidValue visitChoice(ChoicePattern p) {
      List<Pattern> list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      for (int i = 0; i < len; i++) {
        Pattern member = list.get(i);
        ContentType t = getContentType(member);
        if (t != ContentType.NOT_ALLOWED) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          member.accept(this);
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = list.get(i);
        ContentType t = getContentType(member);
        if (t == ContentType.NOT_ALLOWED) {
          if (needSep)
            buf.append(' ');
          else
            needSep = true;
          member.accept(this);
        }
      }
      return VoidValue.VOID;
    }
  }

  class TopLevelSimpleTypeOutput extends SimpleTypeOutput {
    public VoidValue visitList(ListPattern p) {
      if (warnDatatypes)
        er.warning("list_approx", p.getSourceLocation());
      buf.append("CDATA");
      return VoidValue.VOID;
    }

    public VoidValue visitValue(ValuePattern p) {
      if (getContentType(p) == ContentType.ENUM) {
        buf.append('(');
        super.visitValue(p);
        buf.append(')');
      }
      else {
        Datatypes.Info info = Datatypes.getInfo(p.getDatatypeLibrary(), p.getType());
        if (info == null) {
          er.warning("unrecognized_datatype", p.getSourceLocation());
          buf.append("CDATA");
        }
        else {
          String type = info.closestType();
          if (warnDatatypes)
            er.warning("value_approx", type, p.getSourceLocation());
          buf.append(type);
        }
      }
      return VoidValue.VOID;
    }

    public VoidValue visitChoice(ChoicePattern p) {
      ContentType t = getContentType(p);
      if (t == ContentType.ENUM) {
        buf.append('(');
        nestedSimpleTypeOutput.visitChoice(p);
        buf.append(')');
      }
      else if (t == ContentType.SIMPLE_TYPE_CHOICE) {
        if (warnDatatypes)
          er.warning("datatype_choice_approx", p.getSourceLocation());
        buf.append("CDATA");
      }
      else
        super.visitChoice(p);
      return VoidValue.VOID;
    }

    public VoidValue visitRef(RefPattern p) {
      ContentType t = getContentType(p);
      if (t == ContentType.ENUM) {
        buf.append('(');
        super.visitRef(p);
        buf.append(')');
      }
      else if (t == ContentType.TEXT)
        buf.append("CDATA");
      else
        super.visitRef(p);
      return VoidValue.VOID;
    }

  }

  private class ValueOutput extends PatternOutput {
    public VoidValue visitValue(ValuePattern p) {
      buf.append("CDATA #FIXED ");
      attributeValueLiteral(p.getValue());
      return VoidValue.VOID;
    }

    public VoidValue visitRef(RefPattern p) {
      paramEntityRef(p);
      return VoidValue.VOID;
    }
  }

  class GrammarOutput implements ComponentVisitor<VoidValue> {
    public void visitContainer(Container c) {
      final List<Component> list = c.getComponents();
      for (int i = 0, len = list.size(); i < len; i++)
        (list.get(i)).accept(this);
    }

    public VoidValue visitDiv(DivComponent c) {
      outputLeadingComments(c);
      outputInitialChildComments(c);
      visitContainer(c);
      outputFollowingComments(c);
      return VoidValue.VOID;
    }

    public VoidValue visitDefine(DefineComponent c) {
      if (c.getName() == DefineComponent.START) {
        outputLeadingComments(c);
        outputFollowingComments(c);
        if (analysis.getPattern() == analysis.getGrammarPattern())
          c.getBody().accept(nestedContentModelOutput);
      }
      else {
        if (getContentType(c.getBody()) == ContentType.DIRECT_SINGLE_ELEMENT)
          outputElement((ElementPattern)c.getBody(), c);
        else if (!doneParamEntities.contains(c.getName())) {
          doneParamEntities.add(c.getName());
          outputParamEntity(c);
        }
      }
      outputQueuedElements();
      return VoidValue.VOID;
    }

    public VoidValue visitInclude(IncludeComponent c) {
      outputInclude(c);
      return VoidValue.VOID;
    }
  }

  void outputQueuedElements() {
    for (int i = 0; i < elementQueue.size(); i++)
      outputElement(elementQueue.get(i), null);
    elementQueue.clear();
  }

  static void output(boolean warnDatatypes, Analysis analysis, OutputDirectory od, ErrorReporter er) throws IOException {
    try {
      new DtdOutput(warnDatatypes, analysis.getMainUri(), analysis, new HashSet<String>(), od, er).topLevelOutput();
    }
    catch (WrappedIOException e) {
      throw e.cause;
    }
  }

  void topLevelOutput() {
    GrammarPattern grammarPattern = analysis.getGrammarPattern();
    xmlDecl();
    Pattern p = analysis.getPattern();
    if (p != grammarPattern) {
      outputLeadingComments(p);
      p.accept(nestedContentModelOutput);
      outputQueuedElements();
    }
    if (grammarPattern != null) {
      outputLeadingComments(grammarPattern);
      outputInitialChildComments(grammarPattern);
      grammarOutput.visitContainer(grammarPattern);
      outputFollowingComments(grammarPattern);
    }
    close();
  }

  void subOutput(GrammarPattern grammarPattern) {
    xmlDecl();
    outputLeadingComments(grammarPattern);
    outputInitialChildComments(grammarPattern);
    grammarOutput.visitContainer(grammarPattern);
    outputFollowingComments(grammarPattern);
    close();
  }

  void xmlDecl() {
    write("<?xml encoding=\"");
    write(encoding);
    write("\"?>");
    outputNewline();
  }

  ContentType getContentType(Pattern p) {
    return analysis.getContentType(p);
  }

  AttributeType getAttributeType(Pattern p) {
    return analysis.getAttributeType(p);
  }

  Pattern getBody(String name) {
    return analysis.getBody(name);
  }

  void paramEntityRef(RefPattern p) {
    String name = p.getName();
    buf.append('%');
    buf.append(name);
    buf.append(';');
    requiredParamEntities.add(name);
  }

  void attributeValueLiteral(String value) {
    buf.append('\'');
    for (int i = 0, len = value.length(); i < len; i++) {
      char c = value.charAt(i);
      switch (c) {
      case '<':
        buf.append("&lt;");
        break;
      case '&':
        buf.append("&amp;");
        break;
      case '\'':
        buf.append("&apos;");
        break;
      case '"':
        buf.append("&quot;");
        break;
      case '\r':
        buf.append("&#xD;");
        break;
      case '\n':
        buf.append("&#xA;");
        break;
      case '\t':
        buf.append("&#9;");
        break;
      default:
        buf.append(c);
        break;
      }
    }
    buf.append('\'');
  }

  void outputRequiredComponents() {
    for (String name : requiredParamEntities) {
      Component c = part.getWhereProvided(name);
      if (c == null)
        externallyRequiredParamEntities.add(name);
      else if (c instanceof DefineComponent) {
        if (!doneParamEntities.contains(name)) {
          doneParamEntities.add(name);
          outputParamEntity((DefineComponent)c);
        }
      }
      else
        outputInclude((IncludeComponent)c);
    }
    requiredParamEntities.clear();
  }

  void outputInclude(IncludeComponent inc) {
    String href = inc.getHref();
    if (doneIncludes.contains(href))
      return;
    if (pendingIncludes.contains(href)) {
      er.error("sorry_include_depend", inc.getSourceLocation());
      return;
    }
    pendingIncludes.add(href);
    DtdOutput sub = new DtdOutput(warnDatatypes, href, analysis, reservedEntityNames, od, er);
    GrammarPattern g = (GrammarPattern)analysis.getSchema(href);
    sub.subOutput(g);
    requiredParamEntities.addAll(sub.externallyRequiredParamEntities);
    outputRequiredComponents();
    outputLeadingComments(inc);
    String entityName = genEntityName(inc);
    outputNewline();
    write("<!ENTITY % ");
    write(entityName);
    write(" SYSTEM ");
    write('"');
    // XXX deal with " in filename (is it allowed by URI syntax?)
    write(od.reference(sourceUri, href));
    write('"');
    write('>');
    outputNewline();
    write('%');
    write(entityName);
    write(';');
    outputNewline();
    outputFollowingComments(inc);
    doneIncludes.add(href);
    pendingIncludes.remove(href);
  }

  String genEntityName(IncludeComponent inc) {
    String entityName = getAttributeAnnotation(inc, DTD_URI, "entityName");
    if (entityName != null) {
      entityName = entityName.trim();
      if (!Naming.isNcname(entityName)) {
        er.warning("entity_name_not_ncname", entityName, inc.getSourceLocation());
        entityName = null;
      }
    }
    if (entityName == null) {
      String uri = inc.getHref();
      int slash = uri.lastIndexOf('/');
      if (slash >= 0)
        uri = uri.substring(slash + 1);
      int dot = uri.lastIndexOf('.');
      if (dot > 0)
        uri = uri.substring(0, dot);
      if (Naming.isNcname(uri))
        entityName = uri;
    }
    if (entityName == null)
      entityName = "ent";
    if (!reserveEntityName(entityName)) {
      for (int i = 1;; i++) {
        String tem = entityName + Integer.toString(i);
        if (reserveEntityName(tem)) {
          entityName = tem;
          break;
        }
      }
    }
    return entityName;
  }

  private boolean reserveEntityName(String name) {
    if (reservedEntityNames.contains(name))
      return false;
    reservedEntityNames.add(name);
    return true;
  }

  void outputParamEntity(DefineComponent def) {
    String name = def.getName();
    Pattern body = def.getBody();
    ContentType t = getContentType(body);
    buf.setLength(0);
    boolean wrap = true;
    if (t.isA(ContentType.MODEL_GROUP) || t.isA(ContentType.NOT_ALLOWED) || t.isA(ContentType.MIXED_ELEMENT_CLASS))
      body.accept(nestedContentModelOutput);
    else if (t.isA(ContentType.MIXED_MODEL))
      body.accept(topLevelContentModelOutput);
    else if (t.isA(ContentType.EMPTY)) {
      attributeOutput.output(body);
      wrap = false;
    }
    else if (t.isA(ContentType.ENUM))
      body.accept(nestedSimpleTypeOutput);
    else if (t.isA(ContentType.VALUE)) {
      body.accept(valueOutput);
      wrap = false;
    }
    else if (t.isA(ContentType.SIMPLE_TYPE))
      body.accept(topLevelSimpleTypeOutput);
    String replacement = buf.toString();
    outputRequiredComponents();
    outputLeadingComments(def);
    String elementName = analysis.getParamEntityElementName(name);
    if (elementName != null) {
      if (replacement.length() > 0) {
        outputNewline();
        write("<!ATTLIST ");
        write(elementName);
        outputAttributeNamespaces(body);
        write(replacement);
        write('>');
        outputNewline();
      }
    }
    else {
      doneParamEntities.add(name);
      outputNewline();
      String prefix = "<!ENTITY % " + name + " \"";
      String suffix = "\">";
      if (!wrap) {
        write(prefix);
        write(replacement);
        write(suffix);
        outputNewline();
      }
      else
        outputModelBreak(prefix, replacement, suffix);
    }
    outputFollowingComments(def);
  }

  private void outputModelBreak(String prefix, String replacement, String suffix) {
    for (ModelBreaker breaker = new ModelBreaker(prefix, replacement, suffix, lineLength); breaker.hasNextLine();) {
      write(breaker.nextLine());
      outputNewline();
    }
  }

  void outputElement(ElementPattern p, Annotated def) {
    buf.setLength(0);
    Pattern content = p.getChild();
    ContentType ct = getContentType(content);
    if (ct == ContentType.EMPTY)
      ;
    else if (ct == ContentType.MIXED_ELEMENT_CLASS) {
      er.warning("mixed_choice_approx", p.getSourceLocation());
      buf.append("(");
      content.accept(nestedContentModelOutput);
      buf.append(")*");
    }
    else if (ct.isA(ContentType.SIMPLE_TYPE)) {
      if (warnDatatypes)
        er.warning("data_content_approx", p.getSourceLocation());
      buf.append("(#PCDATA)");
    }
    else if (ct == ContentType.NOT_ALLOWED)
      return; // leave it undefined
    else
      content.accept(topLevelContentModelOutput);
    String contentModel = buf.length() == 0 ? "EMPTY" : buf.toString();
    buf.setLength(0);
    attributeOutput.output(content);
    String atts = buf.toString();
    outputRequiredComponents();
    if (def != null)
      outputLeadingComments(def);
    outputLeadingComments(p);
    List<NameNameClass> names = NameClassSplitter.split(p.getNameClass());
    for (NameNameClass name : names) {
      String ns = name.getNamespaceUri();
      String qName;
      String prefix;
      if (ns.equals("") || ns.equals(analysis.getDefaultNamespaceUri()) || ns == NameClass.INHERIT_NS) {
        qName = name.getLocalName();
        prefix = null;
      }
      else {
        prefix = analysis.getPrefixForNamespaceUri(ns);
        qName = prefix + ":" + name.getLocalName();
      }
      outputNewline();
      outputModelBreak("<!ELEMENT " + qName + " ", contentModel, ">");
      boolean needXmlns;
      if (ns == NameClass.INHERIT_NS)
        needXmlns = false;
      else if (prefix == null)
        needXmlns = true;
      else
        needXmlns = !analysis.getAttributeNamespaces(content).contains(ns);
      if (atts.length() != 0 || needXmlns) {
        write("<!ATTLIST ");
        write(qName);
        if (needXmlns) {
          outputNewline();
          outputIndent();
          if (prefix != null) {
            write("xmlns:");
            write(prefix);
          }
          else
            write("xmlns");
          write(" CDATA #FIXED ");
          buf.setLength(0);
          attributeValueLiteral(ns);
          write(buf.toString());
        }
        if (atts.length() != 0)
          outputAttributeNamespaces(content);
        write(atts);
        write('>');
        outputNewline();
      }
    }
    if (def != null)
      outputFollowingComments(def);
  }

  void outputAttributeNamespaces(Pattern p) {
    Set<String> namespaces = analysis.getAttributeNamespaces(p);
    for (String ns : namespaces) {
      String prefix = analysis.getPrefixForNamespaceUri(ns);
      outputNewline();
      outputIndent();
      write("xmlns:");
      write(prefix);
      write(" CDATA #FIXED ");
      buf.setLength(0);
      attributeValueLiteral(ns);
      write(buf.toString());
    }
  }

  void outputLeadingComments(Annotated a) {
    outputComments(a.getLeadingComments());
  }

  void outputInitialChildComments(Annotated a) {
    outputComments(a.getChildElementAnnotations());
  }

  void outputFollowingComments(Annotated a) {
    outputComments(a.getFollowingElementAnnotations());
  }

  void outputComments(List<? extends AnnotationChild> list) {
    for (AnnotationChild child : list)
      if (child instanceof Comment)
        outputComment(((Comment)child).getValue());
  }

  void outputComment(String value) {
    outputNewline();
    write("<!--");
    int start = 0;
    for (;;) {
      int i = value.indexOf('\n', start);
      if (i < 0) {
        if (start == 0) {
          write(' ');
          write(value);
          write(' ');
        }
        else {
          outputNewline();
          write(value.substring(start));
          outputNewline();
        }
        break;
      }
      outputNewline();
      write(value.substring(start, i));
      start = i + 1;
    }
    write("-->");
    outputNewline();
  }

  void outputIndent() {
    for (int i = 0; i < indent; i++)
      write(' ');
  }

  void outputNewline() {
    write(lineSep);
  }

  static class WrappedIOException extends RuntimeException {
    final IOException cause;

    WrappedIOException(IOException cause) {
      this.cause = cause;
    }

    public Throwable getCause() {
      return cause;
    }
  }

  void write(String s) {
    try {
      writer.write(s);
    }
    catch (IOException e) {
      throw new WrappedIOException(e);
    }
  }

  void write(char c) {
    try {
      writer.write(c);
    }
    catch (IOException e) {
      throw new WrappedIOException(e);
    }
  }

  void close() {
    try {
      writer.close();
    }
    catch (IOException e) {
      throw new WrappedIOException(e);
    }
  }

  private static String getDefaultValue(AttributePattern p) {
    return getAttributeAnnotation(p, WellKnownNamespaces.RELAX_NG_COMPATIBILITY_ANNOTATIONS, "defaultValue");
  }

  private static String getAttributeAnnotation(Annotated p, String ns, String localName) {
    List<AttributeAnnotation> list = p.getAttributeAnnotations();
    for (int i = 0, len = list.size(); i < len; i++) {
      AttributeAnnotation att = list.get(i);
      if (att.getLocalName().equals(localName)
          && att.getNamespaceUri().equals(ns))
        return att.getValue();
    }
    return null;
  }
}
