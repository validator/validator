package com.thaiopensource.datatype.xsd;

import java.util.Hashtable;

import org.xml.sax.XMLReader;

import com.thaiopensource.datatype.DatatypeFactory;
import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeReader;
import com.thaiopensource.datatype.DatatypeContext;
import com.thaiopensource.datatype.DatatypeAssignment;

public class DatatypeFactoryImpl implements DatatypeFactory {
  static private final String xsdns = "http://www.w3.org/2001/XMLSchema-datatypes";
  private final Hashtable typeTable = new Hashtable();
  private RegexEngine regexEngine;

  static private final String LONG_MAX = "9223372036854775807";
  static private final String LONG_MIN = "-9223372036854775808";
  static private final String INT_MAX = "2147483647";
  static private final String INT_MIN = "-2147483648";
  static private final String SHORT_MAX = "32767";
  static private final String SHORT_MIN = "-32768";
  static private final String BYTE_MAX = "127";
  static private final String BYTE_MIN = "-128";

  static private final String UNSIGNED_LONG_MAX = "18446744073709551615";
  static private final String UNSIGNED_INT_MAX = "4294967295";
  static private final String UNSIGNED_SHORT_MAX = "65535";
  static private final String UNSIGNED_BYTE_MAX = "255";

  public DatatypeFactoryImpl() {
    this(new NullRegexEngine());
  }

  public DatatypeFactoryImpl(RegexEngine regexEngine) {
    this.regexEngine = regexEngine;
    typeTable.put("string", new StringDatatype());
    typeTable.put("normalizedString", new CdataDatatype());
    typeTable.put("token", new TokenDatatype());
    typeTable.put("boolean", new BooleanDatatype());

    DatatypeBase decimalType = new DecimalDatatype();
    typeTable.put("decimal", decimalType);
    DatatypeBase integerType = new ScaleRestrictDatatype(decimalType, 0);
    typeTable.put("integer", integerType);
    typeTable.put("nonPositiveInteger", restrictMax(integerType, "0"));
    typeTable.put("negativeInteger", restrictMax(integerType, "-1"));
    typeTable.put("long", restrictMax(restrictMin(integerType, LONG_MIN), LONG_MAX));
    typeTable.put("int", restrictMax(restrictMin(integerType, INT_MIN), INT_MAX));
    typeTable.put("short", restrictMax(restrictMin(integerType, SHORT_MIN), SHORT_MAX));
    typeTable.put("byte", restrictMax(restrictMin(integerType, BYTE_MIN), BYTE_MAX));
    DatatypeBase nonNegativeIntegerType = restrictMin(integerType, "0");
    typeTable.put("nonNegativeInteger", nonNegativeIntegerType);
    typeTable.put("unsignedLong", restrictMax(nonNegativeIntegerType, UNSIGNED_LONG_MAX));
    typeTable.put("unsignedInt", restrictMax(nonNegativeIntegerType, UNSIGNED_INT_MAX));
    typeTable.put("unsignedShort", restrictMax(nonNegativeIntegerType, UNSIGNED_SHORT_MAX));
    typeTable.put("unsignedByte", restrictMax(nonNegativeIntegerType, UNSIGNED_BYTE_MAX));
    typeTable.put("positiveInteger", restrictMin(integerType, "1"));
    typeTable.put("double", new DoubleDatatype());
    typeTable.put("float", new FloatDatatype());

    typeTable.put("Name", new NameDatatype());
    typeTable.put("QName", new QNameDatatype());

    DatatypeBase ncNameType = new NCNameDatatype();
    typeTable.put("NCName", ncNameType);

    DatatypeBase nmtokenDatatype = new NmtokenDatatype();
    typeTable.put("NMTOKEN", nmtokenDatatype);
    typeTable.put("NMTOKENS", list(nmtokenDatatype));

    typeTable.put("ID", new IdDatatype());
    DatatypeBase idrefType = new IdrefDatatype();
    typeTable.put("IDREF", idrefType);
    typeTable.put("IDREFS", list(idrefType));

    // Partially implemented
    DatatypeBase entityType = ncNameType;
    typeTable.put("ENTITY", entityType);
    typeTable.put("ENTITIES", list(entityType));
    typeTable.put("NOTATION", new NameDatatype());
    typeTable.put("language", new LanguageDatatype());

    // Not implemented yet
    typeTable.put("anyURI", new StringDatatype());
    typeTable.put("base64Binary", new StringDatatype());
    typeTable.put("hexBinary", new StringDatatype());
    typeTable.put("duration", new StringDatatype());
    typeTable.put("dateTime", new StringDatatype());
    typeTable.put("time", new StringDatatype());
    typeTable.put("date", new StringDatatype());
    typeTable.put("gYearMonth", new StringDatatype());
    typeTable.put("gYear", new StringDatatype());
    typeTable.put("gMonthDay", new StringDatatype());
    typeTable.put("gDay", new StringDatatype());
    typeTable.put("gMonth", new StringDatatype());
  }

  public Datatype createDatatype(String namespaceURI, String localName) {
    if (xsdns.equals(namespaceURI))
      return createXsdDatatype(localName);
    return null;
  }

  public DatatypeReader createDatatypeReader(String namespaceURI, DatatypeContext context) {
    if (xsdns.equals(namespaceURI))
      return new DatatypeReaderImpl(this, context);
    return null;
  }

  public DatatypeAssignment createDatatypeAssignment(XMLReader xr) {
    return new DatatypeAssignmentImpl(xr);
  }

  DatatypeBase createXsdDatatype(String localName) {
    return (DatatypeBase)typeTable.get(localName);
  }

  RegexEngine getRegexEngine() {
    return regexEngine;
  }

  private DatatypeBase restrictMax(DatatypeBase base, String limit) {
    return new MaxInclusiveRestrictDatatype(base, base.getValue(limit, null));
  }

  private DatatypeBase restrictMin(DatatypeBase base, String limit) {
    return new MinInclusiveRestrictDatatype(base, base.getValue(limit, null));
  }

  private DatatypeBase list(DatatypeBase base) {
    return new MinLengthRestrictDatatype(new ListDatatype(base), 1);
  }
}
