package com.thaiopensource.relaxng.infer;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.util.ErrorHandlerImpl;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.xml.infer.InferHandler;
import com.thaiopensource.xml.infer.Schema;
import com.thaiopensource.xml.infer.ElementDecl;
import com.thaiopensource.xml.infer.Particle;
import com.thaiopensource.xml.infer.AttributeDecl;
import com.thaiopensource.xml.infer.ParticleVisitor;
import com.thaiopensource.xml.infer.ElementParticle;
import com.thaiopensource.xml.infer.ChoiceParticle;
import com.thaiopensource.xml.infer.SequenceParticle;
import com.thaiopensource.xml.infer.EmptyParticle;
import com.thaiopensource.xml.infer.TextParticle;
import com.thaiopensource.xml.infer.OneOrMoreParticle;
import com.thaiopensource.util.UriOrFile;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public class Inferrer {
  private final Schema schema;
  private final GrammarPattern grammar;
  private final ParticleConverter particleConverter = new ParticleConverter();

  private class ParticleConverter implements ParticleVisitor {
    public Object visitElement(ElementParticle p) {
      return new RefPattern(getDefineName(p.getName()));
    }

    public Object visitChoice(ChoiceParticle p) {
      ChoicePattern cp = new ChoicePattern();
      List children = cp.getChildren();
      addChoices(children, p.getChild1());
      addChoices(children, p.getChild2());
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

  public static SchemaCollection infer(String[] args, ErrorHandlerImpl eh) throws SAXException, IOException {
    InferHandler handler = new InferHandler(new DatatypeLibraryLoader());
    XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();
    XMLReader xr = xrc.createXMLReader();
    xr.setErrorHandler(eh);
    xr.setContentHandler(handler);
    for (int i = 1; i < args.length; i++)
       xr.parse(new InputSource(UriOrFile.toUri(args[i])));
    SchemaCollection sc = new SchemaCollection();
    sc.setMainUri(UriOrFile.toUri(args[0]));
    SchemaDocument sd = new SchemaDocument(new Inferrer(handler.getSchema()).grammar);
    sc.getSchemaDocumentMap().put(sc.getMainUri(), sd);
    return sc;
  }

  Inferrer(Schema schema) {
    this.schema = schema;
    this.grammar = new GrammarPattern();
    ChoicePattern cp = new ChoicePattern();
    List startChoices = cp.getChildren();
    DefineComponent startComponent = new DefineComponent(DefineComponent.START, cp);
    grammar.getComponents().add(startComponent);
    for (Iterator iter = schema.getElementDecls().entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      Name elementName = (Name)entry.getKey();
      String defineName = getDefineName(elementName);
      ElementDecl elementDecl = (ElementDecl)entry.getValue();
      if (elementDecl.isStart())
        startChoices.add(new RefPattern(defineName));
      grammar.getComponents().add(new DefineComponent(defineName,
                                                      createPattern(elementName, elementDecl)));

    }
    startComponent.setBody(normalize(cp));
  }

  private Pattern createPattern(Name elementName, ElementDecl elementDecl) {
    Pattern contentPattern;
    Particle particle = elementDecl.getContentModel();
    if (particle != null)
      contentPattern = particleConverter.convert(particle);
    else
      contentPattern = makeDatatype(elementDecl.getDatatype());
    Map attributeDecls = elementDecl.getAttributeDecls();
    if (attributeDecls.size() > 0) {
      GroupPattern group = new GroupPattern();
      for (Iterator iter = attributeDecls.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        AttributeDecl att = (AttributeDecl)entry.getValue();
        Pattern tem;
        if (att.getDatatype() == null)
          tem = new TextPattern();
        else
          tem = makeDatatype(att.getDatatype());
        tem = new AttributePattern(makeNameClass((Name)entry.getKey()), tem);
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
    return new NameNameClass(name.getNamespaceUri(), name.getLocalName());
  }

  private DataPattern makeDatatype(Name datatypeName) {
    return new DataPattern(datatypeName.getNamespaceUri(), datatypeName.getLocalName());
  }

  String getDefineName(Name elementName) {
    return elementName.getLocalName();
  }

  private Pattern normalize(CompositePattern cp) {
    if (cp.getChildren().size() == 1)
      return (Pattern)cp.getChildren().get(0);
    return cp;
  }

}
