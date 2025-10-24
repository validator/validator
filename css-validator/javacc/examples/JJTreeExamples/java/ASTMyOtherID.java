public
class ASTMyOtherID extends SimpleNode {
  private String name;
  public ASTMyOtherID(int id) {
    super(id);
  }

  public ASTMyOtherID(Eg4 p, int id) {
    super(p, id);
  }

  /**
   * Set the name.
   * @param n the name
   */
  public void setName(String n) {
    name = n;
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.examples.jjtree.eg2.SimpleNode#toString()
   */
  public String toString() {
    return "Identifier: " + name;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(Eg4Visitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
