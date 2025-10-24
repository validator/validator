[Home](../index.md) > [Documentation](index.md) > JJTree

---

This page is the reference documentation for JJTree.

### <a name="toc"></a>Contents

- [**Introduction**](#introduction)
  * [Node scopes and user actions](#scopes)
  * [Exception handling](#exceptions)
  * [Node scope hooks](#hooks)
  * [Tracking tokens](#tracking)
  * [Lifecycle of a node](#lifecycle)
  * [Visitor support](#visitor)
  * [Options](#options)
- [**JJTree API**](#jjtree-api)
  * [JJTree state](#state)
  * [Node objects](#node)
- [**Examples**](#examples)

## <a name="introduction"></a>Introduction

JJTree is a preprocessor for JavaCC that inserts parse tree building actions at various places in the JavaCC source. The output of JJTree is run through JavaCC to create the parser. This document describes how to use JJTree, and how you can interface your parser to it.

By default JJTree generates code to construct parse tree nodes for each non-terminal in the language. This behavior can be modified so that some non-terminals do not have nodes generated, or so that a node is generated for a part of a production's expansion.

JJTree defines a Java interface `Node` that all parse tree nodes must implement. The interface provides methods for operations such as setting the parent of the node, and for adding children and retrieving them.

JJTree operates in one of two modes, simple and multi (for want of better terms). In simple mode each parse tree node is of concrete type `SimpleNode`, in multi mode the type of the parse tree node is derived from the name of the node. If you don't provide implementations for the node classes JJTree will generate sample implementations based on `SimpleNode` for you. You can then modify the implementations to suit.

Although JavaCC is a top-down parser, JJTree constructs the parse tree from the bottom up. To do this it uses a stack where it pushes nodes after they have been created. When it finds a parent for them, it pops the children from the stack and adds them to the parent, and finally pushes the new parent node itself. The stack is open, which means that you have access to it from within grammar actions: you can push, pop and otherwise manipulate its contents however you feel appropriate (see [Node Scopes and User Actions](#scopes) for more information).

JJTree provides decorations for two basic varieties of nodes, and some syntactic shorthand to make their use convenient.

#### Definite node

A definite node is constructed with a specific number of children. That many nodes are popped from the stack and made the children of the new node, which is then pushed on the stack itself.

You notate a definite node like this:

```java
#ADefiniteNode(INTEGER EXPRESSION)
```

A definite node descriptor expression can be any integer expression, although literal integer constants are by far the most common expressions.

#### Conditional node

A conditional node is constructed with all of the children that were pushed on the stack within its node scope if and only if its condition evaluates to `true`. If it evaluates to `false,` the node is not constructed, and all of the children remain on the node stack.

You notate a conditional node like this:

```java
#ConditionalNode(BOOLEAN EXPRESSION)
```

A conditional node descriptor expression can be any `boolean` expression.

There are two common shorthands for conditional nodes:

1. Indefinite nodes

```java
#IndefiniteNode is short for #IndefiniteNode(true)
```

2. Greater-than nodes

```java
#GTNode(>1) is short for #GTNode(jjtree.arity() > 1)
```

The indefinite node shorthand `(1)` can lead to ambiguities in the JJTree source when it is followed by a parenthesized expansion. In those cases the shorthand must be replaced by the full expression.

For example:

```java
( ... ) #N ( a() )
```

is ambiguous; you have to use the explicit condition:

```java
( ... ) #N(true) ( a() )
```

**WARNING: Node descriptor expressions should not have side-effects. JJTree doesn't specify how many times the expression will be evaluated**.

By default JJTree treats each non-terminal as an indefinite node and derives the name of the node from the name of its production. You can give it a different name with the following syntax:

```java
void P1() #MyNode : { ... } { ... }
```

When the parser recognizes a `P1` non-terminal it begins an indefinite node. It marks the stack, so that any parse tree nodes created and pushed on the stack by non-terminals in the expansion for `P1` will be popped off and made children of the node `MyNode`.

If you want to suppress the creation of a node for a production you can use the following syntax:

```java
void P2() #void : { ... } { ... }
```

Now any parse tree nodes pushed by non-terminals in the expansion of `P2` will remain on the stack, to be popped and made children of a production further up the tree. You can make this the default behavior for non-decorated nodes by using the `NODE_DEFAULT_VOID` option.

```java
void P3() : {} {
    P4() ( P5() )+ P6()
}
```

In this example, an indefinite node `P3` is begun, marking the stack, and then a `P4` node, one or more `P5` nodes and a `P6` node are parsed. Any nodes that they push are popped and made the children of `P3`.

You can further customize the generated tree:

```java
void P3() : {} {
    P4() ( P5() )+ #ListOfP5s P6()
}
```

Now the `P3` node will have a `P4` node, a `ListOfP5s` node and a `P6` node as children. The `#Name` construct acts as a postfix operator, and its scope is the immediately preceding expansion unit.

<br>

### <a name="scopes"></a>Node scopes and user actions

---

Each node is associated with a *node scope*. User actions within this scope can access the node under construction by using the special identifier `jjtThis` to refer to the node. This identifier is implicitly declared to be of the correct type for the node, so any fields and methods that the node has can be easily accessed.

A scope is the expansion unit immediately preceding the node decoration. This can be a parenthesized expression. When the production signature is decorated (perhaps implicitly with the default node), the scope is the entire right hand side of the production including its declaration block.

You can also use an expression involving `jjtThis` on the left hand side of an expansion reference.

For example:

```java
... ( jjtThis.my_foo = foo() ) #Baz ...
```

Here `jjtThis` refers to a `Baz` node, which has a field called `my_foo`. The result of parsing the production `foo()` is assigned to that `my_foo`.

The final user action in a node scope is different from all the others. When the code within it executes, the node's children have already been popped from the stack and added to the node, which has itself been pushed onto the stack. The children can now be accessed via the node's methods such as `jjtGetChild()`.

User actions other than the final one can only access the children on the stack. They have not yet been added to the node, so they aren't available via the node's methods.

A conditional node that has a node descriptor expression that evaluates to false will not get added to the stack, nor have children added to it. The final user action within a conditional node scope can determine whether the node was created or not by calling the `nodeCreated()` method. This returns true if the node's condition was satisfied and the node was created and pushed on the node stack, and false otherwise.

<br>

### <a name="exceptions"></a>Exception handling

---

An exception thrown by an expansion within a node scope that is not caught within the node scope is caught by JJTree itself. When this occurs, any nodes that have been pushed on to the node stack within the node scope are popped and thrown away. Then the exception is re-thrown.

The intention is to make it possible for parsers to implement error recovery and continue with the node stack in a known state.

**WARNING: JJTree currently cannot detect whether exceptions are thrown from user actions within a node scope. Such an exception will probably be handled incorrectly**.

<br>

### <a name="hooks"></a>Node scope hooks

---

If the `NODE_SCOPE_HOOK` option is set to `true`, JJTree generates calls to two user-defined parser methods on the entry and exit of every node scope. The methods must have the following signatures:

```java
void jjtreeOpenNodeScope(Node n)
void jjtreeCloseNodeScope(Node n)
```

If the parser is `STATIC` then these methods will have to be declared as `static` as well. They are both called with the current node as a parameter.

One use might be to store the parser object itself in the node so that state that should be shared by all nodes produced by that parser can be provided. For example, the parser might maintain a symbol table.

```java
void jjtreeOpenNodeScope(Node n) {
  ((SimpleNode)n).jjtSetValue(getSymbolTable());
}

void jjtreeCloseNodeScope(Node n) {
}
```

Where `getSymbolTable()` is a user-defined method to return a symbol table structure for the node.

<br>

### <a name="tracking"></a>Tracking tokens

---

It is often useful to keep track of each node's first and last token so that input can be easily reproduced again. By setting the `TRACK_TOKENS` option the generated `SimpleNode` class will contain 4 extra methods:

```java
public Token jjtGetFirstToken()
public void jjtSetFirstToken(Token token)
public Token jjtGetLastToken()
public void jjtSetLastToken(Token token)
```

The first and last token for each node will be set up automatically when the parser is run.

<br>

### <a name="lifecycle"></a>Lifecycle of a node

---

A node goes through a well determined sequence of steps as it is built. This is that sequence viewed from the perspective of the node itself:

1. The node's constructor is called with a unique integer parameter. This parameter identifies the kind of node and is especially useful in simple mode. JJTree automatically generates a file called `<parser>TreeConstants.java` that declares valid constants. The names of constants are derived by prepending JJT to the uppercase names of nodes, with dot symbols (`.`") replaced by underscore symbols (`_`). For convenience, an array of Strings called `jjtNodeName[]` that maps the constants to the unmodified names of nodes is maintained in the same file.

2. The node's `jjtOpen()` method is called.

3. If the option `NODE_SCOPE_HOOK` is set, the user-defined parser method `openNodeScope()` is called and passed the node as its parameter. This method can initialize fields in the node or call its methods. For example, it might store the node's first token in the node.

4. If an unhandled exception is thrown while the node is being parsed then the node is abandoned. JJTree will never refer to it again. It will not be closed, and the user-defined node scope hook `closeNodeHook()` will not be called with it as a parameter.

5. Otherwise, if the node is conditional and its conditional expression evaluates to false then the node is abandoned. It will not be closed, although the user-defined node scope hook `closeNodeHook()` might be called with it as a parameter.

6. Otherwise, all of the children of the node as specified by the integer expression of a definite node, or all the nodes that were pushed on the stack within a conditional node scope are added to the node. The order they are added is not specified.

7. The node's `jjtClose()` method is called.

8. The node is pushed on the stack.

9. If the option `NODE_SCOPE_HOOK` is set, the user-defined parser method `closeNodeScope()` is called and passed the node as its parameter.

10. If the node is not the root node, it is added as a child of another node and its `jjtSetParent()` method is called.

<br>

### <a name="visitor"></a>Visitor support

---

JJTree provides some basic support for the visitor design pattern. If the `VISITOR` option is set to true JJTree will insert an `jjtAccept()` method into all of the node classes it generates, and also generate a visitor interface that can be implemented and passed to the nodes to accept.

The name of the visitor interface is constructed by appending `Visitor` to the name of the parser. The interface is regenerated every time that JJTree is run, so that it accurately represents the set of nodes used by the parser. This will cause compile time errors if the implementation class has not been updated for the new nodes. This is a feature.

<br>

### <a name="options"></a>Options

---

| Option | Default | Description |
| :--- | :--- | :--- |
| `BUILD_NODE_FILES` | `true` | Generate sample implementations for `SimpleNode` and any other nodes used in the grammar.|
| `MULTI` | `false` | Generate a multi mode parse tree. The default for this is false, generating a simple mode parse tree.|
| `NODE_DEFAULT_VOID` | `false` | Instead of making each non-decorated production an indefinite node, make it void instead.|
| `NODE_CLASS` | `""` | If set defines the name of a user-supplied class that will extend `SimpleNode`. Any tree nodes created will then be subclasses of `NODE_CLASS`.|
| `NODE_FACTORY` | `""` | Specify a class containing a factory method with following signature to construct nodes:<br>`public static Node jjtCreate(int id)`<br>For backwards compatibility, the value false may also be specified, meaning that `SimpleNode` will be used as the factory class.|
| `NODE_PACKAGE` | `""` | The package to generate the node classes into. The default for this is the parser package.|
| `NODE_EXTENDS` | `""` | Deprecated. The superclass for the `SimpleNode` class. By providing a custom superclass you may be able to avoid the need to edit the generated `SimpleNode.java`.
| `NODE_PREFIX` | `"AST"` | The prefix used to construct node class names from node identifiers in multi mode. The default for this is AST.|
| `NODE_SCOPE_HOOK` | `false` | Insert calls to user-defined parser methods on entry and exit of every node scope. |
| `NODE_USES_PARSER` | `false` | JJTree will use an alternate form of the node construction routines where it passes the parser object in. For example:<br>`public static Node MyNode.jjtCreate(MyParser p, int id);<br>  MyNode(MyParser p, int id);`<br> |
| `TRACK_TOKENS` | `false` | Insert `jjtGetFirstToken()`, `jjtSetFirstToken()`, `getLastToken()`, and `jjtSetLastToken()` methods in `SimpleNode`. The `FirstToken` is automatically set up on entry to a node scope; the `LastToken` is automatically set up on exit from a node scope.
| `STATIC` | `true` | Generate code for a static parser. This must be used consistently with the equivalent JavaCC options. The value of this option is emitted in the JavaCC source.|
| `VISITOR` | `false` | Insert a `jjtAccept()` method in the node classes, and generate a visitor implementation with an entry for every node type used in the grammar.|
| `VISITOR_DATA_TYPE` | `"Object"` | If this option is set, it is used in the signature of the generated `jjtAccept()` methods and the `visit()` methods as the type of the data argument.|
| `VISITOR_RETURN_TYPE` | `"Object"` | If this option is set, it is used in the signature of the generated jjtAccept() methods and the `visit()` methods as the return type of the method.|
| `VISITOR_EXCEPTION` | `""` | If this option is set, it is used in the signature of the generated `jjtAccept()` methods and the `visit()` methods.
| `JJTREE_OUTPUT_DIRECTORY` | `"OUTPUT_DIRECTORY"` | By default, JJTree generates its output in the directory specified in the global `OUTPUT_DIRECTORY` setting. Explicitly setting this option allows the user to separate the parser from the tree files.|

<br>

## <a name="jjtree-api"></a>JJTree API
### <a name="state"></a>JJTree state

---

JJTree keeps its state in a parser class field called `jjtree`. You can use methods in this member to manipulate the node stack.

```java
final class JJTreeState {
  // Call this to reinitialize the node stack.
  void reset();

  // Return the root node of the AST.
  Node rootNode();

  // Determine whether the current node was actually closed and pushed
  boolean nodeCreated();

  // Return the number of nodes currently pushed on the node
  // stack in the current node scope.
  int arity();

  // Push a node on to the stack.
  void pushNode(Node n);

  // Return the node on the top of the stack, and remove it from the stack.
  Node popNode();

  // Return the node currently on the top of the stack.
  Node peekNode();
}
```

<br>

### <a name="node"></a>Node objects

---

```java
/*
 * All AST nodes must implement this interface.  It provides basic
 * machinery for constructing the parent and child relationships
 * between nodes.
 */
public interface Node {
  // This method is called after the node has been made the current node.
  // It indicates that child nodes can now be added to it.
  public void jjtOpen();

  // This method is called after all the child nodes have been added.
  public void jjtClose();

  // This pair of methods are used to inform the node of its parent.
  public void jjtSetParent(Node n);
  public Node jjtGetParent();

  // This method tells the node to add its argument to the node's list of children.
  public void jjtAddChild(Node n, int i);

  // This method returns a child node.  
  // The children are numbered from zero, left to right.
  public Node jjtGetChild(int i);

  // Return the number of children the node has.
  int jjtGetNumChildren();
}
```

The class `SimpleNode` implements the `Node` interface, and is automatically generated by JJTree if it doesn't already exist. You can use this class as a template or superclass for your node implementations, or you can modify it to suit. `SimpleNode` additionally provides a rudimentary mechanism for recursively dumping the node and its children.

You might use this is in action like this:

```java
{
  ((SimpleNode)jjtree.rootNode()).dump(">");
}
```

The String parameter to `dump()` is used as padding to indicate the tree hierarchy.

Another utility method is generated if the `VISITOR` options is set:

```java
{
  public void childrenAccept(MyParserVisitor visitor);
}
```

This walks over the node's children in turn, asking them to accept the visitor. This can be useful when implementing preorder and postorder traversals.

<br>

## <a name="examples"></a>Examples

There are some examples in the JJTree [tutorial](../tutorials/examples.md#jjtree).

JJTree is distributed with some simple examples containing a grammar that parses arithmetic expressions (see `examples/JJTreeExamples/` for further details).

There is also an interpreter for a simple language that uses JJTree to build the program representation (see `examples/Interpreter/` for more information.

Information about an example using the visitor support is in `examples/VTransformer/`.


<br>

---

[NEXT >>](jjdoc.md)

<br>
