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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.javacc.parser.Options;
import org.javacc.parser.OutputFile;

/**
 * Generate the State of a tree.
 */
final class JJTreeState
{

  private JJTreeState() {}

  static void insertParserMembers(IO io) {
    String s;

    if (JJTreeOptions.getStatic()) {
      s = "static ";
    } else {
      s = "";
    }

    io.println();
    io.println("  protected " + s + nameState() +
         " jjtree = new " + nameState() + "();");
    io.println();
  }


  private static String nameState() {
    return "JJT" + JJTreeGlobals.parserName + "State";
  }


  static void generateTreeState_java()
  {
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), nameState() + ".java");

    try {
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();
      NodeFiles.generatePrologue(ostr);
      insertState(ostr);
      outputFile.close();
    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  private static void insertState(PrintWriter ostr) {
    ostr.println("public class " + nameState() + " {");

    if (!JJTreeOptions.getGenerateGenerics())
       ostr.println("  private java.util.List nodes;");
    else
       ostr.println("  private java.util.List<Node> nodes;");

    if (!JJTreeOptions.getGenerateGenerics())
       ostr.println("  private java.util.List marks;");
    else
       ostr.println("  private java.util.List<Integer> marks;");

    ostr.println("");
    ostr.println("  private int sp;        // number of nodes on stack");
    ostr.println("  private int mk;        // current mark");
    ostr.println("  private boolean node_created;");
    ostr.println("");
    ostr.println("  public " + nameState() + "() {");

    if (!JJTreeOptions.getGenerateGenerics())
       ostr.println("    nodes = new java.util.ArrayList();");
    else
       ostr.println("    nodes = new java.util.ArrayList<Node>();");

    if (!JJTreeOptions.getGenerateGenerics())
       ostr.println("    marks = new java.util.ArrayList();");
    else
       ostr.println("    marks = new java.util.ArrayList<Integer>();");

    ostr.println("    sp = 0;");
    ostr.println("    mk = 0;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Determines whether the current node was actually closed and");
    ostr.println("     pushed.  This should only be called in the final user action of a");
    ostr.println("     node scope.  */");
    ostr.println("  public boolean nodeCreated() {");
    ostr.println("    return node_created;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Call this to reinitialize the node stack.  It is called");
    ostr.println("     automatically by the parser's ReInit() method. */");
    ostr.println("  public void reset() {");
    ostr.println("    nodes.clear();");
    ostr.println("    marks.clear();");
    ostr.println("    sp = 0;");
    ostr.println("    mk = 0;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Returns the root node of the AST.  It only makes sense to call");
    ostr.println("     this after a successful parse. */");
    ostr.println("  public Node rootNode() {");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("    return (Node)nodes.get(0);");
    else
      ostr.println("    return nodes.get(0);");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Pushes a node on to the stack. */");
    ostr.println("  public void pushNode(Node n) {");
    ostr.println("    nodes.add(n);");
    ostr.println("    ++sp;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Returns the node on the top of the stack, and remove it from the");
    ostr.println("     stack.  */");
    ostr.println("  public Node popNode() {");
    ostr.println("    if (--sp < mk) {");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("      mk = ((Integer)marks.remove(marks.size()-1)).intValue();");
    else
      ostr.println("      mk = marks.remove(marks.size()-1);");
    ostr.println("    }");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("    return (Node)nodes.remove(nodes.size()-1);");
    else
      ostr.println("    return nodes.remove(nodes.size()-1);");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Returns the node currently on the top of the stack. */");
    ostr.println("  public Node peekNode() {");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("    return (Node)nodes.get(nodes.size()-1);");
    else
      ostr.println("    return nodes.get(nodes.size()-1);");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Returns the number of children on the stack in the current node");
    ostr.println("     scope. */");
    ostr.println("  public int nodeArity() {");
    ostr.println("    return sp - mk;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("");
    ostr.println("  public void clearNodeScope(Node n) {");
    ostr.println("    while (sp > mk) {");
    ostr.println("      popNode();");
    ostr.println("    }");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("    mk = ((Integer)marks.remove(marks.size()-1)).intValue();");
    else
      ostr.println("    mk = marks.remove(marks.size()-1);");
    ostr.println("  }");
    ostr.println("");
    ostr.println("");
    ostr.println("  public void openNodeScope(Node n) {");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("    marks.add(Integer.valueOf(mk));");
    else
      ostr.println("    marks.add(mk);");
    ostr.println("    mk = sp;");
    ostr.println("    n.jjtOpen();");
    ostr.println("  }");
    ostr.println("");
    ostr.println("");
    ostr.println("  /* A definite node is constructed from a specified number of");
    ostr.println("     children.  That number of nodes are popped from the stack and");
    ostr.println("     made the children of the definite node.  Then the definite node");
    ostr.println("     is pushed on to the stack. */");
    ostr.println("  public void closeNodeScope(Node n, int num) {");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("    mk = ((Integer)marks.remove(marks.size()-1)).intValue();");
    else
      ostr.println("    mk = marks.remove(marks.size()-1);");
    ostr.println("    while (num-- > 0) {");
    ostr.println("      Node c = popNode();");
    ostr.println("      c.jjtSetParent(n);");
    ostr.println("      n.jjtAddChild(c, num);");
    ostr.println("    }");
    ostr.println("    n.jjtClose();");
    ostr.println("    pushNode(n);");
    ostr.println("    node_created = true;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("");
    ostr.println("  /* A conditional node is constructed if its condition is true.  All");
    ostr.println("     the nodes that have been pushed since the node was opened are");
    ostr.println("     made children of the conditional node, which is then pushed");
    ostr.println("     on to the stack.  If the condition is false the node is not");
    ostr.println("     constructed and they are left on the stack. */");
    ostr.println("  public void closeNodeScope(Node n, boolean condition) {");
    ostr.println("    if (condition) {");
    ostr.println("      int a = nodeArity();");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("      mk = ((Integer)marks.remove(marks.size()-1)).intValue();");
    else
      ostr.println("      mk = marks.remove(marks.size()-1);");
    ostr.println("      while (a-- > 0) {");
    ostr.println("        Node c = popNode();");
    ostr.println("        c.jjtSetParent(n);");
    ostr.println("        n.jjtAddChild(c, a);");
    ostr.println("      }");
    ostr.println("      n.jjtClose();");
    ostr.println("      pushNode(n);");
    ostr.println("      node_created = true;");
    ostr.println("    } else {");
    if (!JJTreeOptions.getGenerateGenerics())
      ostr.println("      mk = ((Integer)marks.remove(marks.size()-1)).intValue();");
    else
      ostr.println("      mk = marks.remove(marks.size()-1);");
    ostr.println("      node_created = false;");
    ostr.println("    }");
    ostr.println("  }");
    ostr.println("}");
  }

}

/*end*/
