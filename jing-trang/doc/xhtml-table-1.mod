<?xml version="1.0" encoding="UTF-8"?>
<grammar datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
<!-- ...................................................................... -->
<!-- XHTML Table Module  .................................................. -->
<!-- file: xhtml-table-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.
     Revision: $Id: xhtml-table-1.mod,v 1.2 2002-04-30 07:00:05 jjc Exp $ SMI

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Tables 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-table-1.mod"

     Revisions:
     (none)
     ....................................................................... -->
<!-- Tables

        table, caption, thead, tfoot, tbody, colgroup, col, tr, th, td

     This module declares element types and attributes used to provide
     table markup similar to HTML 4, including features that enable
     better accessibility for non-visual user agents.
-->
<!-- declare qualified element type names:
-->
<!-- The frame attribute specifies which parts of the frame around
     the table should be rendered. The values are not the same as
     CALS to avoid a name clash with the valign attribute.
-->
  <define name="frame.attrib">
    <optional>
      <attribute name="frame">
        <choice>
          <value>void</value>
          <value>above</value>
          <value>below</value>
          <value>hsides</value>
          <value>lhs</value>
          <value>rhs</value>
          <value>vsides</value>
          <value>box</value>
          <value>border</value>
        </choice>
      </attribute>
    </optional>
  </define>
<!-- The rules attribute defines which rules to draw between cells:

     If rules is absent then assume:

       "none" if border is absent or border="0" otherwise "all"
-->
  <define name="rules.attrib">
    <optional>
      <attribute name="rules">
        <choice>
          <value>none</value>
          <value>groups</value>
          <value>rows</value>
          <value>cols</value>
          <value>all</value>
        </choice>
      </attribute>
    </optional>
  </define>
<!-- horizontal alignment attributes for cell contents
-->
  <define name="CellHAlign.attrib">
    <optional>
      <attribute name="align">
        <choice>
          <value>left</value>
          <value>center</value>
          <value>right</value>
          <value>justify</value>
          <value>char</value>
        </choice>
      </attribute>
    </optional>
    <optional>
      <attribute name="char">
        <ref name="Character.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="charoff">
        <ref name="Length.datatype"/>
      </attribute>
    </optional>
  </define>
<!-- vertical alignment attribute for cell contents
-->
  <define name="CellVAlign.attrib">
    <optional>
      <attribute name="valign">
        <choice>
          <value>top</value>
          <value>middle</value>
          <value>bottom</value>
          <value>baseline</value>
        </choice>
      </attribute>
    </optional>
  </define>
<!-- scope is simpler than axes attribute for common tables
-->
  <define name="scope.attrib">
    <optional>
      <attribute name="scope">
        <choice>
          <value>row</value>
          <value>col</value>
          <value>rowgroup</value>
          <value>colgroup</value>
        </choice>
      </attribute>
    </optional>
  </define>
<!-- table: Table Element .............................. -->
  <define name="table.content">
    <optional>
      <ref name="caption"/>
    </optional>
    <choice>
      <zeroOrMore>
        <ref name="col"/>
      </zeroOrMore>
      <zeroOrMore>
        <ref name="colgroup"/>
      </zeroOrMore>
    </choice>
    <choice>
      <group>
        <optional>
          <ref name="thead"/>
        </optional>
        <optional>
          <ref name="tfoot"/>
        </optional>
        <oneOrMore>
          <ref name="tbody"/>
        </oneOrMore>
      </group>
      <oneOrMore>
        <ref name="tr"/>
      </oneOrMore>
    </choice>
  </define>
  <define name="table">
    <element name="table">
      <ref name="table.attlist"/>
      <ref name="table.content"/>
    </element>
  </define>
<!-- end of table.element -->
  <define name="table.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <optional>
      <attribute name="summary">
        <ref name="Text.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="width">
        <ref name="Length.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="border">
        <ref name="Pixels.datatype"/>
      </attribute>
    </optional>
    <ref name="frame.attrib"/>
    <ref name="rules.attrib"/>
    <optional>
      <attribute name="cellspacing">
        <ref name="Length.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="cellpadding">
        <ref name="Length.datatype"/>
      </attribute>
    </optional>
  </define>
<!-- end of table.attlist -->
<!-- caption: Table Caption ............................ -->
  <define name="caption">
    <element name="caption">
      <ref name="caption.attlist"/>
      <ref name="caption.content"/>
    </element>
  </define>
<!-- end of caption.element -->
  <define name="caption.attlist" combine="interleave">
    <ref name="Common.attrib"/>
  </define>
<!-- end of caption.attlist -->
<!-- thead: Table Header ............................... -->
<!-- Use thead to duplicate headers when breaking table
     across page boundaries, or for static headers when
     tbody sections are rendered in scrolling panel.
-->
  <define name="thead.content">
    <oneOrMore>
      <ref name="tr"/>
    </oneOrMore>
  </define>
  <define name="thead">
    <element name="thead">
      <ref name="thead.attlist"/>
      <ref name="thead.content"/>
    </element>
  </define>
<!-- end of thead.element -->
  <define name="thead.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <ref name="CellHAlign.attrib"/>
    <ref name="CellVAlign.attrib"/>
  </define>
<!-- end of thead.attlist -->
<!-- tfoot: Table Footer ............................... -->
<!-- Use tfoot to duplicate footers when breaking table
     across page boundaries, or for static footers when
     tbody sections are rendered in scrolling panel.
-->
  <define name="tfoot.content">
    <oneOrMore>
      <ref name="tr"/>
    </oneOrMore>
  </define>
  <define name="tfoot">
    <element name="tfoot">
      <ref name="tfoot.attlist"/>
      <ref name="tfoot.content"/>
    </element>
  </define>
<!-- end of tfoot.element -->
  <define name="tfoot.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <ref name="CellHAlign.attrib"/>
    <ref name="CellVAlign.attrib"/>
  </define>
<!-- end of tfoot.attlist -->
<!-- tbody: Table Body ................................. -->
<!-- Use multiple tbody sections when rules are needed
     between groups of table rows.
-->
  <define name="tbody.content">
    <oneOrMore>
      <ref name="tr"/>
    </oneOrMore>
  </define>
  <define name="tbody">
    <element name="tbody">
      <ref name="tbody.attlist"/>
      <ref name="tbody.content"/>
    </element>
  </define>
<!-- end of tbody.element -->
  <define name="tbody.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <ref name="CellHAlign.attrib"/>
    <ref name="CellVAlign.attrib"/>
  </define>
<!-- end of tbody.attlist -->
<!-- colgroup: Table Column Group ...................... -->
<!-- colgroup groups a set of col elements. It allows you
     to group several semantically-related columns together.
-->
  <define name="colgroup.content">
    <zeroOrMore>
      <ref name="col"/>
    </zeroOrMore>
  </define>
  <define name="colgroup">
    <element name="colgroup">
      <ref name="colgroup.attlist"/>
      <ref name="colgroup.content"/>
    </element>
  </define>
<!-- end of colgroup.element -->
  <define name="colgroup.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <optional>
      <attribute name="span" a:defaultValue="1">
        <ref name="Number.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="width">
        <ref name="MultiLength.datatype"/>
      </attribute>
    </optional>
    <ref name="CellHAlign.attrib"/>
    <ref name="CellVAlign.attrib"/>
  </define>
<!-- end of colgroup.attlist -->
<!-- col: Table Column ................................. -->
<!-- col elements define the alignment properties for
     cells in one or more columns.

     The width attribute specifies the width of the
     columns, e.g.

       width="64"        width in screen pixels
       width="0.5*"      relative width of 0.5

     The span attribute causes the attributes of one
     col element to apply to more than one column.
-->
  <define name="col.content">
    <empty/>
  </define>
  <define name="col">
    <element name="col">
      <ref name="col.attlist"/>
      <ref name="col.content"/>
    </element>
  </define>
<!-- end of col.element -->
  <define name="col.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <optional>
      <attribute name="span" a:defaultValue="1">
        <ref name="Number.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="width">
        <ref name="MultiLength.datatype"/>
      </attribute>
    </optional>
    <ref name="CellHAlign.attrib"/>
    <ref name="CellVAlign.attrib"/>
  </define>
<!-- end of col.attlist -->
<!-- tr: Table Row ..................................... -->
  <define name="tr.content">
    <oneOrMore>
      <choice>
        <ref name="th"/>
        <ref name="td"/>
      </choice>
    </oneOrMore>
  </define>
  <define name="tr">
    <element name="tr">
      <ref name="tr.attlist"/>
      <ref name="tr.content"/>
    </element>
  </define>
<!-- end of tr.element -->
  <define name="tr.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <ref name="CellHAlign.attrib"/>
    <ref name="CellVAlign.attrib"/>
  </define>
<!-- end of tr.attlist -->
<!-- th: Table Header Cell ............................. -->
<!-- th is for header cells, td for data,
     but for cells acting as both use td
-->
  <define name="th.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Flow.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="th">
    <element name="th">
      <ref name="th.attlist"/>
      <ref name="th.content"/>
    </element>
  </define>
<!-- end of th.element -->
  <define name="th.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <optional>
      <attribute name="abbr">
        <ref name="Text.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="axis"/>
    </optional>
    <optional>
      <attribute name="headers">
        <data type="IDREFS"/>
      </attribute>
    </optional>
    <ref name="scope.attrib"/>
    <optional>
      <attribute name="rowspan" a:defaultValue="1">
        <ref name="Number.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="colspan" a:defaultValue="1">
        <ref name="Number.datatype"/>
      </attribute>
    </optional>
    <ref name="CellHAlign.attrib"/>
    <ref name="CellVAlign.attrib"/>
  </define>
<!-- end of th.attlist -->
<!-- td: Table Data Cell ............................... -->
  <define name="td.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Flow.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="td">
    <element name="td">
      <ref name="td.attlist"/>
      <ref name="td.content"/>
    </element>
  </define>
<!-- end of td.element -->
  <define name="td.attlist" combine="interleave">
    <ref name="Common.attrib"/>
    <optional>
      <attribute name="abbr">
        <ref name="Text.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="axis"/>
    </optional>
    <optional>
      <attribute name="headers">
        <data type="IDREFS"/>
      </attribute>
    </optional>
    <ref name="scope.attrib"/>
    <optional>
      <attribute name="rowspan" a:defaultValue="1">
        <ref name="Number.datatype"/>
      </attribute>
    </optional>
    <optional>
      <attribute name="colspan" a:defaultValue="1">
        <ref name="Number.datatype"/>
      </attribute>
    </optional>
    <ref name="CellHAlign.attrib"/>
    <ref name="CellVAlign.attrib"/>
  </define>
<!-- end of td.attlist -->
<!-- end of xhtml-table-1.mod -->
</grammar>
