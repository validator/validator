package com.thaiopensource.relaxng.edit;

public class DefineComponent extends Component {
  public final static String START = new String("#start");
  private String name;
  private Pattern body;
  private Combine combine;

  public DefineComponent(String name, Pattern body) {
    this.name = name;
    this.body = body;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Pattern getBody() {
    return body;
  }

  public void setBody(Pattern body) {
    this.body = body;
  }

  public Combine getCombine() {
    return combine;
  }

  public void setCombine(Combine combine) {
    this.combine = combine;
  }

  public Object accept(ComponentVisitor visitor) {
    return visitor.visitDefine(this);
  }
}
