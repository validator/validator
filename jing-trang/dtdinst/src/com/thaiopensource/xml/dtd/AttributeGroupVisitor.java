package com.thaiopensource.xml.dtd;

public interface AttributeGroupVisitor {
  void attribute(NameSpec nameSpec,
		 Datatype datatype,
		 AttributeDefault attributeDefault)
    throws Exception;
  void attributeGroupRef(String name, AttributeGroup attributeGroup)
    throws Exception;
}
