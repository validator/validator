package com.thaiopensource.relaxng;

import java.util.Vector;
import java.util.Hashtable;
import java.io.PrintWriter;
import java.io.OutputStream;

import com.thaiopensource.datatype.Datatype;

public class PatternDumper {
  private boolean startTagOpen = false;
  private Vector tagStack = new Vector();
  private PrintWriter writer;
  private int level = 0;
  private boolean suppressIndent = false;
  private Vector patternList = new Vector();
  private Hashtable patternTable = new Hashtable();

  PatternVisitor sequencePatternVisitor = new SequenceDumpPatternVisitor();
  PatternVisitor choicePatternVisitor = new ChoiceDumpPatternVisitor();
  PatternVisitor interleavePatternVisitor = new InterleaveDumpPatternVisitor();
  NameClassVisitor nameClassVisitor = new DumpNameClassVisitor();
  NameClassVisitor choiceNameClassVisitor = new ChoiceDumpNameClassVisitor();
  NameClassVisitor differenceNameClassVisitor = new DifferenceDumpNameClassVisitor();

  static public void dump(PrintWriter writer, Pattern p) {
    new PatternDumper(writer).dump(p);
  }

  static public void dump(OutputStream out, Pattern p) {
    new PatternDumper(new PrintWriter(out)).dump(p);
  }

  PatternDumper(PrintWriter writer) {
    this.writer = writer;
  }

  void dump(Pattern p) {
    write("<?xml version=\"1.0\"?>");
    startElement("grammar");
    attribute("xmlns", PatternReader.relaxngURI);
    startElement("start");
    p.accept(sequencePatternVisitor);
    endElement();
    for (int i = 0; i < patternList.size(); i++) {
      startElement("define");
      Pattern tem = (Pattern)patternList.elementAt(i);
      attribute("name", getName(tem));
      tem.accept(sequencePatternVisitor);
      endElement();
    }
    endElement();
    writer.println();
    writer.flush();
  }

  String getName(Pattern p) {
    String name = (String)patternTable.get(p);
    if (name == null) {
      name = "p" + patternList.size();
      patternList.addElement(p);
      patternTable.put(p, name);
    }
    return name;
  }

  void startElement(String name) {
    closeStartTag();
    indent(level);
    write('<');
    write(name);
    push(name);
    startTagOpen = true;
    level++;
  }

  void closeStartTag() {
    if (startTagOpen) {
      startTagOpen = false;
      write('>');
    }
  }

  void attribute(String name, String value) {
    write(' ');
    write(name);
    write('=');
    write('"');
    chars(value, true);
    write('"');
  }

  void data(String str) {
    if (str.length() > 0) {
      closeStartTag();
      chars(str, false);
      suppressIndent = true;
    }
  }

  void chars(String str, boolean isAttribute) {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case '&':
	write("&amp;");
	break;
      case '<':
	write("&lt;");
	break;
      case '>':
	write("&gt;");
	break;
      case '"':
	if (isAttribute) {
	  write("&quot;");
	  break;
	}
	// fall through
      default:
	write(c);
	break;
      }
    }
  }
      
  void endElement() {
    --level;
    if (startTagOpen) {
      startTagOpen = false;
      write("/>");
      pop();
    }
    else {
      if (!suppressIndent)
	indent(level);
      write("</");
      write(pop());
      write(">");
    }
    suppressIndent = false;
  }

  void indent(int level) {
    writer.println();
    for (int i = 0; i < level; i++)
      write("  ");
  }

  void write(String str) {
    writer.print(str);
  }

  void write(char c) {
    writer.print(c);
  }

  void push(String s) {
    tagStack.addElement(s);
  }

  String pop() {
    String s = (String)tagStack.lastElement();
    tagStack.setSize(tagStack.size() - 1);
    return s;
  }

  class DumpPatternVisitor implements PatternVisitor {
    public void visitEmptySequence() {
      startElement("empty");
      endElement();
    }

    public void visitEmptyChoice() {
      startElement("notAllowed");
      endElement();
    }

    public void visitError() {
      startElement("error");
      endElement();
    }

    public void visitSequence(Pattern p1, Pattern p2) {
      startElement("group");
      p1.accept(sequencePatternVisitor);
      p2.accept(sequencePatternVisitor);
      endElement();
    }

    public void visitInterleave(Pattern p1, Pattern p2) {
      startElement("interleave");
      p1.accept(interleavePatternVisitor);
      p2.accept(interleavePatternVisitor);
      endElement();
    }

    public void visitChoice(Pattern p1, Pattern p2) {
      startElement("choice");
      p1.accept(choicePatternVisitor);
      p2.accept(choicePatternVisitor);
      endElement();
    }

    public void visitOneOrMore(Pattern p) {
      startElement("oneOrMore");
      p.accept(sequencePatternVisitor);
      endElement();
    }

    public void visitElement(NameClass nc, Pattern content) {
      startElement("element");
      nc.accept(nameClassVisitor);
      startElement("ref");
      attribute("name", getName(content));
      endElement();
      endElement();
    }

    public void visitAttribute(NameClass nc, Pattern value) {
      startElement("attribute");
      nc.accept(nameClassVisitor);
      value.accept(sequencePatternVisitor);
      endElement();
    }

    public void visitDatatype(Datatype dt, String key, String keyRef) {
      startElement("text");	// XXX
      endElement();
    }

    public void visitValue(Datatype dt, Object obj) {
      startElement("value");
      // XXX dump dt
      // XXX toString will not handle QName
      data(obj.toString());
      endElement();
    }

    public void visitText() {
      startElement("text");
      endElement();
    }

    public void visitList(Pattern p) {
      startElement("list");
      p.accept(sequencePatternVisitor);
      endElement();
    }
  }

  class SequenceDumpPatternVisitor extends DumpPatternVisitor {
    public void visitSequence(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }
  }

  class ChoiceDumpPatternVisitor extends DumpPatternVisitor {
    public void visitChoice(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }
  }

  class InterleaveDumpPatternVisitor extends DumpPatternVisitor {
    public void visitInterleave(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }
  }


  class DumpNameClassVisitor implements NameClassVisitor {
    public void visitChoice(NameClass nc1, NameClass nc2) {
      startElement("choice");
      nc1.accept(choiceNameClassVisitor);
      nc2.accept(choiceNameClassVisitor);
      endElement();
    }

    public void visitDifference(NameClass nc1, NameClass nc2) {
      startElement("difference");
      nc1.accept(differenceNameClassVisitor);
      nc2.accept(nameClassVisitor);
      endElement();
    }

    public void visitNot(NameClass nc) {
      startElement("not");
      nc.accept(nameClassVisitor);
      endElement();
    }

    public void visitNsName(String ns) {
      startElement("nsName");
      attribute("ns", ns);
      endElement();
    }

    public void visitAnyName() {
      startElement("anyName");
      endElement();
    }

    public void visitName(String ns, String localName) {
      startElement("name");
      attribute("ns", ns);
      data(localName);
      endElement();
    }

    public void visitError() {
      startElement("error");
      endElement();
    }
  }

  class ChoiceDumpNameClassVisitor extends DumpNameClassVisitor {
    public void visitChoice(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }
  }

  class DifferenceDumpNameClassVisitor extends DumpNameClassVisitor {
    public void visitDifference(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(nameClassVisitor);
    }
  }
}
