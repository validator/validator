<?xml version="1.0"?>
<!--
n.tag that is misrecognized as a modelGroup
gram
al.gram
general
start
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://icl.com/saxon"
  extension-element-prefixes="saxon"
  xmlns="http://relaxng.org/ns/structure/0.9">

<xsl:import href="rng.xsl"/>

<xsl:output indent="yes" encoding="iso-8859-1"/>

<xsl:param name="out-suffix" select="'.rng'"/>
<xsl:param name="element-prefix" select="''"/>
<xsl:param name="attlist-prefix" select="'al.'"/>

<xsl:key name="override" match="overridden" use="@name"/>

<xsl:template match="modelGroup|attributeGroup|datatype">
  <define name="{@name}">
    <xsl:if test="key('override',@name)">
      <xsl:call-template name="condition"/>
    </xsl:if>
    <xsl:apply-templates/>
  </define>
</xsl:template>

<xsl:template match="flag">
  <xsl:if test="starts-with(@name,'TEI.')
                and @name != 'TEI.2'
                and @name != 'TEI.mixed'
                and @name != 'TEI.general'
                and @name != 'TEI.singleBase'">
    <define name="{@name}">
      <xsl:apply-templates/>
    </define>
  </xsl:if>
</xsl:template>

<xsl:template match="externalIdRef">
  <xsl:variable name="f"
      select="concat(key('param',@name)/@system,$out-suffix)"/>
  <include href="{$f}"/>
  <saxon:output href="{$f}">
    <grammar datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
      <xsl:apply-templates/>
    </grammar>
  </saxon:output>
</xsl:template>

<xsl:template match="element">
  <xsl:variable name="name">
    <xsl:apply-templates select="*[1]"/>
  </xsl:variable>
  <define name="{$element-prefix}{$name}">
    <xsl:call-template name="condition"/>
    <element name="{$name}">
      <ref name="{$attlist-prefix}{$name}"/>
      <xsl:apply-templates select="*[2]"/>
    </element>
  </define>
</xsl:template>

<xsl:template name="condition">
  <xsl:variable name="flag"
    select="ancestor::includedSection[starts-with(@flag,'TEI.')
                                      and @flag != 'TEI.2'
                                      and @flag != 'TEI.mixed'
                                      and @flag != 'TEI.general'
                                      and @flag != 'TEI.singleBase'][1]/@flag"/>
  <xsl:if test="$flag">
    <ref name="{$flag}"/>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
