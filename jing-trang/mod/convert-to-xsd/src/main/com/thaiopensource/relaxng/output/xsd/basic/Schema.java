package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Schema extends Annotated {
  private final String uri;
  private final String encoding;
  private Schema parent;
  private final List<TopLevel> topLevel = new Vector<TopLevel>();
  private final Map<String, GroupDefinition> groupMap;
  private final Map<String, AttributeGroupDefinition> attributeGroupMap;
  private final Map<String, SimpleTypeDefinition> simpleTypeMap;
  private final List<Schema> subSchemas;
  private final List<Comment> leadingComments = new Vector<Comment>();
  private final List<Comment> trailingComments = new Vector<Comment>();

  public Schema(SourceLocation location, Annotation annotation, String uri, String encoding) {
    super(location, annotation);
    this.uri = uri;
    this.encoding = encoding;
    this.groupMap = new HashMap<String, GroupDefinition>();
    this.attributeGroupMap = new HashMap<String, AttributeGroupDefinition>();
    this.simpleTypeMap = new HashMap<String, SimpleTypeDefinition>();
    this.subSchemas = new Vector<Schema>();
    this.subSchemas.add(this);
  }

  private Schema(SourceLocation location, Annotation annotation, String uri, String encoding, Schema parent) {
    super(location, annotation);
    this.parent = parent;
    this.uri = uri;
    this.encoding = encoding;
    this.groupMap = parent.groupMap;
    this.attributeGroupMap = parent.attributeGroupMap;
    this.simpleTypeMap = parent.simpleTypeMap;
    this.subSchemas = parent.subSchemas;
    this.subSchemas.add(this);
  }

  public String getUri() {
    return uri;
  }

  public String getEncoding() {
    return encoding;
  }

  public Schema getParent() {
    return parent;
  }

  public void defineGroup(String name, Particle particle, SourceLocation location, Annotation annotation) {
    GroupDefinition def = new GroupDefinition(location, annotation, this, name, particle);
    topLevel.add(def);
    groupMap.put(name, def);
  }

  public void defineAttributeGroup(String name, AttributeUse attributeUses, SourceLocation location, Annotation annotation) {
    AttributeGroupDefinition def = new AttributeGroupDefinition(location, annotation, this, name, attributeUses);
    topLevel.add(def);
    attributeGroupMap.put(name, def);
  }

  public void defineSimpleType(String name, SimpleType simpleType, SourceLocation location, Annotation annotation) {
    SimpleTypeDefinition def = new SimpleTypeDefinition(location, annotation, this, name, simpleType);
    topLevel.add(def);
    simpleTypeMap.put(name, def);
  }

  public void addRoot(Particle particle, SourceLocation location, Annotation annotation) {
    topLevel.add(new RootDeclaration(location, annotation, particle));
  }

  public Schema addInclude(String uri, String encoding, SourceLocation location, Annotation annotation) {
    Schema included = new Schema(location, annotation, uri, encoding, this);
    topLevel.add(new Include(location, annotation, included));
    return included;
  }

  public void addComment(String content, SourceLocation location) {
    topLevel.add(new Comment(location, content));
  }

  public GroupDefinition getGroup(String name) {
    return groupMap.get(name);
  }

  public SimpleTypeDefinition getSimpleType(String name) {
    return simpleTypeMap.get(name);
  }

  public AttributeGroupDefinition getAttributeGroup(String name) {
    return attributeGroupMap.get(name);
  }

  public void accept(SchemaVisitor visitor) {
    for (TopLevel t : this.topLevel)
      t.accept(visitor);
  }

  public List<Schema> getSubSchemas() {
    return subSchemas;
  }

  public List<Comment> getLeadingComments() {
    return leadingComments;
  }

  public List<Comment> getTrailingComments() {
    return trailingComments;
  }

  public boolean equals(Object obj) {
    return obj == this;
  }

  public int hashCode() {
    return System.identityHashCode(this);
  }
}
