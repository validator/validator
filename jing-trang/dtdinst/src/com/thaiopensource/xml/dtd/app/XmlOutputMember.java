package com.thaiopensource.xml.dtd.app;

import java.io.IOException;

import com.thaiopensource.xml.out.XmlWriter;

interface XmlOutputMember {
  String getSystemId(XmlOutputMember base);
  XmlWriter open(String encoding) throws IOException;
}
