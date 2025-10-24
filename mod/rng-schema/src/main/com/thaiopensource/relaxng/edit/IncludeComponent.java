package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public class IncludeComponent extends Component implements Container {
  // the actual URI used
  private String uri;
  private String ns;
  // the specified href
  private String href;
  // the base for resolving the baseUri
  private String baseUri;
  private final List<Component> components = new Vector<Component>();

  public IncludeComponent() {
  }

  public IncludeComponent(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public List<Component> getComponents() {
    return components;
  }

  public String getNs() {
    return ns;
  }

  public void setNs(String ns) {
    this.ns = ns;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public <T> T accept(ComponentVisitor<T> visitor) {
    return visitor.visitInclude(this);
  }

  public void componentsAccept(ComponentVisitor<?> visitor) {
    for (Component c : components)
      c.accept(visitor);
  }
}
