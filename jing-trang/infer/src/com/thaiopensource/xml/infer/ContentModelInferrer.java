package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ContentModelInferrer {
  public static final Name START = new Name("", "#start");
  public static final Name END = new Name("", "#end");
  public static final Name TEXT = new Name("", "#text");

  /**
   * Maps names to nodes.
   */
  private final Map nameMap = new HashMap();

  private static class Node {
    final Set followingNodes = new HashSet();
  }

  private static class SingleNode extends Node {
    final Name name;
    final int index;
    boolean multi = false;

    SingleNode(Name name, int index) {
      this.name = name;
      this.index = index;
    }


  }


  /**
   * http://citeseer.nj.nec.com/nuutila94finding.html
   */
  private static class StronglyConnectedComponentsFinder {
    private final int[] number;
    private final SingleNode[] root;
    private final boolean[] visited;
    private int time = 0;
    private final Stack stack = new Stack();
    private int nextSccNumber = 0;
    private final int[] sccNumber;

    StronglyConnectedComponentsFinder(int nNodes) {
      number = new int[nNodes];
      root = new SingleNode[nNodes];
      visited = new boolean[nNodes];
      sccNumber = new int[nNodes];
    }

    void scc(SingleNode v) {
      root[v.index] = v;
      number[v.index] = ++time;
      stack.push(v);
      visited[v.index] = true;
      for (Iterator iter = v.followingNodes.iterator(); iter.hasNext();) {
        SingleNode w = (SingleNode)iter.next();
        if (!visited[w.index])
          scc(w);
        if (sccNumber[w.index] == 0)
          root[v.index] = min(root[v.index], root[w.index]);
      }
      if (root[v.index] == v) {
        nextSccNumber++;
        for (;;) {
          SingleNode w = (SingleNode)stack.pop();
          sccNumber[w.index] = nextSccNumber;
          if (w == v)
            break;
        }
      }
    }

    SingleNode min(SingleNode n1, SingleNode n2) {
      return number[n1.index] < number[n2.index] ? n1 : n2;
    }

  }

  void addSequence(Name e1, Name e2) {
    lookup(e1).followingNodes.add(lookup(e2));
  }

  void setMulti(Name e) {
    lookup(e).multi = true;
  }

  private SingleNode lookup(Name name) {
    SingleNode node = (SingleNode)nameMap.get(name);
    if (node == null) {
      node = new SingleNode(name, nameMap.size());
      nameMap.put(name, node);
    }
    return node;
  }

  void sccDebug(Name name) {
    StronglyConnectedComponentsFinder sccFinder = new StronglyConnectedComponentsFinder(nameMap.size());
    sccFinder.scc(lookup(START));
    System.err.println(name.getLocalName());
    int[] sccNumber = sccFinder.sccNumber;
    SingleNode[] nodeTab = new SingleNode[nameMap.size()];
    for (Iterator iter = nameMap.values().iterator(); iter.hasNext();) {
      SingleNode tem = (SingleNode)iter.next();
      nodeTab[tem.index] = tem;
    }
    for (int i = 1; i <= sccFinder.nextSccNumber; i++) {
      System.err.print("  ");
      for (int j = 0; j < sccNumber.length; j++)
        if (sccNumber[j] == i)
          System.err.print(" " + nodeTab[j].name.getLocalName());
      System.err.println();
    }
  }

  Particle inferContentModel() {
    return null;
  }
}
