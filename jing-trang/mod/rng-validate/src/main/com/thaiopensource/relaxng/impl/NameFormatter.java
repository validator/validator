package com.thaiopensource.relaxng.impl;

import com.thaiopensource.xml.util.Name;


public class NameFormatter {
  public static String format(Name name) {
    String localName = name.getLocalName();
    String namespaceUri = name.getNamespaceUri();
    if (namespaceUri.equals(""))
      return SchemaBuilderImpl.localizer.message("name_absent_namespace", localName);
    else
      return SchemaBuilderImpl.localizer.message("name_with_namespace", namespaceUri, localName);
  }
}
