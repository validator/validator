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
import java.util.List;

/**
 * Describes regular expressions which are choices from
 * from among included regular expressions.
 */

public class RChoice extends RegularExpression {

  /**
   * The list of choices of this regular expression.  Each
   * list component will narrow to RegularExpression.
   */
  private List choices = new ArrayList();

  /**
   * @param choices the choices to set
   */
  public void setChoices(List choices) {
    this.choices = choices;
  }

  /**
   * @return the choices
   */
  public List getChoices() {
    return choices;
  }

  public Nfa GenerateNfa(boolean ignoreCase)
  {
     CompressCharLists();

     if (getChoices().size() == 1)
        return ((RegularExpression)getChoices().get(0)).GenerateNfa(ignoreCase);

     Nfa retVal = new Nfa();
     NfaState startState = retVal.start;
     NfaState finalState = retVal.end;

     for (int i = 0; i < getChoices().size(); i++)
     {
        Nfa temp;
        RegularExpression curRE = (RegularExpression)getChoices().get(i);

        temp = curRE.GenerateNfa(ignoreCase);

        startState.AddMove(temp.start);
        temp.end.AddMove(finalState);
     }

     return retVal;
  }

  void CompressCharLists()
  {
     CompressChoices(); // Unroll nested choices
     RegularExpression curRE;
     RCharacterList curCharList = null;

     for (int i = 0; i < getChoices().size(); i++)
     {
        curRE = (RegularExpression)getChoices().get(i);

        while (curRE instanceof RJustName)
           curRE = ((RJustName)curRE).regexpr;

        if (curRE instanceof RStringLiteral &&
            ((RStringLiteral)curRE).image.length() == 1)
           getChoices().set(i, curRE = new RCharacterList(
                      ((RStringLiteral)curRE).image.charAt(0)));

        if (curRE instanceof RCharacterList)
        {
           if (((RCharacterList)curRE).negated_list)
              ((RCharacterList)curRE).RemoveNegation();

           List tmp = ((RCharacterList)curRE).descriptors;

           if (curCharList == null)
              getChoices().set(i, curRE = curCharList = new RCharacterList());
           else
              getChoices().remove(i--);

           for (int j = tmp.size(); j-- > 0;)
              curCharList.descriptors.add(tmp.get(j));
         }

     }
  }

  void CompressChoices()
  {
     RegularExpression curRE;

     for (int i = 0; i < getChoices().size(); i++)
     {
        curRE = (RegularExpression)getChoices().get(i);

        while (curRE instanceof RJustName)
           curRE = ((RJustName)curRE).regexpr;

        if (curRE instanceof RChoice)
        {
           getChoices().remove(i--);
           for (int j = ((RChoice)curRE).getChoices().size(); j-- > 0;)
              getChoices().add(((RChoice)curRE).getChoices().get(j));
        }
     }
  }

  public void CheckUnmatchability()
  {
     RegularExpression curRE;
     int numStrings = 0;

     for (int i = 0; i < getChoices().size(); i++)
     {
        if (!(curRE = (RegularExpression)getChoices().get(i)).private_rexp &&
            //curRE instanceof RJustName &&
            curRE.ordinal > 0 && curRE.ordinal < ordinal &&
            Main.lg.lexStates[curRE.ordinal] == Main.lg.lexStates[ordinal])
        {
           if (label != null)
              JavaCCErrors.warning(this, "Regular Expression choice : " +
                 curRE.label + " can never be matched as : " + label);
           else
              JavaCCErrors.warning(this, "Regular Expression choice : " +
                 curRE.label + " can never be matched as token of kind : " +
                                                                      ordinal);
        }

        if (!curRE.private_rexp && curRE instanceof RStringLiteral)
           numStrings++;
     }
  }

}
