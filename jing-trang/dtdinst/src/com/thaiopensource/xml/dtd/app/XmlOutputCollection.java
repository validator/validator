package com.thaiopensource.xml.dtd.app;

interface XmlOutputCollection {
  XmlOutputMember getMain();
  XmlOutputMember mapUri(String inputUri);
}
