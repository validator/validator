<?xml version="1.0" encoding="UTF-8"?>
<grammar datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
<!-- ...................................................................... -->
<!-- XHTML Inline Presentation Module  .................................... -->
<!-- file: xhtml-inlpres-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.
     Revision: $Id: xhtml-inlpres-1.mod,v 1.2 2002-04-30 07:00:05 jjc Exp $ SMI

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Inline Presentation 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-inlpres-1.mod"

     Revisions:
     (none)
     ....................................................................... -->
<!-- Inline Presentational Elements

        b, big, i, small, sub, sup, tt

     This module declares the elements and their attributes used to
     support inline-level presentational markup.
-->
  <define name="b.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Inline.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="b">
    <element name="b">
      <ref name="b.attlist"/>
      <ref name="b.content"/>
    </element>
  </define>
<!-- end of b.element -->
  <define name="b.attlist" combine="interleave">
    <ref name="Common.attrib"/>
  </define>
<!-- end of b.attlist -->
  <define name="big.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Inline.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="big">
    <element name="big">
      <ref name="big.attlist"/>
      <ref name="big.content"/>
    </element>
  </define>
<!-- end of big.element -->
  <define name="big.attlist" combine="interleave">
    <ref name="Common.attrib"/>
  </define>
<!-- end of big.attlist -->
  <define name="i.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Inline.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="i">
    <element name="i">
      <ref name="i.attlist"/>
      <ref name="i.content"/>
    </element>
  </define>
<!-- end of i.element -->
  <define name="i.attlist" combine="interleave">
    <ref name="Common.attrib"/>
  </define>
<!-- end of i.attlist -->
  <define name="small.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Inline.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="small">
    <element name="small">
      <ref name="small.attlist"/>
      <ref name="small.content"/>
    </element>
  </define>
<!-- end of small.element -->
  <define name="small.attlist" combine="interleave">
    <ref name="Common.attrib"/>
  </define>
<!-- end of small.attlist -->
  <define name="sub.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Inline.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="sub">
    <element name="sub">
      <ref name="sub.attlist"/>
      <ref name="sub.content"/>
    </element>
  </define>
<!-- end of sub.element -->
  <define name="sub.attlist" combine="interleave">
    <ref name="Common.attrib"/>
  </define>
<!-- end of sub.attlist -->
  <define name="sup.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Inline.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="sup">
    <element name="sup">
      <ref name="sup.attlist"/>
      <ref name="sup.content"/>
    </element>
  </define>
<!-- end of sup.element -->
  <define name="sup.attlist" combine="interleave">
    <ref name="Common.attrib"/>
  </define>
<!-- end of sup.attlist -->
  <define name="tt.content">
    <zeroOrMore>
      <choice>
        <text/>
        <ref name="Inline.mix"/>
      </choice>
    </zeroOrMore>
  </define>
  <define name="tt">
    <element name="tt">
      <ref name="tt.attlist"/>
      <ref name="tt.content"/>
    </element>
  </define>
<!-- end of tt.element -->
  <define name="tt.attlist" combine="interleave">
    <ref name="Common.attrib"/>
  </define>
<!-- end of tt.attlist -->
<!-- end of xhtml-inlpres-1.mod -->
</grammar>
