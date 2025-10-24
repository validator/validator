// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc.parser;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class KindInfo
{
   long[] validKinds;
   long[] finalKinds;
   int    validKindCnt = 0;
   int    finalKindCnt = 0;
   Set<Integer> finalKindSet = new HashSet<Integer>();
   Set<Integer> validKindSet = new HashSet<Integer>();

   KindInfo(int maxKind)
   {
      validKinds = new long[maxKind / 64 + 1];
      finalKinds = new long[maxKind / 64 + 1];
   }

   public void InsertValidKind(int kind)
   {
      validKinds[kind / 64] |= (1L << (kind % 64));
      validKindCnt++;
      validKindSet.add(kind);
   }

   public void InsertFinalKind(int kind)
   {
      finalKinds[kind / 64] |= (1L << (kind % 64));
      finalKindCnt++;
      finalKindSet.add(kind);
   }
};

/**
 * Describes string literals.
 */

public class RStringLiteral extends RegularExpression {

  /**
   * The string image of the literal.
   */
  public String image;

  public RStringLiteral() {
  }

  public RStringLiteral(Token t, String image) {
    this.setLine(t.beginLine);
    this.setColumn(t.beginColumn);
    this.image = image;
  }

  private static int maxStrKind = 0;
  private static int maxLen = 0;
  private static int charCnt = 0;
  private static List charPosKind = new ArrayList(); // Elements are hashtables
                                                     // with single char keys;
  private static int[] maxLenForActive = new int[100]; // 6400 tokens
  public static String[] allImages;
  private static int[][] intermediateKinds;
  private static int[][] intermediateMatchedPos;

  private static int startStateCnt = 0;
  private static boolean subString[];
  private static boolean subStringAtPos[];
  private static Hashtable[] statesForPos;

  /**
   * Initialize all the static variables, so that there is no interference
   * between the various states of the lexer.
   *
   * Need to call this method after generating code for each lexical state.
   */
  public static void ReInit()
  {
    maxStrKind = 0;
    maxLen = 0;
    charPosKind = new ArrayList();
    maxLenForActive = new int[100]; // 6400 tokens
    intermediateKinds = null;
    intermediateMatchedPos = null;
    startStateCnt = 0;
    subString = null;
    subStringAtPos = null;
    statesForPos = null;
  }

  public static void DumpStrLiteralImages(CodeGenerator codeGenerator)
  {
	// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
    if (Options.isOutputLanguageJava()) {
      DumpStrLiteralImagesForJava(codeGenerator);
      return;
    } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {

	    // For C++
	    String image;
	    int i;
	    charCnt = 0; // Set to zero in reInit() but just to be sure

	    codeGenerator.genCodeLine("");
	    codeGenerator.genCodeLine("/** Token literal values. */");
	    int literalCount = 0;
	    codeGenerator.switchToStaticsFile();

	    if (allImages == null || allImages.length == 0)
	    {
	      codeGenerator.genCodeLine("static const JJString jjstrLiteralImages[] = {};");
	      return;
	    }

	    allImages[0] = "";
	    for (i = 0; i < allImages.length; i++)
	    {
	      if ((image = allImages[i]) == null ||
	          ((Main.lg.toSkip[i / 64] & (1L << (i % 64))) == 0L &&
	           (Main.lg.toMore[i / 64] & (1L << (i % 64))) == 0L &&
	           (Main.lg.toToken[i / 64] & (1L << (i % 64))) == 0L) ||
	          (Main.lg.toSkip[i / 64] & (1L << (i % 64))) != 0L ||
	          (Main.lg.toMore[i / 64] & (1L << (i % 64))) != 0L ||
	          Main.lg.canReachOnMore[Main.lg.lexStates[i]] ||
	          ((Options.getIgnoreCase() || Main.lg.ignoreCase[i]) &&
	           (!image.equals(image.toLowerCase(Locale.ENGLISH)) ||
	            !image.equals(image.toUpperCase(Locale.ENGLISH)))))
	      {
	        allImages[i] = null;
	        if ((charCnt += 6) > 80)
	        {
	          codeGenerator.genCodeLine("");
	          charCnt = 0;
	        }

	        codeGenerator.genCodeLine("static JJChar jjstrLiteralChars_"
	            + literalCount++ + "[] = {0};");
	        continue;
	      }

	      String toPrint = "static JJChar jjstrLiteralChars_" +
	                           literalCount++ + "[] = {";
	      for (int j = 0; j < image.length(); j++) {
	        String hexVal = Integer.toHexString((int)image.charAt(j));
	        toPrint += "0x" + hexVal + ", ";
	      }

	      // Null char
	      toPrint += "0};";

	      if ((charCnt += toPrint.length()) >= 80)
	      {
	        codeGenerator.genCodeLine("");
	        charCnt = 0;
	      }

	      codeGenerator.genCodeLine(toPrint);
	    }

	    while (++i < Main.lg.maxOrdinal)
	    {
	      if ((charCnt += 6) > 80)
	      {
	        codeGenerator.genCodeLine("");
	        charCnt = 0;
	      }

	      codeGenerator.genCodeLine("static JJChar jjstrLiteralChars_" +
	                                 literalCount++ + "[] = {0};");
	      continue;
	    }

	    // Generate the array here.
	    codeGenerator.genCodeLine("static const JJString " +
	                              "jjstrLiteralImages[] = {");
	    for (int j = 0; j < literalCount; j++) {
	      codeGenerator.genCodeLine("jjstrLiteralChars_" + j + ", ");
	    }
	    codeGenerator.genCodeLine("};");
    } else {
    	throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
    }
  }

  public static void DumpStrLiteralImagesForJava(CodeGenerator codeGenerator) {
    String image;
    int i;
    charCnt = 0; // Set to zero in reInit() but just to be sure

    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("/** Token literal values. */");
    codeGenerator.genCodeLine("public static final String[] jjstrLiteralImages = {");

    if (allImages == null || allImages.length == 0)
    {
      codeGenerator.genCodeLine("};");
      return;
    }

    allImages[0] = "";
    for (i = 0; i < allImages.length; i++)
    {
      if ((image = allImages[i]) == null ||
          ((Main.lg.toSkip[i / 64] & (1L << (i % 64))) == 0L &&
           (Main.lg.toMore[i / 64] & (1L << (i % 64))) == 0L &&
           (Main.lg.toToken[i / 64] & (1L << (i % 64))) == 0L) ||
          (Main.lg.toSkip[i / 64] & (1L << (i % 64))) != 0L ||
          (Main.lg.toMore[i / 64] & (1L << (i % 64))) != 0L ||
          Main.lg.canReachOnMore[Main.lg.lexStates[i]] ||
          ((Options.getIgnoreCase() || Main.lg.ignoreCase[i]) &&
           (!image.equals(image.toLowerCase(Locale.ENGLISH)) ||
            !image.equals(image.toUpperCase(Locale.ENGLISH)))))
      {
        allImages[i] = null;
        if ((charCnt += 6) > 80)
        {
          codeGenerator.genCodeLine("");
          charCnt = 0;
        }

        codeGenerator.genCode("null, ");
        continue;
      }

      String toPrint = "\"";
      for (int j = 0; j < image.length(); j++)
      {
        if (codeGenerator.isJavaLanguage() && image.charAt(j) <= 0xff)
          toPrint += ("\\" + Integer.toOctalString((int)image.charAt(j)));
        else
        {
          String hexVal = Integer.toHexString((int)image.charAt(j));
          if (hexVal.length() == 3)
            hexVal = "0" + hexVal;
          toPrint += ("\\u" + hexVal);
        }
      }

      toPrint += ("\", ");

      if ((charCnt += toPrint.length()) >= 80)
      {
        codeGenerator.genCodeLine("");
        charCnt = 0;
      }

      codeGenerator.genCode(toPrint);
    }

    while (++i < Main.lg.maxOrdinal)
    {
      if ((charCnt += 6) > 80)
      {
        codeGenerator.genCodeLine("");
        charCnt = 0;
      }

      codeGenerator.genCode("null, ");
      continue;
    }

    codeGenerator.genCodeLine("};");
  }

  /**
   * Used for top level string literals.
   */
  public void GenerateDfa(CodeGenerator codeGenerator, int kind)
  {
     String s;
     Hashtable temp;
     KindInfo info;
     int len;

     if (maxStrKind <= ordinal)
        maxStrKind = ordinal + 1;

     if ((len = image.length()) > maxLen)
        maxLen = len;

     char c;
     for (int i = 0; i < len; i++)
     {
        if (Options.getIgnoreCase())
           s = ("" + (c = image.charAt(i))).toLowerCase(Locale.ENGLISH);
        else
           s = "" + (c = image.charAt(i));

        if (!NfaState.unicodeWarningGiven && c > 0xff &&
            !Options.getJavaUnicodeEscape() &&
            !Options.getUserCharStream())
        {
           NfaState.unicodeWarningGiven = true;
           JavaCCErrors.warning(Main.lg.curRE, "Non-ASCII characters used in regular expression." +
              "Please make sure you use the correct Reader when you create the parser, " +
              "one that can handle your character set.");
        }

        if (i >= charPosKind.size()) // Kludge, but OK
           charPosKind.add(temp = new Hashtable());
        else
           temp = (Hashtable)charPosKind.get(i);

        if ((info = (KindInfo)temp.get(s)) == null)
           temp.put(s, info = new KindInfo(Main.lg.maxOrdinal));

        if (i + 1 == len)
           info.InsertFinalKind(ordinal);
        else
           info.InsertValidKind(ordinal);

        if (!Options.getIgnoreCase() && Main.lg.ignoreCase[ordinal] &&
            c != Character.toLowerCase(c))
        {
           s = ("" + image.charAt(i)).toLowerCase(Locale.ENGLISH);

           if (i >= charPosKind.size()) // Kludge, but OK
              charPosKind.add(temp = new Hashtable());
           else
              temp = (Hashtable)charPosKind.get(i);

           if ((info = (KindInfo)temp.get(s)) == null)
              temp.put(s, info = new KindInfo(Main.lg.maxOrdinal));

           if (i + 1 == len)
              info.InsertFinalKind(ordinal);
           else
              info.InsertValidKind(ordinal);
        }

        if (!Options.getIgnoreCase() && Main.lg.ignoreCase[ordinal] &&
            c != Character.toUpperCase(c))
        {
           s = ("" + image.charAt(i)).toUpperCase();

           if (i >= charPosKind.size()) // Kludge, but OK
              charPosKind.add(temp = new Hashtable());
           else
              temp = (Hashtable)charPosKind.get(i);

           if ((info = (KindInfo)temp.get(s)) == null)
              temp.put(s, info = new KindInfo(Main.lg.maxOrdinal));

           if (i + 1 == len)
              info.InsertFinalKind(ordinal);
           else
              info.InsertValidKind(ordinal);
        }
     }

     maxLenForActive[ordinal / 64] = Math.max(maxLenForActive[ordinal / 64],
                                                                        len -1);
     allImages[ordinal] = image;
  }

  public Nfa GenerateNfa(boolean ignoreCase)
  {
     if (image.length() == 1)
     {
        RCharacterList temp = new RCharacterList(image.charAt(0));
        return temp.GenerateNfa(ignoreCase);
     }

     NfaState startState = new NfaState();
     NfaState theStartState = startState;
     NfaState finalState = null;

     if (image.length() == 0)
        return new Nfa(theStartState, theStartState);

     int i;

     for (i = 0; i < image.length(); i++)
     {
        finalState = new NfaState();
        startState.charMoves = new char[1];
        startState.AddChar(image.charAt(i));

        if (Options.getIgnoreCase() || ignoreCase)
        {
           startState.AddChar(Character.toLowerCase(image.charAt(i)));
           startState.AddChar(Character.toUpperCase(image.charAt(i)));
        }

        startState.next = finalState;
        startState = finalState;
     }

     return new Nfa(theStartState, finalState);
  }

  static void DumpNullStrLiterals(CodeGenerator codeGenerator)
  {
     codeGenerator.genCodeLine("{");

     if (NfaState.generatedStates != 0)
        codeGenerator.genCodeLine("   return jjMoveNfa" + Main.lg.lexStateSuffix + "(" + NfaState.InitStateName() + ", 0);");
     else
        codeGenerator.genCodeLine("   return 1;");

     codeGenerator.genCodeLine("}");
  }

  private static int GetStateSetForKind(int pos, int kind)
  {
     if (Main.lg.mixed[Main.lg.lexStateIndex] || NfaState.generatedStates == 0)
        return -1;

     Hashtable allStateSets = statesForPos[pos];

     if (allStateSets == null)
        return -1;

     Enumeration e = allStateSets.keys();

     while (e.hasMoreElements())
     {
        String s = (String)e.nextElement();
        long[] actives = (long[])allStateSets.get(s);

        s = s.substring(s.indexOf(", ") + 2);
        s = s.substring(s.indexOf(", ") + 2);

        if (s.equals("null;"))
           continue;

        if (actives != null &&
            (actives[kind / 64] & (1L << (kind % 64))) != 0L)
        {
           return NfaState.AddStartStateSet(s);
        }
     }

     return -1;
  }

  static String GetLabel(int kind)
  {
     RegularExpression re = Main.lg.rexprs[kind];

     if (re instanceof RStringLiteral)
       return " \"" + JavaCCGlobals.add_escapes(((RStringLiteral)re).image) + "\"";
     else if (!re.label.equals(""))
       return " <" + re.label + ">";
     else
       return " <token of kind " + kind + ">";
  }

  static int GetLine(int kind)
  {
     return Main.lg.rexprs[kind].getLine();
  }

  static int GetColumn(int kind)
  {
     return Main.lg.rexprs[kind].getColumn();
  }

  /**
   * Returns true if s1 starts with s2 (ignoring case for each character).
   */
  static private boolean StartsWithIgnoreCase(String s1, String s2)
  {
     if (s1.length() < s2.length())
        return false;

     for (int i = 0; i < s2.length(); i++)
     {
        char c1 = s1.charAt(i), c2 = s2.charAt(i);

        if (c1 != c2 && Character.toLowerCase(c2) != c1 &&
            Character.toUpperCase(c2) != c1)
           return false;
     }

     return true;
  }

  static void FillSubString()
  {
     String image;
     subString = new boolean[maxStrKind + 1];
     subStringAtPos = new boolean[maxLen];

     for (int i = 0; i < maxStrKind; i++)
     {
        subString[i] = false;

        if ((image = allImages[i]) == null ||
            Main.lg.lexStates[i] != Main.lg.lexStateIndex)
           continue;

        if (Main.lg.mixed[Main.lg.lexStateIndex])
        {
           // We will not optimize for mixed case
           subString[i] = true;
           subStringAtPos[image.length() - 1] = true;
           continue;
        }

        for (int j = 0; j < maxStrKind; j++)
        {
           if (j != i && Main.lg.lexStates[j] == Main.lg.lexStateIndex &&
               ((String)allImages[j]) != null)
           {
              if (((String)allImages[j]).indexOf(image) == 0)
              {
                 subString[i] = true;
                 subStringAtPos[image.length() - 1] = true;
                 break;
              }
              else if (Options.getIgnoreCase() &&
                       StartsWithIgnoreCase((String)allImages[j], image))
              {
                 subString[i] = true;
                 subStringAtPos[image.length() - 1] = true;
                 break;
              }
           }
        }
     }
  }

  static void DumpStartWithStates(CodeGenerator codeGenerator)
  {
	// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
    if (Options.isOutputLanguageJava()) {
     codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private int " +
                  "jjStartNfaWithStates" + Main.lg.lexStateSuffix + "(int pos, int kind, int state)");
    } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
     codeGenerator.generateMethodDefHeader("int", Main.lg.tokMgrClassName, "jjStartNfaWithStates" + Main.lg.lexStateSuffix + "(int pos, int kind, int state)");
    } else {
    	throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
    }
     codeGenerator.genCodeLine("{");
     codeGenerator.genCodeLine("   jjmatchedKind = kind;");
     codeGenerator.genCodeLine("   jjmatchedPos = pos;");

     if (Options.getDebugTokenManager()) {
       if (codeGenerator.isJavaLanguage()) {
         codeGenerator.genCodeLine("   debugStream.println(\"   No more string literal token matches are possible.\");");
         codeGenerator.genCodeLine("   debugStream.println(\"   Currently matched the first \" " +
                 "+ (jjmatchedPos + 1) + \" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");
       } else {
         codeGenerator.genCodeLine("   fprintf(debugStream, \"   No more string literal token matches are possible.\");");
         codeGenerator.genCodeLine("   fprintf(debugStream, \"   Currently matched the first %d characters as a \\\"%s\\\" token.\\n\",  (jjmatchedPos + 1),  addUnicodeEscapes(tokenImage[jjmatchedKind]).c_str());");
       }
     }

     // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
     if (Options.isOutputLanguageJava()) {
       codeGenerator.genCodeLine("   try { curChar = input_stream.readChar(); }");
       codeGenerator.genCodeLine("   catch(java.io.IOException e) { return pos + 1; }");
     } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)){
       codeGenerator.genCodeLine("   if (input_stream->endOfInput()) { return pos + 1; }");
       codeGenerator.genCodeLine("   curChar = input_stream->readChar();");
     } else {
    	 throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
     }
     if (Options.getDebugTokenManager()) {
       if (codeGenerator.isJavaLanguage()) {
         codeGenerator.genCodeLine("   debugStream.println(" +
              (LexGen.maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") +
              "\"Current character : \" + " + Options.getTokenMgrErrorClass() +
              ".addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") " +
              "at line \" + input_stream.getEndLine() + \" column \" + input_stream.getEndColumn());");
       } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
         codeGenerator.genCodeLine("   fprintf(debugStream, " +
            "\"<%s>Current character : %c(%d) at line %d column %d\\n\","+
            "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, " +
            "input_stream->getEndLine(), input_stream->getEndColumn());");
       } else {
    	   throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
       }
     }

     codeGenerator.genCodeLine("   return jjMoveNfa" + Main.lg.lexStateSuffix + "(state, pos + 1);");
     codeGenerator.genCodeLine("}");
  }

  private static boolean boilerPlateDumped = false;
  static void DumpBoilerPlate(CodeGenerator codeGenerator)
  {
	// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
     if (Options.isOutputLanguageJava()) {
     codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private int " +
                  "jjStopAtPos(int pos, int kind)");
     } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
       codeGenerator.generateMethodDefHeader(" int ", Main.lg.tokMgrClassName , "jjStopAtPos(int pos, int kind)");
     } else {
    	 throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
     }
     codeGenerator.genCodeLine("{");
     codeGenerator.genCodeLine("   jjmatchedKind = kind;");
     codeGenerator.genCodeLine("   jjmatchedPos = pos;");

     if (Options.getDebugTokenManager()) {
    	// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
       if (codeGenerator.isJavaLanguage()) {
         codeGenerator.genCodeLine("   debugStream.println(\"   No more string literal token matches are possible.\");");
         codeGenerator.genCodeLine("   debugStream.println(\"   Currently matched the first \" + (jjmatchedPos + 1) + " +
                "\" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");
       } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
        codeGenerator.genCodeLine("   fprintf(debugStream, \"   No more string literal token matches are possible.\");");
        codeGenerator.genCodeLine("   fprintf(debugStream, \"   Currently matched the first %d characters as a \\\"%s\\\" token.\\n\",  (jjmatchedPos + 1),  addUnicodeEscapes(tokenImage[jjmatchedKind]).c_str());");
       } else {
    	   throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
       }
     }

     codeGenerator.genCodeLine("   return pos + 1;");
     codeGenerator.genCodeLine("}");
  }

  static String[] ReArrange(Hashtable tab)
  {
     String[] ret = new String[tab.size()];
     Enumeration e = tab.keys();
     int cnt = 0;

     while (e.hasMoreElements())
     {
        int i = 0, j;
        String s;
        char c = (s = (String)e.nextElement()).charAt(0);

        while (i < cnt && ret[i].charAt(0) < c) i++;

        if (i < cnt)
           for (j = cnt - 1; j >= i; j--)
             ret[j + 1] = ret[j];

        ret[i] = s;
        cnt++;
     }

     return ret;
  }

  static void DumpDfaCode(CodeGenerator codeGenerator)
  {
     Hashtable tab;
     String key;
     KindInfo info;
     int maxLongsReqd = maxStrKind / 64 + 1;
     int i, j, k;
     boolean ifGenerated;
     Main.lg.maxLongsReqd[Main.lg.lexStateIndex] = maxLongsReqd;

     if (maxLen == 0)
     {
    	// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
     if (Options.isOutputLanguageJava()) {
        codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private int " +
                       "jjMoveStringLiteralDfa0" + Main.lg.lexStateSuffix + "()");
      } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
        codeGenerator.generateMethodDefHeader(" int ", Main.lg.tokMgrClassName, "jjMoveStringLiteralDfa0" + Main.lg.lexStateSuffix + "()");
      } else {
    	  throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
      }
        DumpNullStrLiterals(codeGenerator);
        return;
     }

     if (!boilerPlateDumped)
     {
        DumpBoilerPlate(codeGenerator);
        boilerPlateDumped = true;
     }

     boolean createStartNfa = false;;
     for (i = 0; i < maxLen; i++)
     {
        boolean atLeastOne = false;
        boolean startNfaNeeded = false;
        tab = (Hashtable)charPosKind.get(i);
        String[] keys = ReArrange(tab);

        StringBuffer params = new StringBuffer();
        params.append("(");
        if (i != 0)
        {
           if (i == 1)
           {
              for (j = 0; j < maxLongsReqd - 1; j++)
                 if (i <= maxLenForActive[j])
                 {
                    if (atLeastOne)
                       params.append(", ");
                    else
                       atLeastOne = true;
                    params.append("" + Options.getLongType() + " active" + j);
                 }

              if (i <= maxLenForActive[j])
              {
                 if (atLeastOne)
                    params.append(", ");
                 params.append("" + Options.getLongType() + " active" + j);
              }
           }
           else
           {
              for (j = 0; j < maxLongsReqd - 1; j++)
                 if (i <= maxLenForActive[j] + 1)
                 {
                    if (atLeastOne)
                       params.append(", ");
                    else
                       atLeastOne = true;
                    params.append("" + Options.getLongType() + " old" + j + ", " + Options.getLongType() + " active" + j);
                 }

              if (i <= maxLenForActive[j] + 1)
              {
                 if (atLeastOne)
                    params.append(", ");
                 params.append("" + Options.getLongType() + " old" + j + ", " + Options.getLongType() + " active" + j);
              }
           }
        }
        params.append(")");

     // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
     if (Options.isOutputLanguageJava()) {
        codeGenerator.genCode((Options.getStatic() ? "static " : "") + "private int " +
                       "jjMoveStringLiteralDfa" + i + Main.lg.lexStateSuffix + params);
      } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
        codeGenerator.generateMethodDefHeader(" int ", Main.lg.tokMgrClassName, "jjMoveStringLiteralDfa" + i + Main.lg.lexStateSuffix + params);
      } else {
    	  throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
      }

      codeGenerator.genCodeLine("{");

        if (i != 0)
        {
           if (i > 1)
           {
              atLeastOne = false;
              codeGenerator.genCode("   if ((");

              for (j = 0; j < maxLongsReqd - 1; j++)
                 if (i <= maxLenForActive[j] + 1)
                 {
                    if (atLeastOne)
                       codeGenerator.genCode(" | ");
                    else
                       atLeastOne = true;
                    codeGenerator.genCode("(active" + j + " &= old" + j + ")");
                 }

              if (i <= maxLenForActive[j] + 1)
              {
                 if (atLeastOne)
                    codeGenerator.genCode(" | ");
                 codeGenerator.genCode("(active" + j + " &= old" + j + ")");
              }

              codeGenerator.genCodeLine(") == 0L)");
              if (!Main.lg.mixed[Main.lg.lexStateIndex] && NfaState.generatedStates != 0)
              {
                 codeGenerator.genCode("      return jjStartNfa" + Main.lg.lexStateSuffix +
                                 "(" + (i - 2) + ", ");
                 for (j = 0; j < maxLongsReqd - 1; j++)
                    if (i <= maxLenForActive[j] + 1)
                       codeGenerator.genCode("old" + j + ", ");
                    else
                       codeGenerator.genCode("0L, ");
                 if (i <= maxLenForActive[j] + 1)
                    codeGenerator.genCodeLine("old" + j + ");");
                 else
                    codeGenerator.genCodeLine("0L);");
              }
              else if (NfaState.generatedStates != 0)
                 codeGenerator.genCodeLine("      return jjMoveNfa" + Main.lg.lexStateSuffix +
                         "(" + NfaState.InitStateName() + ", " + (i - 1) + ");");
              else
                 codeGenerator.genCodeLine("      return " + i + ";");
           }

           if (i != 0 && Options.getDebugTokenManager())
           {
             if (codeGenerator.isJavaLanguage()) {
               codeGenerator.genCodeLine("   if (jjmatchedKind != 0 && jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
               codeGenerator.genCodeLine("      debugStream.println(\"   Currently matched the first \" + " + "(jjmatchedPos + 1) + \" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");
               codeGenerator.genCodeLine("   debugStream.println(\"   Possible string literal matches : { \"");
             } else {
               codeGenerator.genCodeLine("   if (jjmatchedKind != 0 && jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
               codeGenerator.genCodeLine("      fprintf(debugStream, \"   Currently matched the first %d characters as a \\\"%s\\\" token.\\n\", (jjmatchedPos + 1), addUnicodeEscapes(tokenImage[jjmatchedKind]).c_str());");
              codeGenerator.genCodeLine("   fprintf(debugStream, \"   Possible string literal matches : { \");");
             }

              StringBuffer fmt = new StringBuffer();
              StringBuffer args = new StringBuffer();
              for (int vecs = 0; vecs < maxStrKind / 64 + 1; vecs++)
              {
                 if (i <= maxLenForActive[vecs])
                 {
                   if (codeGenerator.isJavaLanguage()) {
                     codeGenerator.genCodeLine(" +");
                     codeGenerator.genCode("         jjKindsForBitVector(" + vecs + ", ");
                     codeGenerator.genCode("active" + vecs + ") ");
                   } else {
                     if (fmt.length() > 0) {
                       fmt.append(", ");
                       args.append(", ");
                     }

                     fmt.append("%s");
                     args.append("         jjKindsForBitVector(" + vecs + ", ");
                     args.append("active" + vecs + ")" + (codeGenerator.isJavaLanguage() ? " " : ".c_str() "));
                   }
                 }
              }

               // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
              if (codeGenerator.isJavaLanguage()) {
                codeGenerator.genCodeLine(" + \" } \");");
              } else  if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
                fmt.append("}\\n");
                codeGenerator.genCodeLine("    fprintf(debugStream, \"" + fmt + "\"," +  args + ");");
              } else {
            	  throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
              }
           }

           // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
           if (Options.isOutputLanguageJava()) {
             codeGenerator.genCodeLine("   try { curChar = input_stream.readChar(); }");
             codeGenerator.genCodeLine("   catch(java.io.IOException e) {");
           } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
             codeGenerator.genCodeLine("   if (input_stream->endOfInput()) {");
           } else {
        	   throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
           }

           if (!Main.lg.mixed[Main.lg.lexStateIndex] && NfaState.generatedStates != 0)
           {
              codeGenerator.genCode("      jjStopStringLiteralDfa" + Main.lg.lexStateSuffix + "(" + (i - 1) + ", ");
              for (k = 0; k < maxLongsReqd - 1; k++) {
                 if (i <= maxLenForActive[k])
                    codeGenerator.genCode("active" + k + ", ");
                 else
                    codeGenerator.genCode("0L, ");
              }

              if (i <= maxLenForActive[k]) {
                 codeGenerator.genCodeLine("active" + k + ");");
              } else {
                 codeGenerator.genCodeLine("0L);");
              }


              if (i != 0 && Options.getDebugTokenManager()) {
                if (codeGenerator.isJavaLanguage()) {
                  codeGenerator.genCodeLine("      if (jjmatchedKind != 0 && jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
                  codeGenerator.genCodeLine("         debugStream.println(\"   Currently matched the first \" + " + "(jjmatchedPos + 1) + \" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");
                } else {
                  codeGenerator.genCodeLine("      if (jjmatchedKind != 0 && jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
                  codeGenerator.genCodeLine("      fprintf(debugStream, \"   Currently matched the first %d characters as a \\\"%s\\\" token.\\n\", (jjmatchedPos + 1),  addUnicodeEscapes(tokenImage[jjmatchedKind]).c_str());");
                }
              }

              codeGenerator.genCodeLine("      return " + i + ";");
           }
           else if (NfaState.generatedStates != 0) {
              codeGenerator.genCodeLine("   return jjMoveNfa" + Main.lg.lexStateSuffix + "(" + NfaState.InitStateName() +
                      ", " + (i - 1) + ");");
           } else {
              codeGenerator.genCodeLine("      return " + i + ";");
           }

           codeGenerator.genCodeLine("   }");
        }



     // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
        if (i != 0 && Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP) ) {
          codeGenerator.genCodeLine("   curChar = input_stream->readChar();");
        }

        if (i != 0 && Options.getDebugTokenManager()) {

        	// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
          if (codeGenerator.isJavaLanguage()) {
            codeGenerator.genCodeLine("   debugStream.println(" +
                   (LexGen.maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") +
                   "\"Current character : \" + " + Options.getTokenMgrErrorClass() +
                   ".addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") " +
                   "at line \" + input_stream.getEndLine() + \" column \" + input_stream.getEndColumn());");
          } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
            codeGenerator.genCodeLine("   fprintf(debugStream, " +
              "\"<%s>Current character : %c(%d) at line %d column %d\\n\","+
              "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, " +
              "input_stream->getEndLine(), input_stream->getEndColumn());");
          } else {
        	  throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
          }
        }

        codeGenerator.genCodeLine("   switch(curChar)");
        codeGenerator.genCodeLine("   {");

        CaseLoop:
        for (int q = 0; q < keys.length; q++)
        {
           key = keys[q];
           info = (KindInfo)tab.get(key);
           ifGenerated = false;
           char c = key.charAt(0);

           if (i == 0 && c < 128 && info.finalKindCnt != 0 &&
               (NfaState.generatedStates == 0 || !NfaState.CanStartNfaUsingAscii(c)))
           {
              int kind;
              for (j = 0; j < maxLongsReqd; j++)
                 if (info.finalKinds[j] != 0L)
                    break;

              for (k = 0; k < 64; k++)
                 if ((info.finalKinds[j] & (1L << k)) != 0L &&
                     !subString[kind = (j * 64 + k)])
                 {
                   if ((intermediateKinds != null &&
                        intermediateKinds[(j * 64 + k)] != null &&
                        intermediateKinds[(j * 64 + k)][i] < (j * 64 + k) &&
                        intermediateMatchedPos != null &&
                        intermediateMatchedPos[(j * 64 + k)][i] == i) ||
                       (Main.lg.canMatchAnyChar[Main.lg.lexStateIndex] >= 0 &&
                        Main.lg.canMatchAnyChar[Main.lg.lexStateIndex] < (j * 64 + k)))
                      break;
                    else if ((Main.lg.toSkip[kind / 64] & (1L << (kind % 64))) != 0L  &&
                             (Main.lg.toSpecial[kind / 64] & (1L << (kind % 64))) == 0L  &&
                             Main.lg.actions[kind] == null &&
                             Main.lg.newLexState[kind] == null)
                    {
                       Main.lg.AddCharToSkip(c, kind);

                       if (Options.getIgnoreCase())
                       {
                          if (c != Character.toUpperCase(c))
                             Main.lg.AddCharToSkip(Character.toUpperCase(c), kind);

                          if (c != Character.toLowerCase(c))
                             Main.lg.AddCharToSkip(Character.toLowerCase(c), kind);
                       }
                       continue CaseLoop;
                    }
                 }
           }

           // Since we know key is a single character ...
           if (Options.getIgnoreCase())
           {
              if (c != Character.toUpperCase(c))
                 codeGenerator.genCodeLine("      case " + (int)Character.toUpperCase(c) + ":");

              if (c != Character.toLowerCase(c))
                 codeGenerator.genCodeLine("      case " + (int)Character.toLowerCase(c) + ":");
           }

           codeGenerator.genCodeLine("      case " + (int)c + ":");

           long matchedKind;
           String prefix = (i == 0) ? "         " : "            ";

           if (info.finalKindCnt != 0)
           {
              for (j = 0; j < maxLongsReqd; j++)
              {
                 if ((matchedKind = info.finalKinds[j]) == 0L)
                    continue;

                 for (k = 0; k < 64; k++)
                 {
                    if ((matchedKind & (1L << k)) == 0L)
                       continue;

                    if (ifGenerated)
                    {
                       codeGenerator.genCode("         else if ");
                    }
                    else if (i != 0)
                       codeGenerator.genCode("         if ");

                    ifGenerated = true;

                    int kindToPrint;
                    if (i != 0)
                    {
                       codeGenerator.genCodeLine("((active" + j +
                          " & 0x" + Long.toHexString(1L << k) + "L) != 0L)");
                    }

                    if (intermediateKinds != null &&
                        intermediateKinds[(j * 64 + k)] != null &&
                        intermediateKinds[(j * 64 + k)][i] < (j * 64 + k) &&
                        intermediateMatchedPos != null &&
                        intermediateMatchedPos[(j * 64 + k)][i] == i)
                    {
                       JavaCCErrors.warning(" \"" +
                           JavaCCGlobals.add_escapes(allImages[j * 64 + k]) +
                           "\" cannot be matched as a string literal token " +
                           "at line " + GetLine(j * 64 + k) + ", column " + GetColumn(j * 64 + k) +
                           ". It will be matched as " +
                           GetLabel(intermediateKinds[(j * 64 + k)][i]) + ".");
                       kindToPrint = intermediateKinds[(j * 64 + k)][i];
                    }
                    else if (i == 0 &&
                         Main.lg.canMatchAnyChar[Main.lg.lexStateIndex] >= 0 &&
                         Main.lg.canMatchAnyChar[Main.lg.lexStateIndex] < (j * 64 + k))
                    {
                       JavaCCErrors.warning(" \"" +
                           JavaCCGlobals.add_escapes(allImages[j * 64 + k]) +
                           "\" cannot be matched as a string literal token " +
                           "at line " + GetLine(j * 64 + k) + ", column " + GetColumn(j * 64 + k) +
                           ". It will be matched as " +
                           GetLabel(Main.lg.canMatchAnyChar[Main.lg.lexStateIndex]) + ".");
                       kindToPrint = Main.lg.canMatchAnyChar[Main.lg.lexStateIndex];
                    }
                    else
                       kindToPrint = j * 64 + k;

                    if (!subString[(j * 64 + k)])
                    {
                       int stateSetName = GetStateSetForKind(i, j * 64 + k);

                       if (stateSetName != -1)
                       {
                          createStartNfa = true;
                          codeGenerator.genCodeLine(prefix + "return jjStartNfaWithStates" +
                              Main.lg.lexStateSuffix + "(" + i +
                              ", " + kindToPrint + ", " + stateSetName + ");");
                       }
                       else
                          codeGenerator.genCodeLine(prefix + "return jjStopAtPos" + "(" + i + ", " + kindToPrint + ");");
                    }
                    else
                    {
                       if ((Main.lg.initMatch[Main.lg.lexStateIndex] != 0 &&
                            Main.lg.initMatch[Main.lg.lexStateIndex] != Integer.MAX_VALUE) ||
                            i != 0)
                       {
                          codeGenerator.genCodeLine("         {");
                          codeGenerator.genCodeLine(prefix + "jjmatchedKind = " +
                                                     kindToPrint + ";");
                          codeGenerator.genCodeLine(prefix + "jjmatchedPos = " + i + ";");
                          codeGenerator.genCodeLine("         }");
                       }
                       else
                          codeGenerator.genCodeLine(prefix + "jjmatchedKind = " +
                                                     kindToPrint + ";");
                    }
                 }
              }
           }

           if (info.validKindCnt != 0)
           {
              atLeastOne = false;

              if (i == 0)
              {
                 codeGenerator.genCode("         return ");

                 codeGenerator.genCode("jjMoveStringLiteralDfa" + (i + 1) +
                                Main.lg.lexStateSuffix + "(");
                 for (j = 0; j < maxLongsReqd - 1; j++)
                    if ((i + 1) <= maxLenForActive[j])
                    {
                       if (atLeastOne)
                          codeGenerator.genCode(", ");
                       else
                          atLeastOne = true;

                       codeGenerator.genCode("0x" + Long.toHexString(info.validKinds[j]) + (codeGenerator.isJavaLanguage() ? "L" : "L"));
                    }

                 if ((i + 1) <= maxLenForActive[j])
                 {
                    if (atLeastOne)
                       codeGenerator.genCode(", ");

                    codeGenerator.genCode("0x" + Long.toHexString(info.validKinds[j]) + (codeGenerator.isJavaLanguage() ? "L" : "L"));
                 }
                 codeGenerator.genCodeLine(");");
              }
              else
              {
                 codeGenerator.genCode("         return ");

                 codeGenerator.genCode("jjMoveStringLiteralDfa" + (i + 1) +
                                Main.lg.lexStateSuffix + "(");

                 for (j = 0; j < maxLongsReqd - 1; j++)
                    if ((i + 1) <= maxLenForActive[j] + 1)
                    {
                       if (atLeastOne)
                          codeGenerator.genCode(", ");
                       else
                          atLeastOne = true;

                       if (info.validKinds[j] != 0L)
                          codeGenerator.genCode("active" + j + ", 0x" +
                                  Long.toHexString(info.validKinds[j]) + (codeGenerator.isJavaLanguage() ? "L" : "L"));
                       else
                          codeGenerator.genCode("active" + j + ", 0L");
                    }

                 if ((i + 1) <= maxLenForActive[j] + 1)
                 {
                    if (atLeastOne)
                       codeGenerator.genCode(", ");
                    if (info.validKinds[j] != 0L)
                       codeGenerator.genCode("active" + j + ", 0x" +
                                  Long.toHexString(info.validKinds[j]) + (codeGenerator.isJavaLanguage() ? "L" : "L"));
                    else
                       codeGenerator.genCode("active" + j + ", 0L");
                 }

                 codeGenerator.genCodeLine(");");
              }
           }
           else
           {
              // A very special case.
              if (i == 0 && Main.lg.mixed[Main.lg.lexStateIndex])
              {

                 if (NfaState.generatedStates != 0)
                    codeGenerator.genCodeLine("         return jjMoveNfa" + Main.lg.lexStateSuffix +
                            "(" + NfaState.InitStateName() + ", 0);");
                 else
                    codeGenerator.genCodeLine("         return 1;");
              }
              else if (i != 0) // No more str literals to look for
              {
                 codeGenerator.genCodeLine("         break;");
                 startNfaNeeded = true;
              }
           }
        }

        /* default means that the current character is not in any of the
           strings at this position. */
        codeGenerator.genCodeLine("      default :");

        if (Options.getDebugTokenManager()) {
          if (codeGenerator.isJavaLanguage()) {
            codeGenerator.genCodeLine("      debugStream.println(\"   No string literal matches possible.\");");
          } else {
           codeGenerator.genCodeLine("      fprintf(debugStream, \"   No string literal matches possible.\\n\");");
          }
        }

        if (NfaState.generatedStates != 0)
        {
           if (i == 0)
           {
              /* This means no string literal is possible. Just move nfa with
                 this guy and return. */
              codeGenerator.genCodeLine("         return jjMoveNfa" + Main.lg.lexStateSuffix +
                      "(" + NfaState.InitStateName() + ", 0);");
           }
           else
           {
              codeGenerator.genCodeLine("         break;");
              startNfaNeeded = true;
           }
        }
        else
        {
           codeGenerator.genCodeLine("         return " + (i + 1) + ";");
        }


        codeGenerator.genCodeLine("   }");

        if (i != 0)
        {
          if (startNfaNeeded)
          {
           if (!Main.lg.mixed[Main.lg.lexStateIndex] && NfaState.generatedStates != 0)
           {
              /* Here, a string literal is successfully matched and no more
                 string literals are possible. So set the kind and state set
                 upto and including this position for the matched string. */

              codeGenerator.genCode("   return jjStartNfa" + Main.lg.lexStateSuffix + "(" + (i - 1) + ", ");
              for (k = 0; k < maxLongsReqd - 1; k++)
                 if (i <= maxLenForActive[k])
                    codeGenerator.genCode("active" + k + ", ");
                 else
                    codeGenerator.genCode("0L, ");
              if (i <= maxLenForActive[k])
                 codeGenerator.genCodeLine("active" + k + ");");
              else
                 codeGenerator.genCodeLine("0L);");
           }
           else if (NfaState.generatedStates != 0)
              codeGenerator.genCodeLine("   return jjMoveNfa" + Main.lg.lexStateSuffix +
                      "(" + NfaState.InitStateName() + ", " + i + ");");
           else
              codeGenerator.genCodeLine("   return " + (i + 1) + ";");
          }
        }

        codeGenerator.genCodeLine("}");
     }

     if (!Main.lg.mixed[Main.lg.lexStateIndex] && NfaState.generatedStates != 0 && createStartNfa)
        DumpStartWithStates(codeGenerator);
  }

  static final int GetStrKind(String str)
  {
     for (int i = 0; i < maxStrKind; i++)
     {
        if (Main.lg.lexStates[i] != Main.lg.lexStateIndex)
           continue;

        String image = allImages[i];
        if (image != null && image.equals(str))
           return i;
     }

     return Integer.MAX_VALUE;
  }

  static void GenerateNfaStartStates(CodeGenerator codeGenerator,
                                                NfaState initialState)
  {
     boolean[] seen = new boolean[NfaState.generatedStates];
     Hashtable stateSets = new Hashtable();
     String stateSetString  = "";
     int i, j, kind, jjmatchedPos = 0;
     int maxKindsReqd = maxStrKind / 64 + 1;
     long[] actives;
     List newStates = new ArrayList();
     List oldStates = null, jjtmpStates;

     statesForPos = new Hashtable[maxLen];
     intermediateKinds = new int[maxStrKind + 1][];
     intermediateMatchedPos = new int[maxStrKind + 1][];

     for (i = 0; i < maxStrKind; i++)
     {
        if (Main.lg.lexStates[i] != Main.lg.lexStateIndex)
           continue;

        String image = allImages[i];

        if (image == null || image.length() < 1)
           continue;

        try
        {
           if ((oldStates = (List)initialState.epsilonMoves.clone()) == null ||
               oldStates.size() == 0)
           {
              DumpNfaStartStatesCode(statesForPos, codeGenerator);
              return;
           }
        }
        catch(Exception e)
        {
           JavaCCErrors.semantic_error("Error cloning state vector");
        }

        intermediateKinds[i] = new int[image.length()];
        intermediateMatchedPos[i] = new int[image.length()];
        jjmatchedPos = 0;
        kind = Integer.MAX_VALUE;

        for (j = 0; j < image.length(); j++)
        {
           if (oldStates == null || oldStates.size() <= 0)
           {
              // Here, j > 0
              kind = intermediateKinds[i][j] = intermediateKinds[i][j - 1];
              jjmatchedPos = intermediateMatchedPos[i][j] = intermediateMatchedPos[i][j - 1];
           }
           else
           {
              kind = NfaState.MoveFromSet(image.charAt(j), oldStates, newStates);
              oldStates.clear();

              if (j == 0 && kind != Integer.MAX_VALUE &&
                  Main.lg.canMatchAnyChar[Main.lg.lexStateIndex] != -1 &&
                  kind > Main.lg.canMatchAnyChar[Main.lg.lexStateIndex])
                 kind = Main.lg.canMatchAnyChar[Main.lg.lexStateIndex];

              if (GetStrKind(image.substring(0, j + 1)) < kind)
              {
                 intermediateKinds[i][j] = kind = Integer.MAX_VALUE;
                 jjmatchedPos = 0;
              }
              else if (kind != Integer.MAX_VALUE)
              {
                 intermediateKinds[i][j] = kind;
                 jjmatchedPos = intermediateMatchedPos[i][j] = j;
              }
              else if (j == 0)
                 kind = intermediateKinds[i][j] = Integer.MAX_VALUE;
              else
              {
                 kind = intermediateKinds[i][j] = intermediateKinds[i][j - 1];
                 jjmatchedPos = intermediateMatchedPos[i][j] = intermediateMatchedPos[i][j - 1];
              }

              stateSetString = NfaState.GetStateSetString(newStates);
           }

           if (kind == Integer.MAX_VALUE &&
               (newStates == null || newStates.size() == 0))
              continue;

           int p;
           if (stateSets.get(stateSetString) == null)
           {
              stateSets.put(stateSetString, stateSetString);
              for (p = 0; p < newStates.size(); p++)
              {
                 if (seen[((NfaState)newStates.get(p)).stateName])
                    ((NfaState)newStates.get(p)).inNextOf++;
                 else
                    seen[((NfaState)newStates.get(p)).stateName] = true;
              }
           }
           else
           {
              for (p = 0; p < newStates.size(); p++)
                 seen[((NfaState)newStates.get(p)).stateName] = true;
           }

           jjtmpStates = oldStates;
           oldStates = newStates;
           (newStates = jjtmpStates).clear();

           if (statesForPos[j] == null)
              statesForPos[j] = new Hashtable();

           if ((actives = ((long[])statesForPos[j].get(kind + ", " +
                                    jjmatchedPos + ", " + stateSetString))) == null)
           {
              actives = new long[maxKindsReqd];
              statesForPos[j].put(kind + ", " + jjmatchedPos + ", " +
                                                 stateSetString, actives);
           }

           actives[i / 64] |= 1L << (i % 64);
           //String name = NfaState.StoreStateSet(stateSetString);
        }
     }

     // TODO(Sreeni) : Fix this mess.
     if (Options.getTokenManagerCodeGenerator() == null) {
       DumpNfaStartStatesCode(statesForPos, codeGenerator);
     }
  }

  static void DumpNfaStartStatesCode(Hashtable[] statesForPos,
                                              CodeGenerator codeGenerator)
  {
      if (maxStrKind == 0) { // No need to generate this function
         return;
      }

     int i, maxKindsReqd = maxStrKind / 64 + 1;
     boolean condGenerated = false;
     int ind = 0;

     StringBuffer params = new StringBuffer();
     for (i = 0; i < maxKindsReqd - 1; i++)
        params.append("" + Options.getLongType() + " active" + i + ", ");
     params.append("" + Options.getLongType() + " active" + i + ")");

  // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
     if (Options.isOutputLanguageJava()) {
     codeGenerator.genCode("private" + (Options.getStatic() ? " static" : "") + " final int jjStopStringLiteralDfa" +
                  Main.lg.lexStateSuffix + "(int pos, " + params);
      } else if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)) {
        codeGenerator.generateMethodDefHeader(" int", Main.lg.tokMgrClassName, "jjStopStringLiteralDfa" + Main.lg.lexStateSuffix + "(int pos, " + params);
      } else {
    	  throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
      }

     codeGenerator.genCodeLine("{");

     if (Options.getDebugTokenManager()) {
    	// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
       if (codeGenerator.isJavaLanguage()) {
         codeGenerator.genCodeLine("      debugStream.println(\"   No more string literal token matches are possible.\");");
       } else  if (Options.getOutputLanguage().equals(Options.OUTPUT_LANGUAGE__CPP)){
         codeGenerator.genCodeLine("      fprintf(debugStream, \"   No more string literal token matches are possible.\");");
       } else {
    	   throw new RuntimeException("Output language type not fully implemented : " + Options.getOutputLanguage());
       }
     }

     codeGenerator.genCodeLine("   switch (pos)");
     codeGenerator.genCodeLine("   {");

     for (i = 0; i < maxLen - 1; i++)
     {
        if (statesForPos[i] == null)
           continue;

        codeGenerator.genCodeLine("      case " + i + ":");

        Enumeration e = statesForPos[i].keys();
        while (e.hasMoreElements())
        {
           String stateSetString = (String)e.nextElement();
           long[] actives = (long[])statesForPos[i].get(stateSetString);

           for (int j = 0; j < maxKindsReqd; j++)
           {
              if (actives[j] == 0L)
                 continue;

              if (condGenerated)
                 codeGenerator.genCode(" || ");
              else
                 codeGenerator.genCode("         if (");

              condGenerated = true;

              codeGenerator.genCode("(active" + j + " & 0x" +
                  Long.toHexString(actives[j]) + "L) != 0L");
           }

           if (condGenerated)
           {
              codeGenerator.genCodeLine(")");

              String kindStr = stateSetString.substring(0,
                                       ind = stateSetString.indexOf(", "));
              String afterKind = stateSetString.substring(ind + 2);
              int jjmatchedPos = Integer.parseInt(
                           afterKind.substring(0, afterKind.indexOf(", ")));

              if (!kindStr.equals(String.valueOf(Integer.MAX_VALUE)))
                 codeGenerator.genCodeLine("         {");

              if (!kindStr.equals(String.valueOf(Integer.MAX_VALUE)))
              {
                 if (i == 0)
                 {
                    codeGenerator.genCodeLine("            jjmatchedKind = " + kindStr + ";");

                    if ((Main.lg.initMatch[Main.lg.lexStateIndex] != 0 &&
                        Main.lg.initMatch[Main.lg.lexStateIndex] != Integer.MAX_VALUE))
                       codeGenerator.genCodeLine("            jjmatchedPos = 0;");
                 }
                 else if (i == jjmatchedPos)
                 {
                    if (subStringAtPos[i])
                    {
                       codeGenerator.genCodeLine("            if (jjmatchedPos != " + i + ")");
                       codeGenerator.genCodeLine("            {");
                       codeGenerator.genCodeLine("               jjmatchedKind = " + kindStr + ";");
                       codeGenerator.genCodeLine("               jjmatchedPos = " + i + ";");
                       codeGenerator.genCodeLine("            }");
                    }
                    else
                    {
                       codeGenerator.genCodeLine("            jjmatchedKind = " + kindStr + ";");
                       codeGenerator.genCodeLine("            jjmatchedPos = " + i + ";");
                    }
                 }
                 else
                 {
                    if (jjmatchedPos > 0)
                       codeGenerator.genCodeLine("            if (jjmatchedPos < " + jjmatchedPos + ")");
                    else
                       codeGenerator.genCodeLine("            if (jjmatchedPos == 0)");
                    codeGenerator.genCodeLine("            {");
                    codeGenerator.genCodeLine("               jjmatchedKind = " + kindStr + ";");
                    codeGenerator.genCodeLine("               jjmatchedPos = " + jjmatchedPos + ";");
                    codeGenerator.genCodeLine("            }");
                 }
              }

              kindStr = stateSetString.substring(0,
                                    ind = stateSetString.indexOf(", "));
              afterKind = stateSetString.substring(ind + 2);
              stateSetString = afterKind.substring(
                                       afterKind.indexOf(", ") + 2);

              if (stateSetString.equals("null;"))
                 codeGenerator.genCodeLine("            return -1;");
              else
                 codeGenerator.genCodeLine("            return " +
                    NfaState.AddStartStateSet(stateSetString) + ";");

              if (!kindStr.equals(String.valueOf(Integer.MAX_VALUE)))
                 codeGenerator.genCodeLine("         }");
              condGenerated = false;
           }
        }

        codeGenerator.genCodeLine("         return -1;");
     }

     codeGenerator.genCodeLine("      default :");
     codeGenerator.genCodeLine("         return -1;");
     codeGenerator.genCodeLine("   }");
     codeGenerator.genCodeLine("}");

     params.setLength(0);
     params.append("(int pos, ");
     for (i = 0; i < maxKindsReqd - 1; i++)
        params.append("" + Options.getLongType() + " active" + i + ", ");
     params.append("" + Options.getLongType() + " active" + i + ")");

     if (codeGenerator.isJavaLanguage()) {
       codeGenerator.genCode("private" + (Options.getStatic() ? " static" : "") + " final int jjStartNfa" +
                  Main.lg.lexStateSuffix + params);
     } else {
       codeGenerator.generateMethodDefHeader("int ", Main.lg.tokMgrClassName, "jjStartNfa" + Main.lg.lexStateSuffix + params);
     }
     codeGenerator.genCodeLine("{");

     if (Main.lg.mixed[Main.lg.lexStateIndex])
     {
         if (NfaState.generatedStates != 0)
            codeGenerator.genCodeLine("   return jjMoveNfa" + Main.lg.lexStateSuffix +
                    "(" + NfaState.InitStateName() + ", pos + 1);");
         else
            codeGenerator.genCodeLine("   return pos + 1;");

         codeGenerator.genCodeLine("}");
         return;
     }

     codeGenerator.genCode("   return jjMoveNfa" + Main.lg.lexStateSuffix + "(" +
               "jjStopStringLiteralDfa" + Main.lg.lexStateSuffix + "(pos, ");
     for (i = 0; i < maxKindsReqd - 1; i++)
        codeGenerator.genCode("active" + i + ", ");
     codeGenerator.genCode("active" + i + ")");
     codeGenerator.genCodeLine(", pos + 1);");
     codeGenerator.genCodeLine("}");
  }
  /**
   * Return to original state.
   */
  public static void reInit()
  {
    ReInit();

    charCnt = 0;
    allImages = null;
    boilerPlateDumped = false;
  }

  public StringBuffer dump(int indent, Set alreadyDumped) {
    StringBuffer sb = super.dump(indent, alreadyDumped).append(' ').append(image);
    return sb;
  }

  public String toString() {
    return super.toString() + " - " + image;
  }

/*
  static void GenerateData(TokenizerData tokenizerData) {
     Hashtable tab;
     String key;
     KindInfo info;
     for (int i = 0; i < maxLen; i++) {
        tab = (Hashtable)charPosKind.get(i);
        String[] keys = ReArrange(tab);
        if (Options.getIgnoreCase()) {
          for (String s : keys) {
            char c = s.charAt(0);
            tab.put(Character.toLowerCase(c), tab.get(c));
            tab.put(Character.toUpperCase(c), tab.get(c));
          }
        }
        for (int q = 0; q < keys.length; q++) {
           key = keys[q];
           info = (KindInfo)tab.get(key);
           char c = key.charAt(0);
           for (int kind : info.finalKindSet) {
             tokenizerData.addDfaFinalKindAndState(
                 i, c, kind, GetStateSetForKind(i, kind));
           }
           for (int kind : info.validKindSet) {
             tokenizerData.addDfaValidKind(i, c, kind);
           }
        }
     }
     for (int i = 0; i < maxLen; i++) {
        Enumeration e = statesForPos[i].keys();
        while (e.hasMoreElements())
        {
           String stateSetString = (String)e.nextElement();
           long[] actives = (long[])statesForPos[i].get(stateSetString);
           int ind = stateSetString.indexOf(", ");
           String kindStr = stateSetString.substring(0, ind);
           String afterKind = stateSetString.substring(ind + 2);
           stateSetString = afterKind.substring(afterKind.indexOf(", ") + 2);
           BitSet bits = BitSet.valueOf(actives);

           for (int j = 0; j < bits.length(); j++) {
             if (bits.get(j)) tokenizerData.addFinalDfaKind(j);
           }
           // Pos
           codeGenerator.genCode(
               ", " + afterKind.substring(0, afterKind.indexOf(", ")));
           // Kind
           codeGenerator.genCode(", " + kindStr);

           // State
           if (stateSetString.equals("null;")) {
              codeGenerator.genCodeLine(", -1");
           } else {
              codeGenerator.genCodeLine(
                  ", " + NfaState.AddStartStateSet(stateSetString));
           }
        }
        codeGenerator.genCode("}");
     }
     codeGenerator.genCodeLine("};");
  }
*/

  static final Map<Integer, List<String>> literalsByLength =
      new HashMap<Integer, List<String>>();
  static final Map<Integer, List<Integer>> literalKinds =
      new HashMap<Integer, List<Integer>>();
  static final Map<Integer, Integer> kindToLexicalState =
      new HashMap<Integer, Integer>();
  static final Map<Integer, NfaState> nfaStateMap =
      new HashMap<Integer, NfaState>();
  public static void UpdateStringLiteralData(
      int generatedNfaStates, int lexStateIndex) {
    for (int kind = 0; kind < allImages.length; kind++) {
      if (allImages[kind] == null || allImages[kind].equals("") ||
          Main.lg.lexStates[kind] != lexStateIndex) {
        continue;
      }
      String s = allImages[kind];
      int actualKind;
      if (intermediateKinds != null &&
          intermediateKinds[kind][s.length() - 1] != Integer.MAX_VALUE &&
          intermediateKinds[kind][s.length() - 1] < kind) {
        JavaCCErrors.warning("Token: " + s + " will not be matched as " +
                             "specified. It will be matched as token " +
                             "of kind: " +
                             intermediateKinds[kind][s.length() - 1] +
                             " instead.");
        actualKind = intermediateKinds[kind][s.length() - 1];
      } else {
        actualKind = kind;
      }
      kindToLexicalState.put(actualKind, lexStateIndex);
      if (Options.getIgnoreCase()) {
        s = s.toLowerCase(Locale.ENGLISH);
      }
      char c = s.charAt(0);
      int key = (int)Main.lg.lexStateIndex << 16 | (int)c;
      List<String> l = literalsByLength.get(key);
      List<Integer> kinds = literalKinds.get(key);
      int j = 0;
      if (l == null) {
        literalsByLength.put(key, l = new ArrayList<String>());
        assert(kinds == null);
        kinds = new ArrayList<Integer>();
        literalKinds.put(key, kinds = new ArrayList<Integer>());
      }
      while (j < l.size() && l.get(j).length() > s.length()) j++;
      l.add(j, s);
      kinds.add(j, actualKind);
      int stateIndex = GetStateSetForKind(s.length() - 1, kind);
      if (stateIndex != -1) {
        nfaStateMap.put(actualKind, NfaState.getNfaState(stateIndex));
      } else {
        nfaStateMap.put(actualKind, null);
      }
    }
  }

  public static void BuildTokenizerData(TokenizerData tokenizerData) {
    Map<Integer, Integer> nfaStateIndices = new HashMap<Integer, Integer>();
    for (int kind : nfaStateMap.keySet()) {
      if (nfaStateMap.get(kind) != null) {
        nfaStateIndices.put(kind, nfaStateMap.get(kind).stateName);
      } else {
        nfaStateIndices.put(kind, -1);
      }
    }
    tokenizerData.setLiteralSequence(literalsByLength);
    tokenizerData.setLiteralKinds(literalKinds);
    tokenizerData.setKindToNfaStartState(nfaStateIndices);
  }
}
