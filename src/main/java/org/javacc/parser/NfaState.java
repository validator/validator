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
import java.util.BitSet;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * The state of a Non-deterministic Finite Automaton.
 */
public class NfaState
{
   public static boolean unicodeWarningGiven = false;
   public static int generatedStates = 0;

   private static int idCnt = 0;
   private static int lohiByteCnt;
   private static int dummyStateIndex = -1;
   private static boolean done;
   private static boolean mark[];
   private static boolean stateDone[];

   private static List<NfaState> allStates = new ArrayList<NfaState>();
   private static List<NfaState> indexedAllStates = new ArrayList<NfaState>();
   private static List<NfaState> nonAsciiTableForMethod = new ArrayList<NfaState>();
   private static Hashtable equivStatesTable = new Hashtable();
   private static Hashtable allNextStates = new Hashtable();
   private static Hashtable lohiByteTab = new Hashtable();
   private static Hashtable stateNameForComposite = new Hashtable();
   private static Hashtable compositeStateTable = new Hashtable();
   private static Hashtable stateBlockTable = new Hashtable();
   private static Hashtable stateSetsToFix = new Hashtable();

   private static boolean jjCheckNAddStatesUnaryNeeded = false;
   private static boolean jjCheckNAddStatesDualNeeded = false;

   public static void ReInit()
   {
      generatedStates = 0;
      idCnt = 0;
      dummyStateIndex = -1;
      done = false;
      mark = null;
      stateDone = null;

      allStates.clear();
      indexedAllStates.clear();
      equivStatesTable.clear();
      allNextStates.clear();
      compositeStateTable.clear();
      stateBlockTable.clear();
      stateNameForComposite.clear();
      stateSetsToFix.clear();
   }

   long[] asciiMoves = new long[2];
   char[] charMoves = null;
   private char[] rangeMoves = null;
   NfaState next = null;
   private NfaState stateForCase;
   Vector<NfaState> epsilonMoves = new Vector<NfaState>();
   private String epsilonMovesString;
   private NfaState[] epsilonMoveArray;

   private int id;
   int stateName = -1;
   int kind = Integer.MAX_VALUE;
   private int lookingFor;
   private int usefulEpsilonMoves = 0;
   int inNextOf;
   private int lexState;
   private int nonAsciiMethod = -1;
   private int kindToPrint = Integer.MAX_VALUE;
   boolean dummy = false;
   private boolean isComposite = false;
   private int[] compositeStates = null;
   boolean isFinal = false;
   private Vector loByteVec;
   private int[] nonAsciiMoveIndices;
   private int round = 0;
   private int onlyChar = 0;
   private char matchSingleChar;

   NfaState()
   {
      id = idCnt++;
      allStates.add(this);
      lexState = Main.lg.lexStateIndex;
      lookingFor = Main.lg.curKind;
   }

   NfaState CreateClone()
   {
      NfaState retVal = new NfaState();

      retVal.isFinal = isFinal;
      retVal.kind = kind;
      retVal.lookingFor = lookingFor;
      retVal.lexState = lexState;
      retVal.inNextOf = inNextOf;

      retVal.MergeMoves(this);

      return retVal;
   }

   static void InsertInOrder(List v, NfaState s)
   {
      int j;

      for (j = 0; j < v.size(); j++)
         if (((NfaState)v.get(j)).id > s.id)
            break;
         else if (((NfaState)v.get(j)).id  == s.id)
            return;

      v.add(j, s);
   }

   private static char[] ExpandCharArr(char[] oldArr, int incr)
   {
      char[] ret = new char[oldArr.length + incr];
      System.arraycopy(oldArr, 0, ret, 0, oldArr.length);
      return ret;
   }

   void AddMove(NfaState newState)
   {
      if (!epsilonMoves.contains(newState))
         InsertInOrder(epsilonMoves, newState);
   }

   private final void AddASCIIMove(char c)
   {
      asciiMoves[c / 64] |= (1L << (c % 64));
   }

   void AddChar(char c)
   {
      onlyChar++;
      matchSingleChar = c;
      int i;
      char temp;
      char temp1;

      if ((int)c < 128) // ASCII char
      {
         AddASCIIMove(c);
         return;
      }

      if (charMoves == null)
         charMoves = new char[10];

      int len = charMoves.length;

      if (charMoves[len - 1] != 0)
      {
         charMoves = ExpandCharArr(charMoves, 10);
         len += 10;
      }

      for (i = 0; i < len; i++)
         if (charMoves[i] == 0 || charMoves[i] > c)
            break;

      if (!unicodeWarningGiven && c > 0xff &&
          !Options.getJavaUnicodeEscape() &&
          !Options.getUserCharStream())
      {
         unicodeWarningGiven = true;
         JavaCCErrors.warning(Main.lg.curRE, "Non-ASCII characters used in regular expression.\n" +
              "Please make sure you use the correct Reader when you create the parser, " +
              "one that can handle your character set.");
      }

      temp = charMoves[i];
      charMoves[i] = c;

      for (i++; i < len; i++)
      {
         if (temp == 0)
            break;

         temp1 = charMoves[i];
         charMoves[i] = temp;
         temp = temp1;
      }
   }

   void AddRange(char left, char right)
   {
      onlyChar = 2;
      int i;
      char tempLeft1, tempLeft2, tempRight1, tempRight2;

      if (left < 128)
      {
         if (right < 128)
         {
            for (; left <= right; left++)
               AddASCIIMove(left);

            return;
         }

         for (; left < 128; left++)
            AddASCIIMove(left);
      }

      if (!unicodeWarningGiven && (left > 0xff || right > 0xff) &&
          !Options.getJavaUnicodeEscape() &&
          !Options.getUserCharStream())
      {
         unicodeWarningGiven = true;
         JavaCCErrors.warning(Main.lg.curRE, "Non-ASCII characters used in regular expression.\n" +
              "Please make sure you use the correct Reader when you create the parser, " +
              "one that can handle your character set.");
      }

      if (rangeMoves == null)
         rangeMoves = new char[20];

      int len = rangeMoves.length;

      if (rangeMoves[len - 1] != 0)
      {
         rangeMoves = ExpandCharArr(rangeMoves, 20);
         len += 20;
      }

      for (i = 0; i < len; i += 2)
         if (rangeMoves[i] == 0 ||
             (rangeMoves[i] > left) ||
             ((rangeMoves[i] == left) && (rangeMoves[i + 1] > right)))
            break;

      tempLeft1 = rangeMoves[i];
      tempRight1 = rangeMoves[i + 1];
      rangeMoves[i] = left;
      rangeMoves[i + 1] = right;

      for (i += 2; i < len; i += 2)
      {
         if (tempLeft1 == 0)
            break;

         tempLeft2 = rangeMoves[i];
         tempRight2 = rangeMoves[i + 1];
         rangeMoves[i] = tempLeft1;
         rangeMoves[i + 1] = tempRight1;
         tempLeft1 = tempLeft2;
         tempRight1 = tempRight2;
      }
   }

   // From hereon down all the functions are used for code generation

   private static boolean EqualCharArr(char[] arr1, char[] arr2)
   {
      if (arr1 == arr2)
         return true;

      if (arr1 != null &&
          arr2 != null &&
          arr1.length == arr2.length)
      {
         for (int i = arr1.length; i-- > 0;)
            if (arr1[i] != arr2[i])
               return false;

         return true;
      }

      return false;
   }

   private boolean closureDone = false;

   /** This function computes the closure and also updates the kind so that
     * any time there is a move to this state, it can go on epsilon to a
     * new state in the epsilon moves that might have a lower kind of token
     * number for the same length.
   */

   private void EpsilonClosure()
   {
      int i = 0;

      if (closureDone || mark[id])
         return;

      mark[id] = true;

      // Recursively do closure
      for (i = 0; i < epsilonMoves.size(); i++)
         ((NfaState)epsilonMoves.get(i)).EpsilonClosure();

      Enumeration<NfaState> e = epsilonMoves.elements();

      while (e.hasMoreElements())
      {
         NfaState tmp = (NfaState)e.nextElement();

         for (i = 0; i < tmp.epsilonMoves.size(); i++)
         {
            NfaState tmp1 = (NfaState)tmp.epsilonMoves.get(i);
            if (tmp1.UsefulState() && !epsilonMoves.contains(tmp1))
            {
               InsertInOrder(epsilonMoves, tmp1);
               done = false;
            }
         }

         if (kind > tmp.kind)
            kind = tmp.kind;
      }

      if (HasTransitions() && !epsilonMoves.contains(this))
         InsertInOrder(epsilonMoves, this);
   }

   private boolean UsefulState()
   {
      return isFinal || HasTransitions();
   }

   public boolean HasTransitions()
   {
      return (asciiMoves[0] != 0L || asciiMoves[1] != 0L ||
              (charMoves != null && charMoves[0] != 0) ||
              (rangeMoves != null && rangeMoves[0] != 0));
   }

   void MergeMoves(NfaState other)
   {
      // Warning : This function does not merge epsilon moves
      if (asciiMoves == other.asciiMoves)
      {
         JavaCCErrors.semantic_error("Bug in JavaCC : Please send " +
                   "a report along with the input that caused this. Thank you.");
         throw new Error();
      }

      asciiMoves[0] = asciiMoves[0] | other.asciiMoves[0];
      asciiMoves[1] = asciiMoves[1] | other.asciiMoves[1];

      if (other.charMoves != null)
      {
         if (charMoves == null)
            charMoves = other.charMoves;
         else
         {
            char[] tmpCharMoves = new char[charMoves.length +
                                              other.charMoves.length];
            System.arraycopy(charMoves, 0, tmpCharMoves, 0, charMoves.length);
            charMoves = tmpCharMoves;

            for (int i = 0; i < other.charMoves.length; i++)
               AddChar(other.charMoves[i]);
         }
      }

      if (other.rangeMoves != null)
      {
         if (rangeMoves == null)
            rangeMoves = other.rangeMoves;
         else
         {
            char[] tmpRangeMoves = new char[rangeMoves.length +
                                                     other.rangeMoves.length];
            System.arraycopy(rangeMoves, 0, tmpRangeMoves,
                                                        0, rangeMoves.length);
            rangeMoves = tmpRangeMoves;
            for (int i = 0; i < other.rangeMoves.length; i += 2)
               AddRange(other.rangeMoves[i], other.rangeMoves[i + 1]);
         }
      }

      if (other.kind < kind)
         kind = other.kind;

      if (other.kindToPrint < kindToPrint)
         kindToPrint = other.kindToPrint;

      isFinal |= other.isFinal;
   }

   NfaState CreateEquivState(List<NfaState> states)
   {
      NfaState newState = ((NfaState)states.get(0)).CreateClone();

      newState.next = new NfaState();

      InsertInOrder(newState.next.epsilonMoves,
                           ((NfaState)states.get(0)).next);

      for (int i = 1; i < states.size(); i++)
      {
         NfaState tmp2 = ((NfaState)states.get(i));

         if (tmp2.kind < newState.kind)
            newState.kind = tmp2.kind;

         newState.isFinal |= tmp2.isFinal;

         InsertInOrder(newState.next.epsilonMoves, tmp2.next);
      }

      return newState;
   }

   private NfaState GetEquivalentRunTimeState()
   {
      Outer :
      for (int i = allStates.size(); i-- > 0;)
      {
         NfaState other = (NfaState)allStates.get(i);

         if (this != other && other.stateName != -1 &&
             kindToPrint == other.kindToPrint &&
             asciiMoves[0] == other.asciiMoves[0] &&
             asciiMoves[1] == other.asciiMoves[1] &&
             EqualCharArr(charMoves, other.charMoves) &&
             EqualCharArr(rangeMoves, other.rangeMoves))
         {
            if (next == other.next)
               return other;
            else if (next != null && other.next != null)
            {
               if (next.epsilonMoves.size() == other.next.epsilonMoves.size())
               {
                  for (int j = 0; j < next.epsilonMoves.size(); j++)
                     if (next.epsilonMoves.get(j) !=
                           other.next.epsilonMoves.get(j))
                        continue Outer;

                  return other;
               }
            }
         }
      }

      return null;
   }

   // generates code (without outputting it) and returns the name used.
   void GenerateCode()
   {
      if (stateName != -1)
         return;

      if (next != null)
      {
         next.GenerateCode();
         if (next.kind != Integer.MAX_VALUE)
            kindToPrint = next.kind;
      }

      if (stateName == -1 && HasTransitions())
      {
         NfaState tmp = GetEquivalentRunTimeState();

         if (tmp != null)
         {
            stateName = tmp.stateName;
//????
            //tmp.inNextOf += inNextOf;
//????
            dummy = true;
            return;
         }

         stateName = generatedStates++;
         indexedAllStates.add(this);
         GenerateNextStatesCode();
      }
   }

   public static void ComputeClosures()
   {
      for (int i = allStates.size(); i-- > 0; )
      {
         NfaState tmp = (NfaState)allStates.get(i);

         if (!tmp.closureDone)
            tmp.OptimizeEpsilonMoves(true);
      }

      for (int i = 0; i < allStates.size(); i++)
      {
         NfaState tmp = (NfaState)allStates.get(i);

         if (!tmp.closureDone)
            tmp.OptimizeEpsilonMoves(false);
      }

      for (int i = 0; i < allStates.size(); i++)
      {
         NfaState tmp = (NfaState)allStates.get(i);
         tmp.epsilonMoveArray = new NfaState[tmp.epsilonMoves.size()];
         tmp.epsilonMoves.copyInto(tmp.epsilonMoveArray);
      }
   }

   void OptimizeEpsilonMoves(boolean optReqd)
   {
      int i;

      // First do epsilon closure
      done = false;
      while (!done)
      {
         if (mark == null || mark.length < allStates.size())
            mark = new boolean[allStates.size()];

         for (i = allStates.size(); i-- > 0;)
            mark[i] = false;

         done = true;
         EpsilonClosure();
      }

      for (i = allStates.size(); i-- > 0;)
         ((NfaState)allStates.get(i)).closureDone =
                                  mark[((NfaState)allStates.get(i)).id];

      // Warning : The following piece of code is just an optimization.
      // in case of trouble, just remove this piece.

      boolean sometingOptimized = true;

      NfaState newState = null;
      NfaState tmp1, tmp2;
      int j;
      List<NfaState> equivStates = null;

      while (sometingOptimized)
      {
         sometingOptimized = false;
         for (i = 0; optReqd && i < epsilonMoves.size(); i++)
         {
            if ((tmp1 = (NfaState)epsilonMoves.get(i)).HasTransitions())
            {
               for (j = i + 1; j < epsilonMoves.size(); j++)
               {
                  if ((tmp2 = (NfaState)epsilonMoves.get(j)).
                                                           HasTransitions() &&
                      (tmp1.asciiMoves[0] == tmp2.asciiMoves[0] &&
                       tmp1.asciiMoves[1] == tmp2.asciiMoves[1] &&
                       EqualCharArr(tmp1.charMoves, tmp2.charMoves) &&
                       EqualCharArr(tmp1.rangeMoves, tmp2.rangeMoves)))
                  {
                     if (equivStates == null)
                     {
                        equivStates = new ArrayList();
                        equivStates.add(tmp1);
                     }

                     InsertInOrder(equivStates, tmp2);
                     epsilonMoves.removeElementAt(j--);
                  }
               }
            }

            if (equivStates != null)
            {
               sometingOptimized = true;
               String tmp = "";
               for (int l = 0; l < equivStates.size(); l++)
                  tmp += String.valueOf(
                            ((NfaState)equivStates.get(l)).id) + ", ";

               if ((newState = (NfaState)equivStatesTable.get(tmp)) == null)
               {
                  newState = CreateEquivState(equivStates);
                  equivStatesTable.put(tmp, newState);
               }

               epsilonMoves.removeElementAt(i--);
               epsilonMoves.add(newState);
               equivStates = null;
               newState = null;
            }
         }

         for (i = 0; i < epsilonMoves.size(); i++)
         {
            //if ((tmp1 = (NfaState)epsilonMoves.elementAt(i)).next == null)
               //continue;
            tmp1 = (NfaState)epsilonMoves.get(i);

            for (j = i + 1; j < epsilonMoves.size(); j++)
            {
               tmp2 = (NfaState)epsilonMoves.get(j);

               if (tmp1.next == tmp2.next)
               {
                  if (newState == null)
                  {
                     newState = tmp1.CreateClone();
                     newState.next = tmp1.next;
                     sometingOptimized = true;
                  }

                  newState.MergeMoves(tmp2);
                  epsilonMoves.removeElementAt(j--);
               }
            }

            if (newState != null)
            {
               epsilonMoves.removeElementAt(i--);
               epsilonMoves.add(newState);
               newState = null;
            }
         }
      }

      // End Warning

      // Generate an array of states for epsilon moves (not vector)
      if (epsilonMoves.size() > 0)
      {
         for (i = 0; i < epsilonMoves.size(); i++)
            // Since we are doing a closure, just epsilon moves are unnecessary
            if (((NfaState)epsilonMoves.get(i)).HasTransitions())
               usefulEpsilonMoves++;
            else
               epsilonMoves.removeElementAt(i--);
      }
   }

   void GenerateNextStatesCode()
   {
      if (next.usefulEpsilonMoves > 0)
         next.GetEpsilonMovesString();
   }

   String GetEpsilonMovesString()
   {
      int[] stateNames = new int[usefulEpsilonMoves];
      int cnt = 0;

      if (epsilonMovesString != null)
         return epsilonMovesString;

      if (usefulEpsilonMoves > 0)
      {
         NfaState tempState;
         epsilonMovesString = "{ ";
         for (int i = 0; i < epsilonMoves.size(); i++)
         {
            if ((tempState = (NfaState)epsilonMoves.get(i)).
                                                     HasTransitions())
            {
               if (tempState.stateName == -1)
                  tempState.GenerateCode();

               ((NfaState)indexedAllStates.get(tempState.stateName)).inNextOf++;
               stateNames[cnt] = tempState.stateName;
               epsilonMovesString += tempState.stateName + ", ";
               if (cnt++ > 0 && cnt % 16 == 0)
                  epsilonMovesString += "\n";
            }
         }

         epsilonMovesString += "};";
      }

      usefulEpsilonMoves = cnt;
      if (epsilonMovesString != null &&
          allNextStates.get(epsilonMovesString) == null)
      {
         int[] statesToPut = new int[usefulEpsilonMoves];

         System.arraycopy(stateNames, 0, statesToPut, 0, cnt);
         allNextStates.put(epsilonMovesString, statesToPut);
      }

      return epsilonMovesString;
   }

   public static boolean CanStartNfaUsingAscii(char c)
   {
      if (c >= 128)
         throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu");

      String s = Main.lg.initialState.GetEpsilonMovesString();

      if (s == null || s.equals("null;"))
         return false;

      int[] states = (int[])allNextStates.get(s);

      for (int i = 0; i < states.length; i++)
      {
         NfaState tmp = (NfaState)indexedAllStates.get(states[i]);

         if ((tmp.asciiMoves[c / 64 ] & (1L << c % 64)) != 0L)
            return true;
      }

      return false;
   }

   final boolean CanMoveUsingChar(char c)
   {
      int i;

      if (onlyChar == 1)
         return c == matchSingleChar;

      if (c < 128)
         return ((asciiMoves[c / 64 ] & (1L << c % 64)) != 0L);

      // Just check directly if there is a move for this char
      if (charMoves != null && charMoves[0] != 0)
      {
         for (i = 0; i < charMoves.length; i++)
         {
            if (c == charMoves[i])
               return true;
            else if (c < charMoves[i] || charMoves[i] == 0)
               break;
         }
      }


      // For ranges, iterate through the table to see if the current char
      // is in some range
      if (rangeMoves != null && rangeMoves[0] != 0)
         for (i = 0; i < rangeMoves.length; i += 2)
            if (c >= rangeMoves[i] && c <= rangeMoves[i + 1])
               return true;
            else if (c < rangeMoves[i] || rangeMoves[i] == 0)
               break;

      //return (nextForNegatedList != null);
      return false;
   }

   public int getFirstValidPos(String s, int i, int len)
   {
      if (onlyChar == 1)
      {
         char c = matchSingleChar;
         while (c != s.charAt(i) && ++i < len);
         return i;
      }

      do
      {
         if (CanMoveUsingChar(s.charAt(i)))
            return i;
      } while (++i < len);

      return i;
   }

   public int MoveFrom(char c, List newStates)
   {
      if (CanMoveUsingChar(c))
      {
         for (int i = next.epsilonMoves.size(); i-- > 0;)
            InsertInOrder(newStates, (NfaState)next.epsilonMoves.get(i));

         return kindToPrint;
      }

      return Integer.MAX_VALUE;
   }

   public static int MoveFromSet(char c, List states, List newStates)
   {
      int tmp;
      int retVal = Integer.MAX_VALUE;

      for (int i = states.size(); i-- > 0;)
         if (retVal >
             (tmp = ((NfaState)states.get(i)).MoveFrom(c, newStates)))
            retVal = tmp;

      return retVal;
   }

   public static int moveFromSetForRegEx(char c, NfaState[] states, NfaState[] newStates, int round)
   {
      int start = 0;
      int sz = states.length;

      for (int i = 0; i < sz; i++)
      {
         NfaState tmp1, tmp2;

         if ((tmp1 = states[i]) == null)
            break;

         if (tmp1.CanMoveUsingChar(c))
         {
            if (tmp1.kindToPrint != Integer.MAX_VALUE)
            {
               newStates[start] = null;
               return 1;
            }

            NfaState[] v = tmp1.next.epsilonMoveArray;
            for (int j = v.length; j-- > 0;)
            {
               if ((tmp2 = v[j]).round != round)
               {
                  tmp2.round = round;
                  newStates[start++] = tmp2;
               }
            }
         }
      }

      newStates[start] = null;
      return Integer.MAX_VALUE;
   }

   static List allBitVectors = new ArrayList();

   /* This function generates the bit vectors of low and hi bytes for common
      bit vectors and returns those that are not common with anything (in
      loBytes) and returns an array of indices that can be used to generate
      the function names for char matching using the common bit vectors.
      It also generates code to match a char with the common bit vectors.
      (Need a better comment). */

   static int[] tmpIndices = new int[512]; // 2 * 256
   void GenerateNonAsciiMoves(CodeGenerator codeGenerator)
   {
      int i = 0, j = 0;
      char hiByte;
      int cnt = 0;
      long[][] loBytes = new long[256][4];

      if ((charMoves == null || charMoves[0] == 0) &&
          (rangeMoves == null || rangeMoves[0] == 0))
         return;

      if (charMoves != null)
      {
         for (i = 0; i < charMoves.length; i++)
         {
            if (charMoves[i] == 0)
               break;

            hiByte = (char)(charMoves[i] >> 8);
            loBytes[hiByte][(charMoves[i] & 0xff) / 64] |=
                              (1L << ((charMoves[i] & 0xff) % 64));
         }
      }

      if (rangeMoves != null)
      {
         for (i = 0; i < rangeMoves.length; i += 2)
         {
            if (rangeMoves[i] == 0)
               break;

            char c, r;

            r = (char)(rangeMoves[i + 1] & 0xff);
            hiByte = (char)(rangeMoves[i] >> 8);

            if (hiByte == (char)(rangeMoves[i + 1] >> 8))
            {
               for (c = (char)(rangeMoves[i] & 0xff); c <= r; c++)
                  loBytes[hiByte][c / 64] |= (1L << (c % 64));

               continue;
            }

            for (c = (char)(rangeMoves[i] & 0xff); c <= 0xff; c++)
               loBytes[hiByte][c / 64] |= (1L << (c % 64));

            while (++hiByte < (char)(rangeMoves[i + 1] >> 8))
            {
               loBytes[hiByte][0] |= 0xffffffffffffffffL;
               loBytes[hiByte][1] |= 0xffffffffffffffffL;
               loBytes[hiByte][2] |= 0xffffffffffffffffL;
               loBytes[hiByte][3] |= 0xffffffffffffffffL;
            }

            for (c = 0; c <= r; c++)
               loBytes[hiByte][c / 64] |= (1L << (c % 64));
         }
      }

      long[] common = null;
      boolean[] done = new boolean[256];

      for (i = 0; i <= 255; i++)
      {
         if (done[i] ||
             (done[i] =
              loBytes[i][0] == 0 &&
              loBytes[i][1] == 0 &&
              loBytes[i][2] == 0 &&
              loBytes[i][3] == 0))
            continue;

         for (j = i + 1; j < 256; j++)
         {
            if (done[j])
               continue;

            if (loBytes[i][0] == loBytes[j][0] &&
                loBytes[i][1] == loBytes[j][1] &&
                loBytes[i][2] == loBytes[j][2] &&
                loBytes[i][3] == loBytes[j][3])
            {
               done[j] = true;
               if (common == null)
               {
                  done[i] = true;
                  common = new long[4];
                  common[i / 64] |= (1L << (i % 64));
               }

               common[j / 64] |= (1L << (j % 64));
            }
         }

         if (common != null)
         {
            Integer ind;
            String tmp;

            tmp = "{\n   0x" + Long.toHexString(common[0]) + "L, " +
                    "0x" + Long.toHexString(common[1]) + "L, " +
                    "0x" + Long.toHexString(common[2]) + "L, " +
                    "0x" + Long.toHexString(common[3]) + "L\n};";
            if ((ind = (Integer)lohiByteTab.get(tmp)) == null)
            {
               allBitVectors.add(tmp);

               if (!AllBitsSet(tmp)) {
                  if (codeGenerator.isJavaLanguage()) {
                    codeGenerator.genCodeLine("static final " + Options.getLongType() + "[] jjbitVec" +  lohiByteCnt + " = " + tmp);
                  } else {
                    codeGenerator.switchToStaticsFile();
                    codeGenerator.genCodeLine("static const " + Options.getLongType() + " jjbitVec" +  lohiByteCnt + "[] = " + tmp);
                  }
               }
               lohiByteTab.put(tmp, ind = Integer.valueOf(lohiByteCnt++));
            }

            tmpIndices[cnt++] = ind.intValue();

            tmp = "{\n   0x" + Long.toHexString(loBytes[i][0]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][1]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][2]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][3]) + "L\n};";
            if ((ind = (Integer)lohiByteTab.get(tmp)) == null)
            {
               allBitVectors.add(tmp);

               if (!AllBitsSet(tmp))
                  if (codeGenerator.isJavaLanguage()) {
                    codeGenerator.genCodeLine("static final " + Options.getLongType() + "[] jjbitVec" + lohiByteCnt + " = " + tmp);
                  } else {
                    codeGenerator.switchToStaticsFile();
                    codeGenerator.genCodeLine("static const " + Options.getLongType() + " jjbitVec" + lohiByteCnt + "[] = " + tmp);
                    codeGenerator.switchToMainFile();
                  }
               lohiByteTab.put(tmp, ind = Integer.valueOf(lohiByteCnt++));
            }

            tmpIndices[cnt++] = ind.intValue();

            common = null;
         }
      }

      nonAsciiMoveIndices = new int[cnt];
      System.arraycopy(tmpIndices, 0, nonAsciiMoveIndices, 0, cnt);

/*
      System.out.println("state : " + stateName + " cnt : " + cnt);
      while (cnt > 0)
      {
         System.out.print(nonAsciiMoveIndices[cnt - 1] + ", " + nonAsciiMoveIndices[cnt - 2] + ", ");
         cnt -= 2;
      }
      System.out.println("");
*/

      for (i = 0; i < 256; i++)
      {
         if (done[i])
            loBytes[i] = null;
         else
         {
            //System.out.print(i + ", ");
            String tmp;
            Integer ind;

            tmp = "{\n   0x" + Long.toHexString(loBytes[i][0]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][1]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][2]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][3]) + "L\n};";

            if ((ind = (Integer)lohiByteTab.get(tmp)) == null)
            {
               allBitVectors.add(tmp);

               if (!AllBitsSet(tmp))
                  if (codeGenerator.isJavaLanguage()) {
                    codeGenerator.genCodeLine("static final " + Options.getLongType() + "[] jjbitVec" +  lohiByteCnt + " = " + tmp);
                  } else {
                    codeGenerator.switchToStaticsFile();
                    codeGenerator.genCodeLine("static const " + Options.getLongType() + " jjbitVec" +  lohiByteCnt + "[] = " + tmp);
                  }
               lohiByteTab.put(tmp, ind = Integer.valueOf(lohiByteCnt++));
            }

            if (loByteVec == null)
               loByteVec = new Vector();

            loByteVec.add(Integer.valueOf(i));
            loByteVec.add(ind);
         }
      }
      //System.out.println("");
      UpdateDuplicateNonAsciiMoves();
   }

   private void UpdateDuplicateNonAsciiMoves()
   {
      for (int i = 0; i < nonAsciiTableForMethod.size(); i++)
      {
         NfaState tmp = (NfaState)nonAsciiTableForMethod.get(i);
         if (EqualLoByteVectors(loByteVec, tmp.loByteVec) &&
             EqualNonAsciiMoveIndices(nonAsciiMoveIndices, tmp.nonAsciiMoveIndices))
         {
            nonAsciiMethod = i;
            return;
         }
      }

      nonAsciiMethod = nonAsciiTableForMethod.size();
      nonAsciiTableForMethod.add(this);
   }

   private static boolean EqualLoByteVectors(List vec1, List vec2)
   {
      if (vec1 == null || vec2 == null)
         return false;

      if (vec1 == vec2)
         return true;

      if (vec1.size() != vec2.size())
         return false;

      for (int i = 0; i < vec1.size(); i++)
      {
         if (((Integer)vec1.get(i)).intValue() !=
             ((Integer)vec2.get(i)).intValue())
            return false;
      }

      return true;
   }

   private static boolean EqualNonAsciiMoveIndices(int[] moves1, int[] moves2)
   {
      if (moves1 == moves2)
         return true;

      if (moves1 == null || moves2 == null)
         return false;

      if (moves1.length != moves2.length)
         return false;

      for (int i = 0; i < moves1.length; i++)
      {
         if (moves1[i] != moves2[i])
            return false;
      }

      return true;
   }

   static String allBits = "{\n   0xffffffffffffffffL, " +
                    "0xffffffffffffffffL, " +
                    "0xffffffffffffffffL, " +
                    "0xffffffffffffffffL\n};";

   static boolean AllBitsSet(String bitVec)
   {
      return bitVec.equals(allBits);
   }

   static int AddStartStateSet(String stateSetString)
   {
      return AddCompositeStateSet(stateSetString, true);
   }

   private static int AddCompositeStateSet(String stateSetString, boolean starts)
   {
      Integer stateNameToReturn;

      if ((stateNameToReturn = (Integer)stateNameForComposite.get(stateSetString)) != null)
         return stateNameToReturn.intValue();

      int toRet = 0;
      int[] nameSet = (int[])allNextStates.get(stateSetString);

      if (!starts)
         stateBlockTable.put(stateSetString, stateSetString);

      if (nameSet == null)
         throw new Error("JavaCC Bug: Please file a bug at: https://github.com/javacc/javacc/issues");

      if (nameSet.length == 1)
      {
         stateNameToReturn = Integer.valueOf(nameSet[0]);
         stateNameForComposite.put(stateSetString, stateNameToReturn);
         return nameSet[0];
      }

      for (int i = 0; i < nameSet.length; i++)
      {
         if (nameSet[i] == -1)
            continue;

         NfaState st = (NfaState)indexedAllStates.get(nameSet[i]);
         st.isComposite = true;
         st.compositeStates = nameSet;
      }

      while (toRet < nameSet.length &&
             (starts && ((NfaState)indexedAllStates.get(nameSet[toRet])).inNextOf > 1))
         toRet++;

      Enumeration<String> e = compositeStateTable.keys();
      String s;
      while (e.hasMoreElements())
      {
         s = (String)e.nextElement();
         if (!s.equals(stateSetString) && Intersect(stateSetString, s))
         {
            int[] other = (int[])compositeStateTable.get(s);

            while (toRet < nameSet.length &&
                   ((starts && ((NfaState)indexedAllStates.get(nameSet[toRet])).inNextOf > 1) ||
                    ElemOccurs(nameSet[toRet], other) >= 0))
               toRet++;
         }
      }

      int tmp;

      if (toRet >= nameSet.length)
      {
         if (dummyStateIndex == -1)
            tmp = dummyStateIndex = generatedStates;
         else
            tmp = ++dummyStateIndex;

        // TODO(sreeni) : Fix this
        if (Options.getTokenManagerCodeGenerator() != null) {
          NfaState dummyState = new NfaState();
          dummyState.isComposite = true;
          dummyState.compositeStates = nameSet;
          dummyState.stateName = tmp;
        }
      }
      else
         tmp = nameSet[toRet];

      stateNameToReturn = Integer.valueOf(tmp);
      stateNameForComposite.put(stateSetString, stateNameToReturn);
      compositeStateTable.put(stateSetString, nameSet);

      return tmp;
   }

   private static int StateNameForComposite(String stateSetString)
   {
      return ((Integer)stateNameForComposite.get(stateSetString)).intValue();
   }

   static int InitStateName()
   {
      String s = Main.lg.initialState.GetEpsilonMovesString();

      if (Main.lg.initialState.usefulEpsilonMoves != 0)
         return StateNameForComposite(s);
      return -1;
   }

   public int GenerateInitMoves(CodeGenerator codeGenerator)
   {
      GetEpsilonMovesString();

      if (epsilonMovesString == null)
         epsilonMovesString = "null;";

      return AddStartStateSet(epsilonMovesString);
   }

   static Hashtable tableToDump = new Hashtable();
   static List orderedStateSet = new ArrayList();

   static int lastIndex = 0;
   private static int[] GetStateSetIndicesForUse(String arrayString)
   {
      int[] ret;
      int[] set = (int[])allNextStates.get(arrayString);

      if ((ret = (int[])tableToDump.get(arrayString)) == null)
      {
         ret = new int[2];
         ret[0] = lastIndex;
         ret[1] = lastIndex + set.length - 1;
         lastIndex += set.length;
         tableToDump.put(arrayString, ret);
         orderedStateSet.add(set);
      }

      return ret;
   }

   public static void DumpStateSets(CodeGenerator codeGenerator)
   {
      int cnt = 0;

      if (codeGenerator.isJavaLanguage()) {
        codeGenerator.genCode("static final int[] jjnextStates = {");
      } else {
        codeGenerator.switchToStaticsFile();
        codeGenerator.genCode("static const int jjnextStates[] = {");
      }
      if (orderedStateSet.size() > 0)
    	  for (int i = 0; i < orderedStateSet.size(); i++)
    	  {
    		  int[] set = (int[])orderedStateSet.get(i);

    		  for (int j = 0; j < set.length; j++)
    		  {
    			  if (cnt++ % 16  == 0)
    				  codeGenerator.genCode("\n   ");

    			  codeGenerator.genCode(set[j] + ", ");
    		  }
    	  }
      else
    	  codeGenerator.genCode("0");

      codeGenerator.genCodeLine("\n};");
      if (!codeGenerator.isJavaLanguage()) {
        codeGenerator.switchToMainFile();
      }
   }

   static String GetStateSetString(int[] states)
   {
      String retVal = "{ ";
      for (int i = 0; i < states.length; )
      {
         retVal += states[i] + ", ";

         if (i++ > 0 && i % 16 == 0)
            retVal += "\n";
      }

      retVal += "};";
      allNextStates.put(retVal, states);
      return retVal;
   }

   static String GetStateSetString(List states)
   {
      if (states == null || states.size() == 0)
         return "null;";

      int[] set = new int[states.size()];
      String retVal = "{ ";
      for (int i = 0; i < states.size(); )
      {
         int k;
         retVal += (k = ((NfaState)states.get(i)).stateName) + ", ";
         set[i] = k;

         if (i++ > 0 && i % 16 == 0)
            retVal += "\n";
      }

      retVal += "};";
      allNextStates.put(retVal, set);
      return retVal;
   }

   static int NumberOfBitsSet(long l)
   {
      int ret = 0;
      for (int i = 0; i < 63; i++)
         if (((l >> i) & 1L) != 0L)
            ret++;

      return ret;
   }

   static int OnlyOneBitSet(long l)
   {
      int oneSeen = -1;
      for (int i = 0; i < 64; i++)
         if (((l >> i) & 1L) != 0L)
         {
            if (oneSeen >= 0)
               return -1;
            oneSeen = i;
         }

      return oneSeen;
   }

   private static int ElemOccurs(int elem, int[] arr)
   {
      for (int i = arr.length; i-- > 0;)
         if (arr[i] == elem)
            return i;

      return -1;
   }

   private boolean FindCommonBlocks()
   {
      if (next == null || next.usefulEpsilonMoves <= 1)
         return false;

      if (stateDone == null)
         stateDone = new boolean[generatedStates];

      String set = next.epsilonMovesString;

      int[] nameSet = (int[])allNextStates.get(set);

      if (nameSet.length <= 2 || compositeStateTable.get(set) != null)
         return false;

      int i;
      int freq[] = new int[nameSet.length];
      boolean live[] = new boolean[nameSet.length];
      int[] count = new int[allNextStates.size()];

      for (i = 0; i < nameSet.length; i++)
      {
         if (nameSet[i] != -1)
         {
           live[i] = !stateDone[nameSet[i]];
           if (live[i])
             count[0]++;
         }
      }

      int j, blockLen = 0, commonFreq = 0;
      Enumeration e = allNextStates.keys();
      boolean needUpdate;

      while (e.hasMoreElements())
      {
         int[] tmpSet = (int[])allNextStates.get(e.nextElement());
         if (tmpSet == nameSet)
            continue;

         needUpdate = false;
         for (j = 0; j < nameSet.length; j++)
         {
            if (nameSet[j] == -1)
               continue;

            if (live[j] && ElemOccurs(nameSet[j], tmpSet) >= 0)
            {
               if (!needUpdate)
               {
                  needUpdate = true;
                  commonFreq++;
               }

               count[freq[j]]--;
               count[commonFreq]++;
               freq[j] = commonFreq;
            }
         }

         if (needUpdate)
         {
            int foundFreq = -1;
            blockLen = 0;

            for (j = 0; j <= commonFreq; j++)
               if (count[j] > blockLen)
               {
                  foundFreq = j;
                  blockLen = count[j];
               }

            if (blockLen <= 1)
               return false;

            for (j = 0; j < nameSet.length; j++)
               if (nameSet[j] != -1 && freq[j] != foundFreq)
               {
                  live[j] = false;
                  count[freq[j]]--;
               }
         }
      }

      if (blockLen <= 1)
         return false;

      int[] commonBlock = new int[blockLen];
      int cnt = 0;
      //System.out.println("Common Block for " + set + " :");
      for (i = 0; i < nameSet.length; i++)
      {
         if (live[i])
         {
            if (((NfaState)indexedAllStates.get(nameSet[i])).isComposite)
               return false;

            stateDone[nameSet[i]] = true;
            commonBlock[cnt++] = nameSet[i];
            //System.out.print(nameSet[i] + ", ");
         }
      }

      //System.out.println("");

      String s = GetStateSetString(commonBlock);
      e = allNextStates.keys();

      Outer :
      while (e.hasMoreElements())
      {
         int at;
         boolean firstOne = true;
         String stringToFix;
         int[] setToFix = (int[])allNextStates.get(stringToFix = (String)e.nextElement());

         if (setToFix == commonBlock)
            continue;

         for (int k = 0; k < cnt; k++)
         {
            if ((at = ElemOccurs(commonBlock[k], setToFix)) >= 0)
            {
               if (!firstOne)
                  setToFix[at] = -1;
               firstOne = false;
            }
            else
               continue Outer;
         }

         if (stateSetsToFix.get(stringToFix) == null)
            stateSetsToFix.put(stringToFix, setToFix);
      }

      next.usefulEpsilonMoves -= blockLen - 1;
      AddCompositeStateSet(s, false);
      return true;
   }

   private boolean CheckNextOccursTogether()
   {
      if (next == null || next.usefulEpsilonMoves <= 1)
         return true;

      String set = next.epsilonMovesString;

      int[] nameSet = (int[])allNextStates.get(set);

      if (nameSet.length == 1 || compositeStateTable.get(set) != null ||
          stateSetsToFix.get(set) != null)
         return false;

      int i;
      Hashtable occursIn = new Hashtable();
      NfaState tmp = (NfaState)allStates.get(nameSet[0]);

      for (i = 1; i < nameSet.length; i++)
      {
         NfaState tmp1 = (NfaState)allStates.get(nameSet[i]);

         if (tmp.inNextOf != tmp1.inNextOf)
            return false;
      }

      int isPresent, j;
      Enumeration e = allNextStates.keys();
      while (e.hasMoreElements())
      {
         String s;
         int[] tmpSet = (int[])allNextStates.get(s = (String)e.nextElement());

         if (tmpSet == nameSet)
            continue;

         isPresent = 0;
         for (j = 0; j < nameSet.length; j++)
         {
            if (ElemOccurs(nameSet[j], tmpSet) >= 0)
               isPresent++;
            else if (isPresent > 0)
               return false;
         }

         if (isPresent == j)
         {
            if (tmpSet.length > nameSet.length)
               occursIn.put(s, tmpSet);

            //May not need. But safe.
            if (compositeStateTable.get(s) != null ||
                stateSetsToFix.get(s) != null)
               return false;
         }
         else if (isPresent != 0)
            return false;
      }

      e = occursIn.keys();
      while (e.hasMoreElements())
      {
         String s;
         int[] setToFix = (int[])occursIn.get(s = (String)e.nextElement());

         if (stateSetsToFix.get(s) == null)
            stateSetsToFix.put(s, setToFix);

         for (int k = 0; k < setToFix.length; k++)
            if (ElemOccurs(setToFix[k], nameSet) > 0)  // Not >= since need the first one (0)
               setToFix[k] = -1;
      }

      next.usefulEpsilonMoves = 1;
      AddCompositeStateSet(next.epsilonMovesString, false);
      return true;
   }

   private static void FixStateSets()
   {
      Hashtable fixedSets = new Hashtable();
      Enumeration e = stateSetsToFix.keys();
      int[] tmp = new int[generatedStates];
      int i;

      while (e.hasMoreElements())
      {
         String s;
         int[] toFix = (int[])stateSetsToFix.get(s = (String)e.nextElement());
         int cnt = 0;

         //System.out.print("Fixing : ");
         for (i = 0; i < toFix.length; i++)
         {
            //System.out.print(toFix[i] + ", ");
            if (toFix[i] != -1)
               tmp[cnt++] = toFix[i];
         }

         int[] fixed = new int[cnt];
         System.arraycopy(tmp, 0, fixed, 0, cnt);
         fixedSets.put(s, fixed);
         allNextStates.put(s, fixed);
         //System.out.println(" as " + GetStateSetString(fixed));
      }

      for (i = 0; i < allStates.size(); i++)
      {
         NfaState tmpState = (NfaState)allStates.get(i);
         int[] newSet;

         if (tmpState.next == null || tmpState.next.usefulEpsilonMoves == 0)
            continue;

         /*if (compositeStateTable.get(tmpState.next.epsilonMovesString) != null)
            tmpState.next.usefulEpsilonMoves = 1;
         else*/ if ((newSet = (int[])fixedSets.get(tmpState.next.epsilonMovesString)) != null)
            tmpState.FixNextStates(newSet);
      }
   }

   private final void FixNextStates(int[] newSet)
   {
      next.usefulEpsilonMoves = newSet.length;
      //next.epsilonMovesString = GetStateSetString(newSet);
   }

   private static boolean Intersect(String set1, String set2)
   {
      if (set1 == null || set2 == null)
         return false;

      int[] nameSet1 = (int[])allNextStates.get(set1);
      int[] nameSet2 = (int[])allNextStates.get(set2);

      if (nameSet1 == null || nameSet2 == null)
         return false;

      if (nameSet1 == nameSet2)
         return true;

      for (int i = nameSet1.length; i-- > 0; )
         for (int j = nameSet2.length; j-- > 0; )
            if (nameSet1[i] == nameSet2[j])
               return true;

      return false;
   }

   private static void DumpHeadForCase(CodeGenerator codeGenerator, int byteNum)
   {
      if (byteNum == 0) {
         codeGenerator.genCodeLine("         " + Options.getLongType() + " l = 1L << curChar;");
         if (!codeGenerator.isJavaLanguage()) {
           codeGenerator.genCodeLine("         (void)l;");
         }
      } else if (byteNum == 1) {
         codeGenerator.genCodeLine("         " + Options.getLongType() + " l = 1L << (curChar & 077);");
         if (!codeGenerator.isJavaLanguage()) {
           codeGenerator.genCodeLine("         (void)l;");
         }
      } else {
         if (Options.getJavaUnicodeEscape() || unicodeWarningGiven)
         {
           codeGenerator.genCodeLine("         int hiByte = (curChar >> 8);");
           codeGenerator.genCodeLine("         int i1 = hiByte >> 6;");
           codeGenerator.genCodeLine("         " + Options.getLongType() + " l1 = 1L << (hiByte & 077);");
         }

         codeGenerator.genCodeLine("         int i2 = (curChar & 0xff) >> 6;");
         codeGenerator.genCodeLine("         " + Options.getLongType() + " l2 = 1L << (curChar & 077);");
      }

      //codeGenerator.genCodeLine("         MatchLoop: do");
      codeGenerator.genCodeLine("         do");
      codeGenerator.genCodeLine("         {");

      codeGenerator.genCodeLine("            switch(jjstateSet[--i])");
      codeGenerator.genCodeLine("            {");
   }

   private static Vector PartitionStatesSetForAscii(int[] states, int byteNum)
   {
      int[] cardinalities = new int[states.length];
      Vector original = new Vector();
      Vector partition = new Vector();
      NfaState tmp;

      original.setSize(states.length);
      int cnt = 0;
      for (int i = 0; i < states.length; i++)
      {
         tmp = (NfaState)allStates.get(states[i]);

         if (tmp.asciiMoves[byteNum] != 0L)
         {
            int j;
            int p = NumberOfBitsSet(tmp.asciiMoves[byteNum]);

            for (j = 0; j < i; j++)
               if (cardinalities[j] <= p)
                  break;

            for (int k = i; k > j; k--)
               cardinalities[k] = cardinalities[k - 1];

            cardinalities[j] = p;

            original.insertElementAt(tmp, j);
            cnt++;
         }
      }

      original.setSize(cnt);

      while (original.size() > 0)
      {
         tmp = (NfaState)original.get(0);
         original.removeElement(tmp);

         long bitVec = tmp.asciiMoves[byteNum];
         List<NfaState> subSet = new ArrayList<NfaState>();
         subSet.add(tmp);

         for (int j = 0; j < original.size(); j++)
         {
            NfaState tmp1 = (NfaState)original.get(j);

            if ((tmp1.asciiMoves[byteNum] & bitVec) == 0L)
            {
               bitVec |= tmp1.asciiMoves[byteNum];
               subSet.add(tmp1);
               original.removeElementAt(j--);
            }
         }

         partition.add(subSet);
      }

      return partition;
   }

   private String PrintNoBreak(CodeGenerator codeGenerator, int byteNum, boolean[] dumped)
   {
      if (inNextOf != 1)
         throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu");

      dumped[stateName] = true;

      if (byteNum >= 0)
      {
         if (asciiMoves[byteNum] != 0L)
         {
            codeGenerator.genCodeLine("               case " + stateName + ":");
            DumpAsciiMoveForCompositeState(codeGenerator, byteNum, false);
            return "";
         }
      }
      else if (nonAsciiMethod != -1)
      {
         codeGenerator.genCodeLine("               case " + stateName + ":");
         DumpNonAsciiMoveForCompositeState(codeGenerator);
         return "";
      }

      return ("               case " + stateName + ":\n");
   }

   private static void DumpCompositeStatesAsciiMoves(CodeGenerator codeGenerator,
                                String key, int byteNum, boolean[] dumped)
   {
      int i;

      int[] nameSet = (int[])allNextStates.get(key);

      if (nameSet.length == 1 || dumped[StateNameForComposite(key)])
         return;

      NfaState toBePrinted = null;
      int neededStates = 0;
      NfaState tmp;
      NfaState stateForCase = null;
      String toPrint = "";
      boolean stateBlock = (stateBlockTable.get(key) != null);

      for (i = 0; i < nameSet.length; i++)
      {
         tmp = (NfaState)allStates.get(nameSet[i]);

         if (tmp.asciiMoves[byteNum] != 0L)
         {
            if (neededStates++ == 1)
               break;
            else
               toBePrinted = tmp;
         }
         else
            dumped[tmp.stateName] = true;

         if (tmp.stateForCase != null)
         {
            if (stateForCase != null)
               throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu : ");

            stateForCase = tmp.stateForCase;
         }
      }

      if (stateForCase != null)
         toPrint = stateForCase.PrintNoBreak(codeGenerator, byteNum, dumped);

      if (neededStates == 0)
      {
         if (stateForCase != null && toPrint.equals(""))
            codeGenerator.genCodeLine("                  break;");
         return;
      }

      if (neededStates == 1)
      {
         //if (byteNum == 1)
            //System.out.println(toBePrinted.stateName + " is the only state for "
               //+ key + " ; and key is : " + StateNameForComposite(key));

         if (!toPrint.equals(""))
            codeGenerator.genCode(toPrint);

         codeGenerator.genCodeLine("               case " + StateNameForComposite(key) + ":");

         if (!dumped[toBePrinted.stateName] && !stateBlock && toBePrinted.inNextOf > 1)
            codeGenerator.genCodeLine("               case " + toBePrinted.stateName + ":");

         dumped[toBePrinted.stateName] = true;
         toBePrinted.DumpAsciiMove(codeGenerator, byteNum, dumped);
         return;
      }

      List partition = PartitionStatesSetForAscii(nameSet, byteNum);

      if (!toPrint.equals(""))
         codeGenerator.genCode(toPrint);

      int keyState = StateNameForComposite(key);
      codeGenerator.genCodeLine("               case " + keyState + ":");
      if (keyState < generatedStates)
         dumped[keyState] = true;

      for (i = 0; i < partition.size(); i++)
      {
         List subSet = (List)partition.get(i);

         for (int j = 0; j < subSet.size(); j++)
         {
            tmp = (NfaState)subSet.get(j);

            if (stateBlock)
               dumped[tmp.stateName] = true;
            tmp.DumpAsciiMoveForCompositeState(codeGenerator, byteNum, j != 0);
         }
      }

      if (stateBlock)
         codeGenerator.genCodeLine("                  break;");
      else
         codeGenerator.genCodeLine("                  break;");
   }

   private boolean selfLoop()
   {
      if (next == null || next.epsilonMovesString == null)
         return false;

      int[] set = (int[])allNextStates.get(next.epsilonMovesString);
      return ElemOccurs(stateName, set) >= 0;
   }

   private void DumpAsciiMoveForCompositeState(CodeGenerator codeGenerator, int byteNum, boolean elseNeeded)
   {
      boolean nextIntersects = selfLoop();

      for (int j = 0; j < allStates.size(); j++)
      {
         NfaState temp1 = (NfaState)allStates.get(j);

         if (this == temp1 || temp1.stateName == -1 || temp1.dummy ||
             stateName == temp1.stateName || temp1.asciiMoves[byteNum] == 0L)
            continue;

         if (!nextIntersects && Intersect(temp1.next.epsilonMovesString,
                                         next.epsilonMovesString))
         {
            nextIntersects = true;
            break;
         }
      }

      //System.out.println(stateName + " \'s nextIntersects : " + nextIntersects);
      String prefix = "";
      if (asciiMoves[byteNum] != 0xffffffffffffffffL)
      {
         int oneBit = OnlyOneBitSet(asciiMoves[byteNum]);

         if (oneBit != -1)
            codeGenerator.genCodeLine("                  " + (elseNeeded ? "else " : "") + "if (curChar == " +
                    (64 * byteNum + oneBit) + ")");
         else
            codeGenerator.genCodeLine("                  " + (elseNeeded ? "else " : "") +
                    "if ((0x" + Long.toHexString(asciiMoves[byteNum]) + "L & l) != 0L)");
         prefix = "   ";
      }

      if (kindToPrint != Integer.MAX_VALUE)
      {
         if (asciiMoves[byteNum] != 0xffffffffffffffffL)
         {
            codeGenerator.genCodeLine("                  {");
         }

         codeGenerator.genCodeLine(prefix + "                  if (kind > " + kindToPrint + ")");
         codeGenerator.genCodeLine(prefix + "                     kind = " + kindToPrint + ";");
      }

      if (next != null && next.usefulEpsilonMoves > 0)
      {
         int[] stateNames = (int[])allNextStates.get(
                                          next.epsilonMovesString);
         if (next.usefulEpsilonMoves == 1)
         {
            int name = stateNames[0];

            if (nextIntersects)
               codeGenerator.genCodeLine(prefix + "                  { jjCheckNAdd(" + name + "); }");
            else
               codeGenerator.genCodeLine(prefix + "                  jjstateSet[jjnewStateCnt++] = " + name + ";");
         }
         else if (next.usefulEpsilonMoves == 2 && nextIntersects)
         {
            codeGenerator.genCodeLine(prefix + "                  { jjCheckNAddTwoStates(" +
               stateNames[0] + ", " + stateNames[1] + "); }");
         }
         else
         {
            int[] indices = GetStateSetIndicesForUse(next.epsilonMovesString);
            boolean notTwo = (indices[0] + 1 != indices[1]);

            if (nextIntersects) {
              codeGenerator.genCode(prefix + "                  { jjCheckNAddStates(" + indices[0]);
              if (notTwo) {
                jjCheckNAddStatesDualNeeded = true;
                codeGenerator.genCode(", " + indices[1]);
              } else {
                jjCheckNAddStatesUnaryNeeded = true;
              }
              codeGenerator.genCodeLine("); }");
            } else
               codeGenerator.genCodeLine(prefix + "                  { jjAddStates(" +
                                     indices[0] + ", " + indices[1] + "); }");
         }
      }

      if (asciiMoves[byteNum] != 0xffffffffffffffffL && kindToPrint != Integer.MAX_VALUE)
         codeGenerator.genCodeLine("                  }");
   }

   private void DumpAsciiMove(CodeGenerator codeGenerator, int byteNum, boolean dumped[])
   {
      boolean nextIntersects = selfLoop() && isComposite;
      boolean onlyState = true;

      for (int j = 0; j < allStates.size(); j++)
      {
         NfaState temp1 = (NfaState)allStates.get(j);

         if (this == temp1 || temp1.stateName == -1 || temp1.dummy ||
             stateName == temp1.stateName || temp1.asciiMoves[byteNum] == 0L)
            continue;

         if (onlyState && (asciiMoves[byteNum] & temp1.asciiMoves[byteNum]) != 0L)
            onlyState = false;

         if (!nextIntersects && Intersect(temp1.next.epsilonMovesString,
                                         next.epsilonMovesString))
            nextIntersects = true;

         if (!dumped[temp1.stateName] && !temp1.isComposite &&
             asciiMoves[byteNum] == temp1.asciiMoves[byteNum] &&
             kindToPrint == temp1.kindToPrint &&
             (next.epsilonMovesString == temp1.next.epsilonMovesString ||
              (next.epsilonMovesString != null &&
               temp1.next.epsilonMovesString != null &&
               next.epsilonMovesString.equals(
                            temp1.next.epsilonMovesString))))
         {
            dumped[temp1.stateName] = true;
            codeGenerator.genCodeLine("               case " + temp1.stateName + ":");
         }
      }

      //if (onlyState)
         //nextIntersects = false;

      int oneBit = OnlyOneBitSet(asciiMoves[byteNum]);
      if (asciiMoves[byteNum] != 0xffffffffffffffffL)
      {
         if ((next == null || next.usefulEpsilonMoves == 0) &&
             kindToPrint != Integer.MAX_VALUE)
         {
            String kindCheck = "";

            if (!onlyState)
               kindCheck = " && kind > " + kindToPrint;

            if (oneBit != -1)
               codeGenerator.genCodeLine("                  if (curChar == " +
                  (64 * byteNum + oneBit) + kindCheck + ")");
            else
               codeGenerator.genCodeLine("                  if ((0x" +
                   Long.toHexString(asciiMoves[byteNum]) +
                   "L & l) != 0L" + kindCheck + ")");

            codeGenerator.genCodeLine("                     kind = " + kindToPrint + ";");

            if (onlyState)
               codeGenerator.genCodeLine("                  break;");
            else
               codeGenerator.genCodeLine("                  break;");

            return;
         }
      }

      String prefix = "";
      if (kindToPrint != Integer.MAX_VALUE)
      {

         if (oneBit != -1)
         {
            codeGenerator.genCodeLine("                  if (curChar != " +
                    (64 * byteNum + oneBit) + ")");
            codeGenerator.genCodeLine("                     break;");
         }
         else if (asciiMoves[byteNum] != 0xffffffffffffffffL)
         {
            codeGenerator.genCodeLine("                  if ((0x" + Long.toHexString(asciiMoves[byteNum]) + "L & l) == 0L)");
            codeGenerator.genCodeLine("                     break;");
         }

         if (onlyState)
         {
            codeGenerator.genCodeLine("                  kind = " + kindToPrint + ";");
         }
         else
         {
            codeGenerator.genCodeLine("                  if (kind > " + kindToPrint + ")");
            codeGenerator.genCodeLine("                     kind = " + kindToPrint + ";");
         }
      }
      else
      {
         if (oneBit != -1)
         {
            codeGenerator.genCodeLine("                  if (curChar == " +
                    (64 * byteNum + oneBit) + ")");
            prefix = "   ";
         }
         else if (asciiMoves[byteNum] != 0xffffffffffffffffL)
         {
            codeGenerator.genCodeLine("                  if ((0x" + Long.toHexString(asciiMoves[byteNum]) + "L & l) != 0L)");
            prefix = "   ";
         }
      }

      if (next != null && next.usefulEpsilonMoves > 0)
      {
         int[] stateNames = (int[])allNextStates.get(
                                          next.epsilonMovesString);
         if (next.usefulEpsilonMoves == 1)
         {
            int name = stateNames[0];
            if (nextIntersects)
               codeGenerator.genCodeLine(prefix + "                  { jjCheckNAdd(" + name + "); }");
            else
               codeGenerator.genCodeLine(prefix + "                  jjstateSet[jjnewStateCnt++] = " + name + ";");
         }
         else if (next.usefulEpsilonMoves == 2 && nextIntersects)
         {
            codeGenerator.genCodeLine(prefix + "                  { jjCheckNAddTwoStates(" +
               stateNames[0] + ", " + stateNames[1] + "); }");
         }
         else
         {
            int[] indices = GetStateSetIndicesForUse(next.epsilonMovesString);
            boolean notTwo = (indices[0] + 1 != indices[1]);

            if (nextIntersects) {
              codeGenerator.genCode(prefix + "                  { jjCheckNAddStates(" + indices[0]);
              if (notTwo) {
                jjCheckNAddStatesDualNeeded = true;
                codeGenerator.genCode(", " + indices[1]);
              } else {
                jjCheckNAddStatesUnaryNeeded = true;
              }
              codeGenerator.genCodeLine("); }");
            } else
               codeGenerator.genCodeLine(prefix + "                  { jjAddStates(" +
                                     indices[0] + ", " + indices[1] + "); }");
         }
      }

      if (onlyState)
         codeGenerator.genCodeLine("                  break;");
      else
         codeGenerator.genCodeLine("                  break;");
   }

   private static void DumpAsciiMoves(CodeGenerator codeGenerator, int byteNum)
   {
      boolean[] dumped = new boolean[Math.max(generatedStates, dummyStateIndex + 1)];
      Enumeration e = compositeStateTable.keys();

      DumpHeadForCase(codeGenerator, byteNum);

      while (e.hasMoreElements())
         DumpCompositeStatesAsciiMoves(codeGenerator, (String)e.nextElement(), byteNum, dumped);

      for (int i = 0; i < allStates.size(); i++)
      {
         NfaState temp = (NfaState)allStates.get(i);

         if (dumped[temp.stateName] || temp.lexState != Main.lg.lexStateIndex ||
             !temp.HasTransitions() || temp.dummy ||
             temp.stateName == -1)
            continue;

         String toPrint = "";

         if (temp.stateForCase != null)
         {
            if (temp.inNextOf == 1)
               continue;

            if (dumped[temp.stateForCase.stateName])
               continue;

            toPrint = (temp.stateForCase.PrintNoBreak(codeGenerator, byteNum, dumped));

            if (temp.asciiMoves[byteNum] == 0L)
            {
               if (toPrint.equals(""))
                  codeGenerator.genCodeLine("                  break;");

               continue;
            }
         }

         if (temp.asciiMoves[byteNum] == 0L)
            continue;

         if (!toPrint.equals(""))
            codeGenerator.genCode(toPrint);

         dumped[temp.stateName] = true;
         codeGenerator.genCodeLine("               case " + temp.stateName + ":");
         temp.DumpAsciiMove(codeGenerator, byteNum, dumped);
      }

      if (byteNum != 0 && byteNum != 1) {
        codeGenerator.genCodeLine("               default : if (i1 == 0 || l1 == 0 || i2 == 0 ||  l2 == 0) break; else break;");
      } else {
        codeGenerator.genCodeLine("               default : break;");
      }

      codeGenerator.genCodeLine("            }");
      codeGenerator.genCodeLine("         } while(i != startsAt);");
   }

   private static void DumpCompositeStatesNonAsciiMoves(CodeGenerator codeGenerator,
                                      String key, boolean[] dumped)
   {
      int i;
      int[] nameSet = (int[])allNextStates.get(key);

      if (nameSet.length == 1 || dumped[StateNameForComposite(key)])
         return;

      NfaState toBePrinted = null;
      int neededStates = 0;
      NfaState tmp;
      NfaState stateForCase = null;
      String toPrint = "";
      boolean stateBlock = (stateBlockTable.get(key) != null);

      for (i = 0; i < nameSet.length; i++)
      {
         tmp = (NfaState)allStates.get(nameSet[i]);

         if (tmp.nonAsciiMethod != -1)
         {
            if (neededStates++ == 1)
               break;
            else
               toBePrinted = tmp;
         }
         else
            dumped[tmp.stateName] = true;

         if (tmp.stateForCase != null)
         {
            if (stateForCase != null)
               throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu : ");

            stateForCase = tmp.stateForCase;
         }
      }

      if (stateForCase != null)
         toPrint = stateForCase.PrintNoBreak(codeGenerator, -1, dumped);

      if (neededStates == 0)
      {
         if (stateForCase != null && toPrint.equals(""))
            codeGenerator.genCodeLine("                  break;");

         return;
      }

      if (neededStates == 1)
      {
         if (!toPrint.equals(""))
            codeGenerator.genCode(toPrint);

         codeGenerator.genCodeLine("               case " + StateNameForComposite(key) + ":");

         if (!dumped[toBePrinted.stateName] && !stateBlock && toBePrinted.inNextOf > 1)
            codeGenerator.genCodeLine("               case " + toBePrinted.stateName + ":");

         dumped[toBePrinted.stateName] = true;
         toBePrinted.DumpNonAsciiMove(codeGenerator, dumped);
         return;
      }

      if (!toPrint.equals(""))
         codeGenerator.genCode(toPrint);

      int keyState = StateNameForComposite(key);
      codeGenerator.genCodeLine("               case " + keyState + ":");
      if (keyState < generatedStates)
         dumped[keyState] = true;

      for (i = 0; i < nameSet.length; i++)
      {
         tmp = (NfaState)allStates.get(nameSet[i]);

         if (tmp.nonAsciiMethod != -1)
         {
            if (stateBlock)
               dumped[tmp.stateName] = true;
            tmp.DumpNonAsciiMoveForCompositeState(codeGenerator);
         }
      }

      if (stateBlock)
         codeGenerator.genCodeLine("                  break;");
      else
         codeGenerator.genCodeLine("                  break;");
   }

   private final void DumpNonAsciiMoveForCompositeState(CodeGenerator codeGenerator)
   {
      boolean nextIntersects = selfLoop();
      for (int j = 0; j < allStates.size(); j++)
      {
         NfaState temp1 = (NfaState)allStates.get(j);

         if (this == temp1 || temp1.stateName == -1 || temp1.dummy ||
             stateName == temp1.stateName || (temp1.nonAsciiMethod == -1))
            continue;

         if (!nextIntersects && Intersect(temp1.next.epsilonMovesString,
                                         next.epsilonMovesString))
         {
            nextIntersects = true;
            break;
         }
      }

      if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
      {
         if (loByteVec != null && loByteVec.size() > 1)
            codeGenerator.genCodeLine("                  if ((jjbitVec" +
             ((Integer)loByteVec.get(1)).intValue() + "[i2" +
                "] & l2) != 0L)");
      }
      else
      {
         codeGenerator.genCodeLine("                  if (jjCanMove_" + nonAsciiMethod +
                                                "(hiByte, i1, i2, l1, l2))");
      }

      if (kindToPrint != Integer.MAX_VALUE)
      {
         codeGenerator.genCodeLine("                  {");
         codeGenerator.genCodeLine("                     if (kind > " + kindToPrint + ")");
         codeGenerator.genCodeLine("                        kind = " + kindToPrint + ";");
      }

      if (next != null && next.usefulEpsilonMoves > 0)
      {
         int[] stateNames = (int[])allNextStates.get(
                                          next.epsilonMovesString);
         if (next.usefulEpsilonMoves == 1)
         {
            int name = stateNames[0];
            if (nextIntersects)
               codeGenerator.genCodeLine("                     { jjCheckNAdd(" + name + "); }");
            else
               codeGenerator.genCodeLine("                     jjstateSet[jjnewStateCnt++] = " + name + ";");
         }
         else if (next.usefulEpsilonMoves == 2 && nextIntersects)
         {
            codeGenerator.genCodeLine("                     { jjCheckNAddTwoStates(" +
               stateNames[0] + ", " + stateNames[1] + "); }");
         }
         else
         {
            int[] indices = GetStateSetIndicesForUse(next.epsilonMovesString);
            boolean notTwo = (indices[0] + 1 != indices[1]);

            if (nextIntersects) {
              codeGenerator.genCode("                     { jjCheckNAddStates(" + indices[0]);
              if (notTwo) {
                jjCheckNAddStatesDualNeeded = true;
                codeGenerator.genCode(", " + indices[1]);
              } else {
                jjCheckNAddStatesUnaryNeeded = true;
              }
              codeGenerator.genCodeLine("); }");
            } else
              codeGenerator.genCodeLine("                     { jjAddStates(" + indices[0] + ", " + indices[1] + "); }");
         }
      }

      if (kindToPrint != Integer.MAX_VALUE)
         codeGenerator.genCodeLine("                  }");
   }

   private final void DumpNonAsciiMove(CodeGenerator codeGenerator, boolean dumped[])
   {
      boolean nextIntersects = selfLoop() && isComposite;

      for (int j = 0; j < allStates.size(); j++)
      {
         NfaState temp1 = (NfaState)allStates.get(j);

         if (this == temp1 || temp1.stateName == -1 || temp1.dummy ||
             stateName == temp1.stateName || (temp1.nonAsciiMethod == -1))
            continue;

         if (!nextIntersects && Intersect(temp1.next.epsilonMovesString,
                                         next.epsilonMovesString))
            nextIntersects = true;

         if (!dumped[temp1.stateName] && !temp1.isComposite &&
             nonAsciiMethod == temp1.nonAsciiMethod &&
             kindToPrint == temp1.kindToPrint &&
             (next.epsilonMovesString == temp1.next.epsilonMovesString ||
              (next.epsilonMovesString != null &&
               temp1.next.epsilonMovesString != null &&
               next.epsilonMovesString.equals(temp1.next.epsilonMovesString))))
         {
            dumped[temp1.stateName] = true;
            codeGenerator.genCodeLine("               case " + temp1.stateName + ":");
         }
      }

      if (next == null || next.usefulEpsilonMoves <= 0)
      {
         String kindCheck = " && kind > " + kindToPrint;

         if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
         {
            if (loByteVec != null && loByteVec.size() > 1)
               codeGenerator.genCodeLine("                  if ((jjbitVec" +
                ((Integer)loByteVec.get(1)).intValue() + "[i2" +
                   "] & l2) != 0L" + kindCheck + ")");
         }
         else
         {
            codeGenerator.genCodeLine("                  if (jjCanMove_" + nonAsciiMethod +
                              "(hiByte, i1, i2, l1, l2)" + kindCheck + ")");
         }
         codeGenerator.genCodeLine("                     kind = " + kindToPrint + ";");
         codeGenerator.genCodeLine("                  break;");
         return;
      }

      String prefix = "   ";
      if (kindToPrint != Integer.MAX_VALUE)
      {
         if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
         {
            if (loByteVec != null && loByteVec.size() > 1)
            {
               codeGenerator.genCodeLine("                  if ((jjbitVec" +
                ((Integer)loByteVec.get(1)).intValue() + "[i2" +
                "] & l2) == 0L)");
               codeGenerator.genCodeLine("                     break;");
            }
         }
         else
         {
            codeGenerator.genCodeLine("                  if (!jjCanMove_" + nonAsciiMethod +
                                                      "(hiByte, i1, i2, l1, l2))");
            codeGenerator.genCodeLine("                     break;");
         }

         codeGenerator.genCodeLine("                  if (kind > " + kindToPrint + ")");
         codeGenerator.genCodeLine("                     kind = " + kindToPrint + ";");
         prefix = "";
      }
      else if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
      {
         if (loByteVec != null && loByteVec.size() > 1)
            codeGenerator.genCodeLine("                  if ((jjbitVec" +
             ((Integer)loByteVec.get(1)).intValue() + "[i2" +
                "] & l2) != 0L)");
      }
      else
      {
         codeGenerator.genCodeLine("                  if (jjCanMove_" + nonAsciiMethod +
                                                   "(hiByte, i1, i2, l1, l2))");
      }

      if (next != null && next.usefulEpsilonMoves > 0)
      {
         int[] stateNames = (int[])allNextStates.get(
                                          next.epsilonMovesString);
         if (next.usefulEpsilonMoves == 1)
         {
            int name = stateNames[0];
            if (nextIntersects)
               codeGenerator.genCodeLine(prefix + "                  { jjCheckNAdd(" + name + "); }");
            else
               codeGenerator.genCodeLine(prefix + "                  jjstateSet[jjnewStateCnt++] = " + name + ";");
         }
         else if (next.usefulEpsilonMoves == 2 && nextIntersects)
         {
            codeGenerator.genCodeLine(prefix + "                  { jjCheckNAddTwoStates(" +
               stateNames[0] + ", " + stateNames[1] + "); }");
         }
         else
         {
            int[] indices = GetStateSetIndicesForUse(next.epsilonMovesString);
            boolean notTwo = (indices[0] + 1 != indices[1]);

            if (nextIntersects) {
              codeGenerator.genCode(prefix + "                  { jjCheckNAddStates(" + indices[0]);
              if (notTwo) {
                jjCheckNAddStatesDualNeeded = true;
                codeGenerator.genCode(", " + indices[1]);
              } else {
                jjCheckNAddStatesUnaryNeeded = true;
              }
              codeGenerator.genCodeLine("); }");
            } else
              codeGenerator.genCodeLine(prefix + "                  { jjAddStates(" + indices[0] + ", " + indices[1] + "); }");
         }
      }

      codeGenerator.genCodeLine("                  break;");
   }

   public static void DumpCharAndRangeMoves(CodeGenerator codeGenerator)
   {
      boolean[] dumped = new boolean[Math.max(generatedStates, dummyStateIndex + 1)];
      Enumeration e = compositeStateTable.keys();
      int i;

      DumpHeadForCase(codeGenerator, -1);

      while (e.hasMoreElements())
         DumpCompositeStatesNonAsciiMoves(codeGenerator, (String)e.nextElement(), dumped);

      for (i = 0; i < allStates.size(); i++)
      {
         NfaState temp = (NfaState)allStates.get(i);

         if (temp.stateName == -1 || dumped[temp.stateName] || temp.lexState != Main.lg.lexStateIndex ||
             !temp.HasTransitions() || temp.dummy )
            continue;

         String toPrint = "";

         if (temp.stateForCase != null)
         {
            if (temp.inNextOf == 1)
               continue;

            if (dumped[temp.stateForCase.stateName])
               continue;

            toPrint = (temp.stateForCase.PrintNoBreak(codeGenerator, -1, dumped));

            if (temp.nonAsciiMethod == -1)
            {
               if (toPrint.equals(""))
                  codeGenerator.genCodeLine("                  break;");

               continue;
            }
         }

         if (temp.nonAsciiMethod == -1)
            continue;

         if (!toPrint.equals(""))
            codeGenerator.genCode(toPrint);

         dumped[temp.stateName] = true;
         //System.out.println("case : " + temp.stateName);
         codeGenerator.genCodeLine("               case " + temp.stateName + ":");
         temp.DumpNonAsciiMove(codeGenerator, dumped);
      }


	  if (Options.getJavaUnicodeEscape() || unicodeWarningGiven) {
	     codeGenerator.genCodeLine("               default : if (i1 == 0 || l1 == 0 || i2 == 0 ||  l2 == 0) break; else break;");
	  } else {
	     codeGenerator.genCodeLine("               default : break;");
      }
      codeGenerator.genCodeLine("            }");
      codeGenerator.genCodeLine("         } while(i != startsAt);");
   }

   public static void DumpNonAsciiMoveMethods(CodeGenerator codeGenerator)
   {
      if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
         return;

      if (nonAsciiTableForMethod.size() <= 0)
         return;

      for (int i = 0; i < nonAsciiTableForMethod.size(); i++)
      {
         NfaState tmp = (NfaState)nonAsciiTableForMethod.get(i);
         tmp.DumpNonAsciiMoveMethod(codeGenerator);
      }
   }

   void DumpNonAsciiMoveMethod(CodeGenerator codeGenerator)
   {
      int j;
      if (codeGenerator.isJavaLanguage()) {
        codeGenerator.genCodeLine("private static final " + Options.getBooleanType() + " jjCanMove_" + nonAsciiMethod +
                       "(int hiByte, int i1, int i2, " + Options.getLongType() + " l1, " + Options.getLongType() + " l2)");
      } else {
        codeGenerator.generateMethodDefHeader("" + Options.getBooleanType() + "", Main.lg.tokMgrClassName, "jjCanMove_" + nonAsciiMethod +
                       "(int hiByte, int i1, int i2, " + Options.getLongType() + " l1, " + Options.getLongType() + " l2)");
      }
      codeGenerator.genCodeLine("{");
      codeGenerator.genCodeLine("   switch(hiByte)");
      codeGenerator.genCodeLine("   {");

      if (loByteVec != null && loByteVec.size() > 0)
      {
         for (j = 0; j < loByteVec.size(); j += 2)
         {
            codeGenerator.genCodeLine("      case " +
                         ((Integer)loByteVec.get(j)).intValue() + ":");
            if (!AllBitsSet((String)allBitVectors.get(
                 ((Integer)loByteVec.get(j + 1)).intValue())))
            {
               codeGenerator.genCodeLine("         return ((jjbitVec" +
                ((Integer)loByteVec.get(j + 1)).intValue() + "[i2" +
                   "] & l2) != 0L);");
            }
            else
               codeGenerator.genCodeLine("            return true;");
         }
      }

      codeGenerator.genCodeLine("      default :");

      if (nonAsciiMoveIndices != null &&
          (j = nonAsciiMoveIndices.length) > 0)
      {
         do
         {
            if (!AllBitsSet((String)allBitVectors.get(
                               nonAsciiMoveIndices[j - 2])))
               codeGenerator.genCodeLine("         if ((jjbitVec" + nonAsciiMoveIndices[j - 2] +
                            "[i1] & l1) != 0L)");
            if (!AllBitsSet((String)allBitVectors.get(
                               nonAsciiMoveIndices[j - 1])))
            {
               codeGenerator.genCodeLine("            if ((jjbitVec" + nonAsciiMoveIndices[j - 1] +
                            "[i2] & l2) == 0L)");
               codeGenerator.genCodeLine("               return false;");
               codeGenerator.genCodeLine("            else");
            }
            codeGenerator.genCodeLine("            return true;");
         }
         while ((j -= 2) > 0);
      }

      codeGenerator.genCodeLine("         return false;");
      codeGenerator.genCodeLine("   }");
      codeGenerator.genCodeLine("}");
   }

   private static void ReArrange()
   {
      List v = allStates;
      allStates = new ArrayList(Collections.nCopies(generatedStates, null));

      if (allStates.size() != generatedStates) throw new Error("What??");

      for (int j = 0; j < v.size(); j++)
      {
         NfaState tmp = (NfaState)v.get(j);
         if (tmp.stateName != -1 && !tmp.dummy)
            allStates.set(tmp.stateName, tmp);
      }
   }

   //private static boolean boilerPlateDumped = false;
   static void PrintBoilerPlate(CodeGenerator codeGenerator)
   {
      codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private void " +
                   "jjCheckNAdd(int state)");
      codeGenerator.genCodeLine("{");
      codeGenerator.genCodeLine("   if (jjrounds[state] != jjround)");
      codeGenerator.genCodeLine("   {");
      codeGenerator.genCodeLine("      jjstateSet[jjnewStateCnt++] = state;");
      codeGenerator.genCodeLine("      jjrounds[state] = jjround;");
      codeGenerator.genCodeLine("   }");
      codeGenerator.genCodeLine("}");

      codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private void " +
                    "jjAddStates(int start, int end)");
      codeGenerator.genCodeLine("{");
      codeGenerator.genCodeLine("   do {");
      codeGenerator.genCodeLine("      jjstateSet[jjnewStateCnt++] = jjnextStates[start];");
      codeGenerator.genCodeLine("   } while (start++ != end);");
      codeGenerator.genCodeLine("}");

      codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private void " +
                    "jjCheckNAddTwoStates(int state1, int state2)");
      codeGenerator.genCodeLine("{");
      codeGenerator.genCodeLine("   jjCheckNAdd(state1);");
      codeGenerator.genCodeLine("   jjCheckNAdd(state2);");
      codeGenerator.genCodeLine("}");
      codeGenerator.genCodeLine("");

      if(jjCheckNAddStatesDualNeeded) {
        codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private void " +
                     "jjCheckNAddStates(int start, int end)");
        codeGenerator.genCodeLine("{");
        codeGenerator.genCodeLine("   do {");
        codeGenerator.genCodeLine("      jjCheckNAdd(jjnextStates[start]);");
        codeGenerator.genCodeLine("   } while (start++ != end);");
        codeGenerator.genCodeLine("}");
        codeGenerator.genCodeLine("");
      }

      if(jjCheckNAddStatesUnaryNeeded) {
        codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private void " +
                  "jjCheckNAddStates(int start)");
        codeGenerator.genCodeLine("{");
        codeGenerator.genCodeLine("   jjCheckNAdd(jjnextStates[start]);");
        codeGenerator.genCodeLine("   jjCheckNAdd(jjnextStates[start + 1]);");
        codeGenerator.genCodeLine("}");
        codeGenerator.genCodeLine("");
      }
   }

   //private static boolean boilerPlateDumped = false;
   static void PrintBoilerPlateCPP(CodeGenerator codeGenerator)
   {
      codeGenerator.switchToIncludeFile();
      codeGenerator.genCodeLine("#define jjCheckNAdd(state)\\");
      codeGenerator.genCodeLine("{\\");
      codeGenerator.genCodeLine("   if (jjrounds[state] != jjround)\\");
      codeGenerator.genCodeLine("   {\\");
      codeGenerator.genCodeLine("      jjstateSet[jjnewStateCnt++] = state;\\");
      codeGenerator.genCodeLine("      jjrounds[state] = jjround;\\");
      codeGenerator.genCodeLine("   }\\");
      codeGenerator.genCodeLine("}");

      codeGenerator.genCodeLine("#define jjAddStates(start, end)\\");
      codeGenerator.genCodeLine("{\\");
      codeGenerator.genCodeLine("   for (int x = start; x <= end; x++) {\\");
      codeGenerator.genCodeLine("      jjstateSet[jjnewStateCnt++] = jjnextStates[x];\\");
      codeGenerator.genCodeLine("   } /*while (start++ != end);*/\\");
      codeGenerator.genCodeLine("}");

      codeGenerator.genCodeLine("#define jjCheckNAddTwoStates(state1, state2)\\");
      codeGenerator.genCodeLine("{\\");
      codeGenerator.genCodeLine("   jjCheckNAdd(state1);\\");
      codeGenerator.genCodeLine("   jjCheckNAdd(state2);\\");
      codeGenerator.genCodeLine("}");
      codeGenerator.genCodeLine("");

      if(jjCheckNAddStatesDualNeeded) {
        codeGenerator.genCodeLine("#define jjCheckNAddStates(start, end)\\");
        codeGenerator.genCodeLine("{\\");
        codeGenerator.genCodeLine("   for (int x = start; x <= end; x++) {\\");
        codeGenerator.genCodeLine("      jjCheckNAdd(jjnextStates[x]);\\");
        codeGenerator.genCodeLine("   } /*while (start++ != end);*/\\");
        codeGenerator.genCodeLine("}");
        codeGenerator.genCodeLine("");
      }

      if(jjCheckNAddStatesUnaryNeeded) {
        codeGenerator.genCodeLine("#define jjCheckNAddStates(start)\\");
        codeGenerator.genCodeLine("{\\");
        codeGenerator.genCodeLine("   jjCheckNAdd(jjnextStates[start]);\\");
        codeGenerator.genCodeLine("   jjCheckNAdd(jjnextStates[start + 1]);\\");
        codeGenerator.genCodeLine("}");
        codeGenerator.genCodeLine("");
      }
      codeGenerator.switchToMainFile();
   }

   private static void FindStatesWithNoBreak()
   {
      Hashtable printed = new Hashtable();
      boolean[] put = new boolean[generatedStates];
      int cnt = 0;
      int i, j, foundAt = 0;

      Outer :
      for (j = 0; j < allStates.size(); j++)
      {
         NfaState stateForCase = null;
         NfaState tmpState = (NfaState)allStates.get(j);

         if (tmpState.stateName == -1 || tmpState.dummy || !tmpState.UsefulState() ||
             tmpState.next == null || tmpState.next.usefulEpsilonMoves < 1)
            continue;

         String s = tmpState.next.epsilonMovesString;

         if (compositeStateTable.get(s) != null || printed.get(s) != null)
            continue;

         printed.put(s, s);
         int[] nexts = (int[])allNextStates.get(s);

         if (nexts.length == 1)
            continue;

         int state = cnt;
         //System.out.println("State " + tmpState.stateName + " : " + s);
         for (i = 0; i < nexts.length; i++)
         {
            if ((state = nexts[i]) == -1)
               continue;

            NfaState tmp = (NfaState)allStates.get(state);

            if (!tmp.isComposite && tmp.inNextOf == 1)
            {
               if (put[state])
                  throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu");

               foundAt = i;
               cnt++;
               stateForCase = tmp;
               put[state] = true;

               //System.out.print(state + " : " + tmp.inNextOf + ", ");
               break;
            }
         }
         //System.out.println("");

         if (stateForCase == null)
            continue;

         for (i = 0; i < nexts.length; i++)
         {
            if ((state = nexts[i]) == -1)
               continue;

            NfaState tmp = (NfaState)allStates.get(state);

            if (!put[state] && tmp.inNextOf > 1 && !tmp.isComposite && tmp.stateForCase == null)
            {
               cnt++;
               nexts[i] = -1;
               put[state] = true;

               int toSwap = nexts[0];
               nexts[0] = nexts[foundAt];
               nexts[foundAt] = toSwap;

               tmp.stateForCase = stateForCase;
               stateForCase.stateForCase = tmp;
               stateSetsToFix.put(s, nexts);

               //System.out.println("For : " + s + "; " + stateForCase.stateName +
                         //" and " + tmp.stateName);

               continue Outer;
            }
         }

         for (i = 0; i < nexts.length; i++)
         {
            if ((state = nexts[i]) == -1)
               continue;

            NfaState tmp = (NfaState)allStates.get(state);
            if (tmp.inNextOf <= 1)
               put[state] = false;
         }
      }
   }

   static int[][] kinds;
   static int[][][] statesForState;
   public static void DumpMoveNfa(CodeGenerator codeGenerator)
   {
      //if (!boilerPlateDumped)
      //   PrintBoilerPlate(codeGenerator);

      //boilerPlateDumped = true;
      int i;
      int[] kindsForStates = null;

      if (kinds == null)
      {
         kinds = new int[Main.lg.maxLexStates][];
         statesForState = new int[Main.lg.maxLexStates][][];
      }

      ReArrange();

      for (i = 0; i < allStates.size(); i++)
      {
         NfaState temp = (NfaState)allStates.get(i);

         if (temp.lexState != Main.lg.lexStateIndex ||
             !temp.HasTransitions() || temp.dummy ||
             temp.stateName == -1)
            continue;

         if (kindsForStates == null)
         {
            kindsForStates = new int[generatedStates];
            statesForState[Main.lg.lexStateIndex] = new int[Math.max(generatedStates, dummyStateIndex + 1)][];
         }

         kindsForStates[temp.stateName] = temp.lookingFor;
         statesForState[Main.lg.lexStateIndex][temp.stateName] = temp.compositeStates;

         temp.GenerateNonAsciiMoves(codeGenerator);
      }

      Enumeration e = stateNameForComposite.keys();

      while (e.hasMoreElements())
      {
         String s = (String)e.nextElement();
         int state = ((Integer)stateNameForComposite.get(s)).intValue();

         if (state >= generatedStates)
            statesForState[Main.lg.lexStateIndex][state] = (int[])allNextStates.get(s);
      }

      if (stateSetsToFix.size() != 0)
         FixStateSets();

      kinds[Main.lg.lexStateIndex] = kindsForStates;

      if (codeGenerator.isJavaLanguage()) {
        codeGenerator.genCodeLine((Options.getStatic() ? "static " : "") + "private int " +
                    "jjMoveNfa" + Main.lg.lexStateSuffix + "(int startState, int curPos)");
      } else {
        codeGenerator.generateMethodDefHeader("int", Main.lg.tokMgrClassName, "jjMoveNfa" + Main.lg.lexStateSuffix + "(int startState, int curPos)");
      }
      codeGenerator.genCodeLine("{");
      if (generatedStates == 0)
      {
         codeGenerator.genCodeLine("   return curPos;");
         codeGenerator.genCodeLine("}");
         return;
      }

      if (Main.lg.mixed[Main.lg.lexStateIndex])
      {
         codeGenerator.genCodeLine("   int strKind = jjmatchedKind;");
         codeGenerator.genCodeLine("   int strPos = jjmatchedPos;");
         codeGenerator.genCodeLine("   int seenUpto;");
         if (codeGenerator.isJavaLanguage()) {
           codeGenerator.genCodeLine("   input_stream.backup(seenUpto = curPos + 1);");
           codeGenerator.genCodeLine("   try { curChar = input_stream.readChar(); }");
           codeGenerator.genCodeLine("   catch(java.io.IOException e) { throw new Error(\"Internal Error\"); }");
         } else {
           codeGenerator.genCodeLine("   input_stream->backup(seenUpto = curPos + 1);");
           codeGenerator.genCodeLine("   assert(!input_stream->endOfInput());");
           codeGenerator.genCodeLine("   curChar = input_stream->readChar();");
         }
         codeGenerator.genCodeLine("   curPos = 0;");
      }

      codeGenerator.genCodeLine("   int startsAt = 0;");
      codeGenerator.genCodeLine("   jjnewStateCnt = " + generatedStates + ";");
      codeGenerator.genCodeLine("   int i = 1;");
      codeGenerator.genCodeLine("   jjstateSet[0] = startState;");

      if (Options.getDebugTokenManager()) {
        if (codeGenerator.isJavaLanguage()) {
         codeGenerator.genCodeLine("      debugStream.println(\"   Starting NFA to match one of : \" + " +
                 "jjKindsForStateVector(curLexState, jjstateSet, 0, 1));");
        } else {
          codeGenerator.genCodeLine("      fprintf(debugStream, \"   Starting NFA to match one of : %s\\n\", jjKindsForStateVector(curLexState, jjstateSet, 0, 1).c_str());");
        }
      }

      if (Options.getDebugTokenManager()) {
        if (codeGenerator.isJavaLanguage()) {
         codeGenerator.genCodeLine("      debugStream.println(" + (LexGen.maxLexStates > 1 ?
                 "\"<\" + lexStateNames[curLexState] + \">\" + " :
                 "") + "\"Current character : \" + " + Options.getTokenMgrErrorClass() +
                 ".addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") " +                 "at line \" + input_stream.getEndLine() + \" column \" + input_stream.getEndColumn());");
        } else {
         codeGenerator.genCodeLine("   fprintf(debugStream, " +
            "\"<%s>Current character : %c(%d) at line %d column %d\\n\","+
            "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, " +
            "input_stream->getEndLine(), input_stream->getEndColumn());");
        }
      }

      codeGenerator.genCodeLine("   int kind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
      codeGenerator.genCodeLine("   for (;;)");
      codeGenerator.genCodeLine("   {");
      codeGenerator.genCodeLine("      if (++jjround == 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
      codeGenerator.genCodeLine("         ReInitRounds();");
      codeGenerator.genCodeLine("      if (curChar < 64)");
      codeGenerator.genCodeLine("      {");

      DumpAsciiMoves(codeGenerator, 0);

      codeGenerator.genCodeLine("      }");

      codeGenerator.genCodeLine("      else if (curChar < 128)");

      codeGenerator.genCodeLine("      {");

      DumpAsciiMoves(codeGenerator, 1);

      codeGenerator.genCodeLine("      }");

      codeGenerator.genCodeLine("      else");
      codeGenerator.genCodeLine("      {");

      DumpCharAndRangeMoves(codeGenerator);

      codeGenerator.genCodeLine("      }");

      codeGenerator.genCodeLine("      if (kind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
      codeGenerator.genCodeLine("      {");
      codeGenerator.genCodeLine("         jjmatchedKind = kind;");
      codeGenerator.genCodeLine("         jjmatchedPos = curPos;");
      codeGenerator.genCodeLine("         kind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
      codeGenerator.genCodeLine("      }");
      codeGenerator.genCodeLine("      ++curPos;");

      if (Options.getDebugTokenManager()) {
        if (codeGenerator.isJavaLanguage()) {
          codeGenerator.genCodeLine("      if (jjmatchedKind != 0 && jjmatchedKind != 0x" +
                Integer.toHexString(Integer.MAX_VALUE) + ")");
          codeGenerator.genCodeLine("         debugStream.println(" +
                 "\"   Currently matched the first \" + (jjmatchedPos + 1) + \" characters as" +
                 " a \" + tokenImage[jjmatchedKind] + \" token.\");");
        } else {
          codeGenerator.genCodeLine("      if (jjmatchedKind != 0 && jjmatchedKind != 0x" +
                Integer.toHexString(Integer.MAX_VALUE) + ")");
          codeGenerator.genCodeLine("   fprintf(debugStream, \"   Currently matched the first %d characters as a \\\"%s\\\" token.\\n\",  (jjmatchedPos + 1),  addUnicodeEscapes(tokenImage[jjmatchedKind]).c_str());");
        }
      }

      if (codeGenerator.isJavaLanguage()) {
        codeGenerator.genCodeLine("      if ((i = jjnewStateCnt) == (startsAt = " +
                   generatedStates + " - (jjnewStateCnt = startsAt)))");
      } else {
        codeGenerator.genCodeLine("      if ((i = jjnewStateCnt), (jjnewStateCnt = startsAt), (i == (startsAt = " +
                   generatedStates + " - startsAt)))");
      }
      if (Main.lg.mixed[Main.lg.lexStateIndex])
         codeGenerator.genCodeLine("         break;");
      else
         codeGenerator.genCodeLine("         return curPos;");

      if (Options.getDebugTokenManager()) {
        if (codeGenerator.isJavaLanguage()) {
          codeGenerator.genCodeLine("      debugStream.println(\"   Possible kinds of longer matches : \" + " +
                 "jjKindsForStateVector(curLexState, jjstateSet, startsAt, i));");
        } else {
          codeGenerator.genCodeLine("      fprintf(debugStream, \"   Possible kinds of longer matches : %s\\n\", jjKindsForStateVector(curLexState, jjstateSet, startsAt, i).c_str());");
        }
      }

      if (codeGenerator.isJavaLanguage()) {
        codeGenerator.genCodeLine("      try { curChar = input_stream.readChar(); }");
      } else {
        if (Main.lg.mixed[Main.lg.lexStateIndex]) {
          codeGenerator.genCodeLine("      if (input_stream->endOfInput()) { break; }");
        } else {
          codeGenerator.genCodeLine("      if (input_stream->endOfInput()) { return curPos; }");
        }
        codeGenerator.genCodeLine("      curChar = input_stream->readChar();");
      }

      if (Main.lg.mixed[Main.lg.lexStateIndex]) {
        if (codeGenerator.isJavaLanguage()) {
          codeGenerator.genCodeLine("      catch(java.io.IOException e) { break; }");
        }
      } else {
        if (codeGenerator.isJavaLanguage()) {
          codeGenerator.genCodeLine("      catch(java.io.IOException e) { return curPos; }");
        }
      }

      if (Options.getDebugTokenManager()) {
        if (codeGenerator.isJavaLanguage()) {
         codeGenerator.genCodeLine("      debugStream.println(" + (LexGen.maxLexStates > 1 ?
                 "\"<\" + lexStateNames[curLexState] + \">\" + " :
                 "") + "\"Current character : \" + " +  Options.getTokenMgrErrorClass() +
                 ".addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") " +
                 "at line \" + input_stream.getEndLine() + \" column \" + input_stream.getEndColumn());");
        } else {
          codeGenerator.genCodeLine("   fprintf(debugStream, " +
             "\"<%s>Current character : %c(%d) at line %d column %d\\n\","+
             "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, " +
             "input_stream->getEndLine(), input_stream->getEndColumn());");
        }
      }

      codeGenerator.genCodeLine("   }");

      if (Main.lg.mixed[Main.lg.lexStateIndex])
      {
         codeGenerator.genCodeLine("   if (jjmatchedPos > strPos)");
         codeGenerator.genCodeLine("      return curPos;");
         codeGenerator.genCodeLine("");
         if (codeGenerator.isJavaLanguage()) {
           codeGenerator.genCodeLine("   int toRet = Math.max(curPos, seenUpto);");
         } else {
           codeGenerator.genCodeLine("   int toRet = MAX(curPos, seenUpto);");
         }
         codeGenerator.genCodeLine("");
         codeGenerator.genCodeLine("   if (curPos < toRet)");
         if (codeGenerator.isJavaLanguage()) {
           codeGenerator.genCodeLine("      for (i = toRet - Math.min(curPos, seenUpto); i-- > 0; )");
         codeGenerator.genCodeLine("         try { curChar = input_stream.readChar(); }");
         codeGenerator.genCodeLine("         catch(java.io.IOException e) { " +
                 "throw new Error(\"Internal Error : Please send a bug report.\"); }");
         } else {
           codeGenerator.genCodeLine("      for (i = toRet - MIN(curPos, seenUpto); i-- > 0; )");
         codeGenerator.genCodeLine("        {  assert(!input_stream->endOfInput());");
         codeGenerator.genCodeLine("           curChar = input_stream->readChar(); }");
         }
         codeGenerator.genCodeLine("");
         codeGenerator.genCodeLine("   if (jjmatchedPos < strPos)");
         codeGenerator.genCodeLine("   {");
         codeGenerator.genCodeLine("      jjmatchedKind = strKind;");
         codeGenerator.genCodeLine("      jjmatchedPos = strPos;");
         codeGenerator.genCodeLine("   }");
         codeGenerator.genCodeLine("   else if (jjmatchedPos == strPos && jjmatchedKind > strKind)");
         codeGenerator.genCodeLine("      jjmatchedKind = strKind;");
         codeGenerator.genCodeLine("");
         codeGenerator.genCodeLine("   return toRet;");
      }

      codeGenerator.genCodeLine("}");
      allStates.clear();
   }

   public static void DumpStatesForStateCPP(CodeGenerator codeGenerator)
   {
      if (statesForState == null) {
         assert(false) : "This should never be null.";
         codeGenerator.genCodeLine("null;");
         return;
      }

      codeGenerator.switchToStaticsFile();
      for (int i = 0; i < Main.lg.maxLexStates; i++)
      {
       if (statesForState[i] == null)
       {
          continue;
       }

       for (int j = 0; j < statesForState[i].length; j++)
       {
         int[] stateSet = statesForState[i][j];

         codeGenerator.genCode("const int stateSet_" + i + "_" + j + "[" +
                    Main.lg.stateSetSize + "] = ");
         if (stateSet == null)
         {
            codeGenerator.genCodeLine("   { " + j + " };");
            continue;
         }

         codeGenerator.genCode("   { ");

         for (int k = 0; k < stateSet.length; k++)
            codeGenerator.genCode(stateSet[k] + ", ");

         codeGenerator.genCodeLine("};");
       }

      }

      for (int i = 0; i < Main.lg.maxLexStates; i++)
      {
       codeGenerator.genCodeLine("const int *stateSet_" + i + "[] = {");
       if (statesForState[i] == null)
       {
         codeGenerator.genCodeLine(" NULL, ");
         codeGenerator.genCodeLine("};");
         continue;
       }

       for (int j = 0; j < statesForState[i].length; j++)
       {
         codeGenerator.genCode("stateSet_" + i + "_" + j + ",");
       }
       codeGenerator.genCodeLine("};");
      }

      codeGenerator.genCode("const int** statesForState[] = { ");
      for (int i = 0; i < Main.lg.maxLexStates; i++)
      {
       codeGenerator.genCodeLine("stateSet_" + i + ", ");
      }

      codeGenerator.genCodeLine("\n};");
      codeGenerator.switchToMainFile();
   }


   public static void DumpStatesForState(CodeGenerator codeGenerator)
   {
      codeGenerator.genCode("protected static final int[][][] statesForState = ");

      if (statesForState == null) {
         assert(false) : "This should never be null.";
         codeGenerator.genCodeLine("null;");
         return;
      } else {
         codeGenerator.genCodeLine("{");
      }

      for (int i = 0; i < Main.lg.maxLexStates; i++)
      {
       if (statesForState[i] == null)
       {
          codeGenerator.genCodeLine(" {},");
          continue;
       }

       codeGenerator.genCodeLine(" {");

       for (int j = 0; j < statesForState[i].length; j++)
       {
         int[] stateSet = statesForState[i][j];

         if (stateSet == null)
         {
            codeGenerator.genCodeLine("   { " + j + " },");
            continue;
         }

         codeGenerator.genCode("   { ");

         for (int k = 0; k < stateSet.length; k++)
            codeGenerator.genCode(stateSet[k] + ", ");

         codeGenerator.genCodeLine("},");
       }

       codeGenerator.genCodeLine("},");
      }

      codeGenerator.genCodeLine("\n};");
   }

   public static void DumpStatesForKind(CodeGenerator codeGenerator)
   {
      if (codeGenerator.isJavaLanguage()) {
        DumpStatesForState(codeGenerator);
      } else {
        DumpStatesForStateCPP(codeGenerator);
      }
      boolean moreThanOne = false;
      int cnt = 0;

      if (codeGenerator.isJavaLanguage()) {
        codeGenerator.genCode("protected static final int[][] kindForState = ");
      } else {
        codeGenerator.switchToStaticsFile();
        codeGenerator.genCode("static const int kindForState[" + Main.lg.stateSetSize + "][" + Main.lg.stateSetSize + "] = ");
      }

      if (kinds == null)
      {
         codeGenerator.genCodeLine("null;");
         return;
      }
      else
         codeGenerator.genCodeLine("{");

      for (int i = 0; i < kinds.length; i++)
      {
         if (moreThanOne)
            codeGenerator.genCodeLine(",");
         moreThanOne = true;

         if (kinds[i] == null)
            codeGenerator.genCodeLine("{}");
         else
         {
            cnt = 0;
            codeGenerator.genCode("{ ");
            for (int j = 0; j < kinds[i].length; j++)
            {
               if (cnt % 15 == 0)
                  codeGenerator.genCode("\n  ");
               else if (cnt > 1)
                  codeGenerator.genCode(" ");

               codeGenerator.genCode(kinds[i][j]);
               codeGenerator.genCode(", ");

            }

            codeGenerator.genCode("}");
         }
      }
      codeGenerator.genCodeLine("\n};");
      codeGenerator.switchToMainFile();
   }

   public static void reInit()
   {
      unicodeWarningGiven = false;
      generatedStates = 0;
      idCnt = 0;
      lohiByteCnt = 0;
      dummyStateIndex = -1;
      done = false;
      mark = null;
      stateDone = null;
      allStates = new ArrayList();
      indexedAllStates = new ArrayList();
      nonAsciiTableForMethod = new ArrayList();
      equivStatesTable = new Hashtable();
      allNextStates = new Hashtable();
      lohiByteTab = new Hashtable();
      stateNameForComposite = new Hashtable();
      compositeStateTable = new Hashtable();
      stateBlockTable = new Hashtable();
      stateSetsToFix = new Hashtable();
      allBitVectors = new ArrayList();
      tmpIndices = new int[512];
      allBits = "{\n   0xffffffffffffffffL, " +
                    "0xffffffffffffffffL, " +
                    "0xffffffffffffffffL, " +
                    "0xffffffffffffffffL\n};";
      tableToDump = new Hashtable();
      orderedStateSet = new ArrayList();
      lastIndex = 0;
      //boilerPlateDumped = false;
      jjCheckNAddStatesUnaryNeeded = false;
      jjCheckNAddStatesDualNeeded = false;
      kinds = null;
      statesForState = null;
   }

   private static final Map<Integer, NfaState> initialStates =
       new HashMap<Integer, NfaState>();
   private static final Map<Integer, List<NfaState>> statesForLexicalState =
       new HashMap<Integer, List<NfaState>>();
   private static final Map<Integer, Integer> nfaStateOffset =
       new HashMap<Integer, Integer>();
   private static final Map<Integer, Integer> matchAnyChar =
       new HashMap<Integer, Integer>();
   static void UpdateNfaData(
       int maxState, int startStateName, int lexicalStateIndex,
       int matchAnyCharKind) {
     // Cleanup the state set.
     final Set<Integer> done = new HashSet<Integer>();
     List<NfaState> cleanStates = new ArrayList<NfaState>();
     NfaState startState = null;
     for (int i = 0; i < allStates.size(); i++) {
       NfaState tmp = (NfaState)allStates.get(i);
       if (tmp.stateName == -1) continue;
       if (done.contains(tmp.stateName)) continue;
       done.add(tmp.stateName);
       cleanStates.add(tmp);
       if (tmp.stateName == startStateName) {
         startState = tmp;
       }
     }

     initialStates.put(lexicalStateIndex, startState);
     statesForLexicalState.put(lexicalStateIndex, cleanStates);
     nfaStateOffset.put(lexicalStateIndex, maxState);
     if (matchAnyCharKind > 0) {
       matchAnyChar.put(lexicalStateIndex, matchAnyCharKind);
     } else {
       matchAnyChar.put(lexicalStateIndex, Integer.MAX_VALUE);
     }
   }

   public static void BuildTokenizerData(TokenizerData tokenizerData) {
     NfaState[] cleanStates;
     List<NfaState> cleanStateList = new ArrayList<NfaState>();
     for (int l : statesForLexicalState.keySet()) {
       int offset = nfaStateOffset.get(l);
       List<NfaState> states = statesForLexicalState.get(l);
       for (int i = 0; i < states.size(); i++) {
         NfaState state = states.get(i);
         if (state.stateName == -1) continue;
         states.get(i).stateName += offset;
       }
       cleanStateList.addAll(states);
     }
     cleanStates = new NfaState[cleanStateList.size()];
     for (NfaState s : cleanStateList) {
       assert(cleanStates[s.stateName] == null);
       cleanStates[s.stateName] = s;
       Set<Character> chars = new TreeSet<Character>();
       for (int c = 0; c <= Character.MAX_VALUE; c++) {
         if (s.CanMoveUsingChar((char)c)) {
           chars.add((char)c);
         }
       }
       Set<Integer> nextStates = new TreeSet<Integer>();
       if (s.next != null) {
         for (NfaState next : s.next.epsilonMoveArray) {
           nextStates.add(next.stateName);
         }
       }
       Set<Integer> composite = new TreeSet<Integer>();
       if (s.isComposite) {
         for (int c : s.compositeStates) composite.add(c);
       }
       tokenizerData.addNfaState(
           s.stateName, chars, nextStates, composite, s.kindToPrint);
     }
     Map<Integer, Integer> initStates = new HashMap<Integer, Integer>();
     for (int l : initialStates.keySet()) {
       if (initialStates.get(l) == null) {
         initStates.put(l, -1);
       } else {
         initStates.put(l, initialStates.get(l).stateName);
       }
     }
     tokenizerData.setInitialStates(initStates);
     tokenizerData.setWildcardKind(matchAnyChar);
   }

   static NfaState getNfaState(int index) {
     if (index == -1) return null;
     for (NfaState s : allStates) {
       if (s.stateName == index) return s;
     }
     assert(false);
     return null;
   }
}
