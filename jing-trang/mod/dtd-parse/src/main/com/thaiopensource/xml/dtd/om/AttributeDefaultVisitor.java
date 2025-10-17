package com.thaiopensource.xml.dtd.om;

public interface AttributeDefaultVisitor {
  public void defaultValue(String value) throws Exception;
  public void fixedValue(String value) throws Exception;
  public void impliedValue() throws Exception;
  public void requiredValue() throws Exception;
  public void attributeDefaultRef(String name, AttributeDefault ad)
    throws Exception;
}
