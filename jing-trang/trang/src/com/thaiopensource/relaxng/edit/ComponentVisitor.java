package com.thaiopensource.relaxng.edit;

public interface ComponentVisitor<T> {
  T visitDiv(DivComponent c);
  T visitInclude(IncludeComponent c);
  T visitDefine(DefineComponent c);
}
