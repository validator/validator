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
package org.javacc.jjtree;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ASTNodeDescriptor extends JJTreeNode {
  ASTNodeDescriptor(int id) {
    super(id);
  }

  private boolean faked = false;

  static ASTNodeDescriptor indefinite(String s)
  {
    ASTNodeDescriptor nd = new ASTNodeDescriptor(JJTreeParserTreeConstants.JJTNODEDESCRIPTOR);
    nd.name = s;
    nd.setNodeIdValue();
    nd.faked = true;
    return nd;
  }


  static List<String> nodeIds = new ArrayList<String>();
  static List<String> nodeNames = new ArrayList<String>();
  static Hashtable<String, String> nodeSeen = new Hashtable<String, String>();

  static List<String> getNodeIds()
  {
    return nodeIds;
  }

  static List<String> getNodeNames()
  {
    return nodeNames;
  }

  void setNodeIdValue()
  {
    String k = getNodeId();
    if (!nodeSeen.containsKey(k)) {
      nodeSeen.put(k, k);
      nodeNames.add(name);
      nodeIds.add(k);
    }
  }

  String getNodeId()
  {
    return "JJT" + name.toUpperCase().replace('.', '_');
  }


  String name;
  boolean isGT;
  ASTNodeDescriptorExpression expression;


  boolean isVoid()
  {
    return name.equals("void");
  }

  public String toString()
  {
    if (faked) {
      return "(faked) " + name;
    } else {
      return super.toString() + ": " + name;
    }
  }


  String getDescriptor()
  {
    if (expression == null) {
      return name;
    } else {
      return "#" + name + "(" + (isGT ? ">" : "") + expression_text() + ")";
    }
  }

  String getNodeType()
  {
    if (JJTreeOptions.getMulti()) {
      return JJTreeOptions.getNodePrefix() + name;
    } else {
      return "SimpleNode";
    }
  }


  String getNodeName()
  {
    return name;
  }


  String openNode(String nodeVar)
  {
    return "jjtree.openNodeScope(" + nodeVar + ");";
  }


  String expression_text()
  {
    if (expression.getFirstToken().image.equals(")") &&
      expression.getLastToken().image.equals("(")) {
      return "true";
    }

    String s = "";
    Token t = expression.getFirstToken();
    while (true) {
       s += " " + t.image;
       if (t == expression.getLastToken()) {
         break;
       }
       t = t.next;
    }
    return s;
  }


  String closeNode(String nodeVar)
  {
    if (expression == null) {
      return "jjtree.closeNodeScope(" + nodeVar + ", true);";
    } else if (isGT) {
      return "jjtree.closeNodeScope(" + nodeVar + ", jjtree.nodeArity() >" +
          expression_text() + ");";
    } else {
      return "jjtree.closeNodeScope(" + nodeVar + ", " +
          expression_text() + ");";
    }
  }


  String translateImage(Token t)
  {
    return whiteOut(t);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(JJTreeParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

}

/*end*/
