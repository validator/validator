package com.thaiopensource.xml.dtd.app;

import java.net.URL;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;

import com.thaiopensource.xml.out.XmlWriter;

class DirectoryOutputCollection implements XmlOutputCollection {

  private final XmlOutputMember mainMember;
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

  DirectoryOutputCollection(String mainUri, File dir, NameMapper nameMapper)
    throws IOException {
    this.dir = dir;
    this.nameMapper = nameMapper;
    this.mainMember = mapUri(mainUri);
  }

  DirectoryOutputCollection(String mainUri, File dir) throws IOException {
    this(mainUri, dir, null);
  }

  public XmlOutputMember getMain() {
    return mainMember;
  }

  public XmlOutputMember mapUri(String inputUri) throws IOException {
    String name = new URL(inputUri).getFile();
    int slash = name.lastIndexOf('/');
    if (slash >= 0)
      name = name.substring(slash + 1);
    name = new File(name).getName();
    if (name.length() == 0)
      throw new IOException("empty file name");
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
