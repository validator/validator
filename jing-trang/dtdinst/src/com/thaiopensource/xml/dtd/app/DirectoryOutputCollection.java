package com.thaiopensource.xml.dtd.app;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;

import com.thaiopensource.xml.out.XmlWriter;

class DirectoryOutputCollection implements XmlOutputCollection {

  private final String mainUri;
  private final File dir;
  private final NameMapper nameMapper;
  private final Hashtable nameTable = new Hashtable();

  private class Member implements XmlOutputMember {
    private final File file;

    Member(File file) {
      this.file = file;
    }

    public String getSystemId(XmlOutputMember base) {
      return file.getName();
    }

    public XmlWriter open(String enc) throws IOException {
      return new XmlOutputStreamWriter(new FileOutputStream(file), enc);
    }
  }

  DirectoryOutputCollection(String mainUri, String dir, NameMapper nameMapper) {
    this.mainUri = mainUri;
    this.dir = new File(dir);
    this.nameMapper = nameMapper;
  }

  DirectoryOutputCollection(String mainUri, String dir) {
    this(mainUri, dir, null);
  }

  public XmlOutputMember getMain() {
    return mapUri(mainUri);
  }

  public XmlOutputMember mapUri(String inputUri) {
    String name = new File(inputUri).getName();
    if (nameMapper != null)
      name = nameMapper.mapName(name);
    if (nameTable.get(name) != null) {
      int i = name.lastIndexOf('.');
      String base;
      String ext;
      if (i < 0) {
	base = name;
	ext = "";
      }
      else {
	base = name.substring(0, i);
	ext = name.substring(i);
      }
      for (int n = 1;; n++) {
	name = base + Integer.toString(n) + ext;
	if (nameTable.get(name) == null)
	  break;
      }
    }
    nameTable.put(name, name);
    return new Member(new File(dir, name));
  }
}
