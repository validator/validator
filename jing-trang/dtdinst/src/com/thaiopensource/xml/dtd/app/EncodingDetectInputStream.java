package com.thaiopensource.xml.dtd.app;

import java.io.InputStream;
import java.io.IOException;
import java.io.CharConversionException;

import com.thaiopensource.xml.tok.TextDecl;
import com.thaiopensource.xml.tok.InvalidTokenException;

public class EncodingDetectInputStream extends InputStream {
  private final InputStream in;
  private byte[] buf;
  private int avail = 0;
  private int start = 0;

  public EncodingDetectInputStream(InputStream in) {
    this.in = in;
  }

  private static final short[] detectProg = { 
    // num bytes, bytes,..., loByteIndex, bytesPerChar, bomLength, encType,
    4, 0x00, 0x00, 0xFE, 0xFF, 3, 4, 4, 0,
    4, 0xFF, 0xFE, 0x00, 0x00, 0, 4, 4, 0,
    4, 0x00, 0x00, 0xFF, 0xFE, 2, 4, 4, 0,
    4, 0xFE, 0xFF, 0x00, 0x00, 1, 4, 4, 0,
    2, 0xFE, 0xFF,             1, 2, 2, 0,
    2, 0xFF, 0xFE,             0, 2, 2, 0,
    3, 0xEF, 0xBB, 0xBF,       0, 1, 3, 0,
    4, 0x00, 0x00, 0x00, 0x3C, 3, 4, 0, 0,
    4, 0x3C, 0x00, 0x00, 0x00, 0, 4, 0, 0,
    4, 0x00, 0x00, 0x3C, 0x00, 2, 4, 0, 0,
    4, 0x00, 0x3C, 0x00, 0x00, 1, 4, 0, 0,
    4, 0x00, 0x3C, 0x00, 0x3F, 1, 2, 0, 0,
    4, 0x3C, 0x00, 0x3F, 0x00, 0, 2, 0, 0,
    4, 0x3C, 0x3F, 0x78, 0x6D, 0, 1, 0, 0,
    4, 0x4C, 0x6F, 0xA7, 0x94, 0, 1, 0, 1
  };

  static final int NPARMS = 4;

  /**
   * Detects the encoding based on the bytes following the current
   * position of the stream.  Returns the name of the encoding.
   * Skips past any byte order mark with UTF-8.
   */
  private static final String OPEN = "<?xml";
  public String detectEncoding() throws IOException {
    makeAvailable(4);

    int bytesPerChar = 1;
    int loByteIndex = 0;
    int bomLength = 0;
    int encType = 0;

    int pc = 0;
    while (pc < detectProg.length) {
      int nBytes = detectProg[pc++];
      if (avail >= nBytes) {
	boolean match = true;
	for (int i = 0; i < nBytes; i++)
	  if ((buf[start + i] & 0xFF) != detectProg[pc + i]) {
	    match = false;
	    break;
	  }
	if (match) {
	  pc += nBytes;
	  loByteIndex = detectProg[pc++];
	  bytesPerChar = detectProg[pc++];
	  bomLength = detectProg[pc++];
	  encType = detectProg[pc++];
	  break;
	}
      }
      pc += nBytes + NPARMS;
    }
    int chIndex = 0;
    boolean prevCharQuestion = false;
    boolean gotXmlDecl = false;
    
    while (makeAvailable((chIndex + 1)*bytesPerChar + bomLength)) {
      byte b = buf[start + bomLength +chIndex*bytesPerChar + loByteIndex];
      for (int i = 0; i < bytesPerChar; i++)
        if (i != loByteIndex && buf[start + bomLength + chIndex*bytesPerChar + i] != 0)
          throw new CharConversionException("non-ASCII character in encoding declaration");
      char ch = convertByte(b, encType);
      if (ch >= 0x80)
	throw new CharConversionException("non-ASCII character in encoding declaration");
      if (chIndex < OPEN.length()) {
	if (ch != OPEN.charAt(chIndex))
	  break;
      }
      else if (ch == '?')
	prevCharQuestion = true;
      else if (ch =='>' && prevCharQuestion == true) {
	gotXmlDecl = true;
	chIndex++;
	break;
      }
      else
	prevCharQuestion = false;
      ++chIndex;
    }
    String enc = null;
    if (gotXmlDecl) {
      char[] b = new char[chIndex];
      for (int i = 0; i < chIndex; i++)
	b[i] = convertByte(buf[start + bomLength + i*bytesPerChar + loByteIndex],
			   encType);
      try {
	TextDecl decl = new TextDecl(b, 0, b.length);
	enc = decl.getEncoding();
      }
      catch (InvalidTokenException e) {
	throw new CharConversionException("invalid text declaration");
      }
    }
    // Skip the BOM for UTF-8
    if (bytesPerChar == 1) {
      start += bomLength;
      avail -= bomLength;
    }
    if (enc == null) {
      if ((bytesPerChar == 2 && bomLength == 0)
	  || bytesPerChar > 2
	  || encType != 0)
	throw new CharConversionException("missing encoding declaration");
      if (bytesPerChar == 2)
	return "UTF-16";
      return "UTF-8";
    }
    return enc;
  }

  static final String EBCDIC_ENCODING = "Cp037";

  static private char convertByte(byte b, int encType) throws IOException {
    if (encType == 1) {
      String s = new String(new byte[]{b}, EBCDIC_ENCODING);
      if (s.length() != 1)
	throw new CharConversionException();
      return s.charAt(0);
    }
    return (char)(b & 0xFF);
  }

  private static final int INIT_BUF_SIZE = 80;

  private boolean makeAvailable(int required) throws IOException {
    if (avail >= required)
      return true;
    if (buf == null)
      buf = new byte[required > INIT_BUF_SIZE ? required : INIT_BUF_SIZE];
    else if (required > buf.length - start) {
      if (buf.length >= required) {
	// move the available bytes
	for (int i = 0; i < avail; i++)
	  buf[i] = buf[i + start];
	start = 0;
      }
      else {
	// reallocate
	int newBufSize = buf.length * 2;
	newBufSize = required > newBufSize ? required : newBufSize;
	byte[] newBuf = new byte[newBufSize];
	System.arraycopy(buf, start, newBuf, 0, avail);
	buf = newBuf;
      }
    }
    do {
      int nRead = in.read(buf, start + avail, buf.length - start - avail);
      if (nRead == -1)
	return false;
      avail += nRead;
    } while (avail < required);
    return true;
  }

  public int read() throws IOException {
    if (avail > 0) {
      --avail;
      return buf[start++] & 0xFF;
    }
    return in.read();
  }

  public int read(byte[] b, int off, int len) throws IOException {
    if (avail > 0) {
      if (avail >= len) {
	if (b != null)
	  System.arraycopy(buf, start, b, off, len);
	start += len;
	avail -= len;
	return len;
      }
      else {
	// avail < len
	if (b != null)
	  System.arraycopy(buf, start, b, off, avail);
	int n = read(b, off + avail, len - avail);
	if (n < 0)
	  n = avail;
	else
	  n += avail;
	avail = 0;
	return n;
      }
    }
    return in.read(b, off, len);
  }

  public static void main(String[] args) throws Exception {
    System.out.println(new EncodingDetectInputStream(new java.io.FileInputStream(args[0])).detectEncoding());
  }
  
}
