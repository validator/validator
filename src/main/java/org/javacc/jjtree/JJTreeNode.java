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
package org.javacc.jjtree;

public class JJTreeNode extends SimpleNode {

  private int myOrdinal;

  public JJTreeNode(int id) {
    super(id);
  }

  public JJTreeNode(JJTreeParser p, int i) {
    this(i);
  }

  public static Node jjtCreate(int id) {
    return new JJTreeNode(id);
  }

  public void jjtAddChild(Node n, int i) {
    super.jjtAddChild(n, i);
    ((JJTreeNode)n).setOrdinal(i);
  }

  public int getOrdinal()
  {
    return myOrdinal;
  }

  public void setOrdinal(int o)
  {
    myOrdinal = o;
  }


  /*****************************************************************
   *
   * The following is added manually to enhance all tree nodes with
   * attributes that store the first and last tokens corresponding to
   * each node, as well as to print the tokens back to the specified
   * output stream.
   *
   *****************************************************************/

  private Token first, last;

  public Token getFirstToken() { return first; }
  public void setFirstToken(Token t) { first = t; }
  public Token getLastToken() { return last;  }
  public void setLastToken(Token t) { last = t; }

  String translateImage(Token t)
  {
    return t.image;
  }

  String whiteOut(Token t)
  {
    StringBuffer sb = new StringBuffer(t.image.length());

    for (int i = 0; i < t.image.length(); ++i) {
      char ch = t.image.charAt(i);
      if (ch != '\t' && ch != '\n' && ch != '\r' && ch != '\f') {
        sb.append(' ');
      } else {
        sb.append(ch);
      }
    }

    return sb.toString();
  }

  /* Indicates whether the token should be replaced by white space or
     replaced with the actual node variable. */
  private boolean whitingOut = false;

  protected void print(Token t, IO io) {
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
        io.print(TokenUtils.addUnicodeEscapes(translateImage(tt)));
        tt = tt.next;
      }
    }

    /* If we're within a node scope we modify the source in the
       following ways:

       1) we rename all references to `jjtThis' to be references to
       the actual node variable.

       2) we replace all calls to `jjtree.currentNode()' with
       references to the node variable. */

    NodeScope s = NodeScope.getEnclosingNodeScope(this);
    if (s == null) {
      /* Not within a node scope so we don't need to modify the
         source. */
      io.print(TokenUtils.addUnicodeEscapes(translateImage(t)));
      return;
    }

    if (t.image.equals("jjtThis")) {
      io.print(s.getNodeVariable());
      return;
    } else if (t.image.equals("jjtree")) {
      if (t.next.image.equals(".")) {
        if (t.next.next.image.equals("currentNode")) {
          if (t.next.next.next.image.equals("(")) {
            if (t.next.next.next.next.image.equals(")")) {
              /* Found `jjtree.currentNode()' so go into white out
                 mode.  We'll stay in this mode until we find the
                 closing parenthesis. */
              whitingOut = true;
            }
          }
        }
      }
    }
    if (whitingOut) {
      if (t.image.equals("jjtree")) {
        io.print(s.getNodeVariable());
        io.print(" ");
      } else if (t.image.equals(")")) {
        io.print(" ");
        whitingOut = false;
      } else {
        for (int i = 0; i < t.image.length(); ++i) {
          io.print(" ");
        }
      }
      return;
    }

    io.print(TokenUtils.addUnicodeEscapes(translateImage(t)));
  }
}
