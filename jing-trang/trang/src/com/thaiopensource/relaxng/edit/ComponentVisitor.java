package com.thaiopensource.relaxng.edit;

public interface ComponentVisitor {
  Object visitDiv(DivComponent c);
  Object visitInclude(IncludeComponent c);
  Object visitDefine(DefineComponent c);
}
