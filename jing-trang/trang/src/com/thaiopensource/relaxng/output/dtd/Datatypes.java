package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.HashMap;
import java.util.Map;

public class Datatypes {

  private static final Map xsdMap = new HashMap();

  // exactly equivalent to DTD datatype of same name
  private static final int COMPATIBLE = 0x0;
  // closest to CDATA
  private static final int CDATA = 0x1;
  // closest to NMTOKEN
  private static final int NMTOKEN = 0x2;
  // closest to CDATA
  private static final int CDATA_EQUALITY = 0x4;
  // closest to CDATA
  private static final int TOKEN_EQUALITY = 0x8;
  // closest CDATA type is exact
  private static final int EXACT = 0x10;

  private static final Object[] others = {
    "",
    new Info("string", CDATA|EXACT|CDATA_EQUALITY),
    "",
    new Info("token", CDATA|EXACT|TOKEN_EQUALITY),
    WellKnownNamespaces.RELAX_NG_COMPATIBILITY_DATATYPES,
    new Info("ID", COMPATIBLE|EXACT|TOKEN_EQUALITY),
    WellKnownNamespaces.RELAX_NG_COMPATIBILITY_DATATYPES,
    new Info("IDREF", COMPATIBLE|EXACT|TOKEN_EQUALITY),
    WellKnownNamespaces.RELAX_NG_COMPATIBILITY_DATATYPES,
    new Info("IDREFS", COMPATIBLE|EXACT|TOKEN_EQUALITY)
  };

  public final static class Info {
    private final String name;
    private final int flags;
    private Info(String name, int flags) {
      this.name = name;
      this.flags = flags;
    }

    public String closestType() {
      switch (flags & 0x3) {
      case COMPATIBLE:
	return name;
      case NMTOKEN:
	return "NMTOKEN";
      default:
	return "CDATA";
      }
    }

    public boolean isExact() {
      return (flags & EXACT) != 0;
    }

    public boolean usesTokenEquality() {
      return (flags & TOKEN_EQUALITY) != 0;
    }

    public boolean usesCdataEquality() {
      return (flags & CDATA_EQUALITY) != 0;
    }
  }

  public static Info getInfo(String datatypeLibrary, String localName) {
    if (datatypeLibrary.equals(WellKnownNamespaces.XML_SCHEMA_DATATYPES))
      return (Info)xsdMap.get(localName);
    for (int i = 0; i < others.length; i += 2)
      if (datatypeLibrary.equals(others[i])
	  && localName.equals(((Info)others[i + 1]).name))
	return (Info)others[i + 1];
    return null;
  }

  private static void xsd(String name, int flags) {
    xsdMap.put(name, new Info(name, flags));
  }

  static {
    xsd("ENTITIES", COMPATIBLE|EXACT|TOKEN_EQUALITY);
    xsd("ENTITY", COMPATIBLE|EXACT|TOKEN_EQUALITY);
    xsd("ID", COMPATIBLE|EXACT|TOKEN_EQUALITY);
    xsd("IDREF", COMPATIBLE|EXACT|TOKEN_EQUALITY);
    xsd("IDREFS", COMPATIBLE|EXACT|TOKEN_EQUALITY);
    xsd("NMTOKEN", COMPATIBLE|EXACT|TOKEN_EQUALITY);
    xsd("NMTOKENS", COMPATIBLE|EXACT|TOKEN_EQUALITY);
    xsd("NOTATION", NMTOKEN);
    xsd("NCName", NMTOKEN);
    xsd("QName", NMTOKEN);
    xsd("anyURI", CDATA|CDATA_EQUALITY);
    xsd("base64Binary", CDATA);
    xsd("boolean", NMTOKEN);
    xsd("byte", CDATA);
    xsd("date", NMTOKEN);
    xsd("dateTime", NMTOKEN);
    xsd("decimal", CDATA);
    xsd("duration", NMTOKEN);
    xsd("gDay", NMTOKEN);
    xsd("gMonth", NMTOKEN);
    xsd("gMonthDay", NMTOKEN);
    xsd("gYear", NMTOKEN);
    xsd("gYearMonth", NMTOKEN);
    xsd("hexBinary", NMTOKEN);
    xsd("int", CDATA);
    xsd("integer", CDATA);
    xsd("language", CDATA); // XXX
    xsd("long", CDATA);
    xsd("negativeInteger", CDATA);
    xsd("nonNegativeInteger", CDATA);
    xsd("nonPositiveInteger", CDATA);
    xsd("normalizedString", CDATA|EXACT|CDATA_EQUALITY);
    xsd("positiveInteger", CDATA);
    xsd("short", CDATA);
    xsd("string", CDATA|EXACT|CDATA_EQUALITY);
    xsd("time", NMTOKEN);
    xsd("token", CDATA|EXACT|TOKEN_EQUALITY);
    xsd("unsignedInt", CDATA);
    xsd("unsignedLong", CDATA);
    xsd("unsignedShort", CDATA);
  }
}
