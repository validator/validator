package com.thaiopensource.xml.dtd.app;

import java.io.IOException;

interface XmlOutputCollection {
  XmlOutputMember getMain();
  XmlOutputMember mapUri(String inputUri) throws IOException;
}
