package com.thaiopensource.relaxng.input.xml;

import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;
import com.thaiopensource.xml.infer.AttributeDecl;
import com.thaiopensource.xml.infer.ChoiceParticle;
import com.thaiopensource.xml.infer.ElementDecl;
import com.thaiopensource.xml.infer.ElementParticle;
import com.thaiopensource.xml.infer.EmptyParticle;
import com.thaiopensource.xml.infer.InferHandler;
import com.thaiopensource.xml.infer.OneOrMoreParticle;
import com.thaiopensource.xml.infer.Particle;
import com.thaiopensource.xml.infer.ParticleVisitor;
import com.thaiopensource.xml.infer.Schema;
import com.thaiopensource.xml.infer.SequenceParticle;
import com.thaiopensource.xml.infer.TextParticle;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ErrorHandler;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Collections;
import java.util.Comparator;

class Inferrer {
  private final Schema schema;
  private final Set multiplyReferencedElementNames = new HashSet();
  private final GrammarPattern grammar;
  private final ParticleConverter particleConverter = new ParticleConverter();
  private final List outputQueue = new Vector();
  private final Set queued = new HashSet();
  private String prefixSeparator;

  private static final String SEPARATORS = ".-_";

  static class Options {
    String encoding;
  }

  private static class PatternComparator implements Comparator {
    private static final Class[] classOrder = {
      TextPattern.class, RefPattern.class, ElementPattern.class
    };

    public int compare(Object o1, Object o2) {
      if (o1.getClass() != o2.getClass())
        return classIndex(o1.getClass()) - classIndex(o2.getClass());
      if (o1 instanceof RefPattern)
        return ((RefPattern)o1).getName().compareTo(((RefPattern)o2).getName());
      if (o1 instanceof ElementPattern)
        return extractElementName(o1).compareTo(extractElementName(o2));
      return 0;
    }

    private static Name extractElementName(Object o) {
      NameNameClass nnc = (NameNameClass)((ElementPattern)o).getNameClass();
      return new Name(nnc.getNamespaceUri(), nnc.getLocalName());
    }

    private static int classIndex(Class aClass) {
      for (int i = 0; i < classOrder.length; i++)
        if (aClass == classOrder[i])
          return i;
      return classOrder.length;
    }
  }

  private class ParticleConverter extends PatternComparator implements ParticleVisitor {
    public Object visitElement(ElementParticle p) {
      Name name = p.getName();
      if (multiplyReferencedElementNames.contains(name)) {
        if (!queued.contains(name)) {
          queued.add(name);
          outputQueue.add(name);
        }
        return new RefPattern(getDefineName(name));
      }
      else
        return createElementPattern(name);
    }

    public Object visitChoice(ChoiceParticle p) {
      ChoicePattern cp = new ChoicePattern();
      List children = cp.getChildren();
      addChoices(children, p.getChild1());
      addChoices(children, p.getChild2());
      Collections.sort(children, this);
      for (Iterator iter = children.iterator(); iter.hasNext();)
        if (iter.next() instanceof EmptyPattern) {
          iter.remove();
          return makeOptional(cp);
        }
      return cp;
    }

    private Object makeOptional(ChoicePattern cp) {
      List children = cp.getChildren();
      boolean done = false;
      for (int i = 0, len = children.size(); i < len; i++) {
        Pattern child = (Pattern)children.get(i);
        if (child instanceof OneOrMorePattern) {
          children.set(i, new ZeroOrMorePattern(((OneOrMorePattern)child).getChild()));
          done = true;
        }
      }
      if (done)
        return normalize(cp);
      return new OptionalPattern(normalize(cp));
    }


    private void addChoices(List children, Particle child) {
      Pattern pattern = convert(child);
      if (pattern instanceof ChoicePattern)
        children.addAll(((ChoicePattern)pattern).getChildren());
      else
        children.add(pattern);
    }

    public Object visitSequence(SequenceParticle p) {
      GroupPattern gp = new GroupPattern();
      addGroup(gp.getChildren(), p.getChild1());
      addGroup(gp.getChildren(), p.getChild2());
      return gp;
    }

    private void addGroup(List children, Particle child) {
      Pattern pattern = convert(child);
      if (pattern instanceof GroupPattern)
        children.addAll(((GroupPattern)pattern).getChildren());
      else
        children.add(pattern);
    }

    public Object visitEmpty(EmptyParticle p) {
      return new EmptyPattern();
    }

    public Object visitText(TextParticle p) {
      return new TextPattern();
    }

    public Object visitOneOrMore(OneOrMoreParticle p) {
      return new OneOrMorePattern(convert(p.getChild()));
    }

    public Pattern convert(Particle particle) {
      return (Pattern)particle.accept(this);
    }
  }

  private class ReferenceFinder implements ParticleVisitor {
    private final Set referencedElementNames = new HashSet();

    public Object visitElement(ElementParticle p) {
      Name name = p.getName();
      if (referencedElementNames.contains(name))
        multiplyReferencedElementNames.add(name);
      else
        referencedElementNames.add(name);
      return null;
    }

    public Object visitChoice(ChoiceParticle p) {
      p.getChild1().accept(this);
      p.getChild2().accept(this);
      return null;
    }

    public Object visitSequence(SequenceParticle p) {
      p.getChild1().accept(this);
      p.getChild2().accept(this);
      return null;
    }

    public Object visitEmpty(EmptyParticle p) {
      return null;
    }

    public Object visitText(TextParticle p) {
      return null;
    }

    public Object visitOneOrMore(OneOrMoreParticle p) {
      return p.getChild().accept(this);
    }
  }

  static SchemaCollection infer(String[] args, Options options, ErrorHandler eh) throws SAXException, IOException {
    InferHandler handler = new InferHandler(new DatatypeLibraryLoader());
    XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();
    XMLReader xr = xrc.createXMLReader();
    xr.setErrorHandler(eh);
    xr.setContentHandler(handler);
    for (int i = 0; i < args.length; i++) {
      InputSource in = new InputSource(args[i]);
      if (options.encoding != null)
        in.setEncoding(options.encoding);
      xr.parse(in);
    }
    SchemaCollection sc = new SchemaCollection();
    sc.setMainUri(args[0]);
    SchemaDocument sd = new SchemaDocument(new Inferrer(handler.getSchema()).grammar);
    sc.getSchemaDocumentMap().put(sc.getMainUri(), sd);
    return sc;
  }

  private Inferrer(Schema schema) {
    this.schema = schema;
    this.grammar = new GrammarPattern();
    findMultiplyReferencedElements();
    choosePrefixSeparator();
    grammar.getComponents().add(new DefineComponent(DefineComponent.START,
                                                    particleConverter.convert(schema.getStart())));
    for (int i = 0; i < outputQueue.size(); i++) {
      Name elementName = (Name)outputQueue.get(i);
      grammar.getComponents().add(new DefineComponent(getDefineName(elementName),
                                                      createElementPattern(elementName)));
    }
  }

  private void findMultiplyReferencedElements() {
    ReferenceFinder finder = new ReferenceFinder();
    schema.getStart().accept(finder);
    for (Iterator iter = schema.getElementDecls().values().iterator(); iter.hasNext();) {
      ElementDecl decl = (ElementDecl)iter.next();
      Particle particle = decl.getContentModel();
      if (particle != null)
        particle.accept(finder);
    }
  }


  private void choosePrefixSeparator() {
    Map prefixMap = schema.getPrefixMap();
    Set namespacesInDefines = new HashSet();
    for (Iterator iter = multiplyReferencedElementNames.iterator(); iter.hasNext();)
      namespacesInDefines.add(((Name)iter.next()).getNamespaceUri());
    if (namespacesInDefines.size() <= 1)
      return; // don't need to use prefixes in defines
    // define additional prefixes if necessary
    namespacesInDefines.removeAll(prefixMap.keySet());
    if (namespacesInDefines.size() > 1) {
      namespacesInDefines.remove("");
      int n = 1;
      for (Iterator iter = namespacesInDefines.iterator(); iter.hasNext();) {
        for (;;) {
          String prefix = "ns" + Integer.toString(n++);
          if (!prefixMap.containsKey(prefix)) {
            prefixMap.put(iter.next(), prefix);
            break;
          }
        }
      }
    }
    // choose a prefixSeparator that avoids all collisions
    StringBuffer buf = new StringBuffer();
    for (int len = 1;; len++)
      for (int i = 0; i < SEPARATORS.length(); i++) {
        char c = SEPARATORS.charAt(i);
        for (int j = 0; j < len; j++)
          buf.append(c);
        prefixSeparator = buf.toString();
        if (prefixSeparatorOk())
          return;
        buf.setLength(0);
      }
  }

  private boolean prefixSeparatorOk() {
    Set names = new HashSet();
    for (Iterator iter = multiplyReferencedElementNames.iterator(); iter.hasNext();) {
      String name = getDefineName((Name)iter.next());
      if (names.contains(name))
        return false;
      names.add(name);
    }
    return true;
  }

  private Pattern createElementPattern(Name elementName) {
    ElementDecl elementDecl = schema.getElementDecl(elementName);
    Pattern contentPattern;
    Particle particle = elementDecl.getContentModel();
    if (particle != null)
      contentPattern = particleConverter.convert(particle);
    else
      contentPattern = makeDatatype(elementDecl.getDatatype());
    Map attributeDecls = elementDecl.getAttributeDecls();
    if (attributeDecls.size() > 0) {
      GroupPattern group = new GroupPattern();
      List attributeNames = new Vector();
      attributeNames.addAll(attributeDecls.keySet());
      Collections.sort(attributeNames);
      for (Iterator iter = attributeNames.iterator(); iter.hasNext();) {
        Name attName = (Name)iter.next();
        AttributeDecl att = (AttributeDecl)attributeDecls.get(attName);
        Pattern tem;
        if (att.getDatatype() == null)
          tem = new TextPattern();
        else
          tem = makeDatatype(att.getDatatype());
        tem = new AttributePattern(makeNameClass(attName), tem);
        if (att.isOptional())
          tem = new OptionalPattern(tem);
        group.getChildren().add(tem);
      }
      if (contentPattern instanceof GroupPattern)
        group.getChildren().addAll(((GroupPattern)contentPattern).getChildren());
      else if (!(contentPattern instanceof EmptyPattern))
        group.getChildren().add(contentPattern);
      contentPattern = group;
    }
    return new ElementPattern(makeNameClass(elementName), contentPattern);
  }

  private NameNameClass makeNameClass(Name name) {
    String ns = name.getNamespaceUri();
    NameNameClass nnc = new NameNameClass(ns, name.getLocalName());
    if (!ns.equals("")) {
      String prefix = (String)schema.getPrefixMap().get(ns);
      if (prefix != null)
        nnc.setPrefix(prefix);
    }
    return nnc;
  }

  private static DataPattern makeDatatype(Name datatypeName) {
    return new DataPattern(datatypeName.getNamespaceUri(), datatypeName.getLocalName());
  }

  private String getDefineName(Name elementName) {
    if (prefixSeparator != null) {
      String prefix = (String)schema.getPrefixMap().get(elementName.getNamespaceUri());
      if (prefix != null)
        return prefix + prefixSeparator + elementName.getLocalName();
    }
    return elementName.getLocalName();
  }

  private static Pattern normalize(CompositePattern cp) {
    if (cp.getChildren().size() == 1)
      return (Pattern)cp.getChildren().get(0);
    return cp;
  }

}
