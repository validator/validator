package com.thaiopensource.xml.dtd.app;

class ExtensionMapper implements NameMapper {
  
  private final String from;
  private final String to;

  ExtensionMapper(String from, String to) {
    this.from = from;
    this.to = to;
  }

  public String mapName(String name) {
    int baseLen = name.length() - from.length();
    if (baseLen < 0)
      return name;
    String ext = name.substring(baseLen);
    if (!ext.equalsIgnoreCase(from))
      return name;
    String newExt;
    if (ext.equals(from))
      newExt = to;
    else {
      char[] tem = to.toCharArray();
      for (int i = 0; i < tem.length; i++) {
	char model = (i < ext.length()
		      ? ext.charAt(i)
		      : ext.charAt(ext.length() - 1));
	if (Character.isUpperCase(model))
	  tem[i] = Character.toUpperCase(tem[i]);
	else if (Character.isLowerCase(model))
	  tem[i] = Character.toLowerCase(tem[i]);
      }
      newExt = new String(tem);
    }
    return name.substring(0, baseLen) + newExt;
  }

}
