package com.thaiopensource.relaxng.impl;


class NameFormatter {
  static String format(Name name) {
    String localName = name.getLocalName();
    String namespaceUri = name.getNamespaceUri();
    if (namespaceUri.equals(""))
      return Localizer.message("name_absent_namespace", localName);
    else
      return Localizer.message("name_with_namespace", namespaceUri, localName);
  }
}
