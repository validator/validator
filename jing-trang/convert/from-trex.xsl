<?xml version="1.0"?>
<!-- Stylesheet performing partial conversion from TREX to RELAX NG. -->
<xsl:stylesheet version="1.0"
  exclude-result-prefixes="trex"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:trex="http://www.thaiopensource.com/trex"
  xmlns="http://relaxng.org/ns/structure/0.9">

<xsl:output encoding="iso-8859-1"/>

<xsl:template match="/trex:grammar">
  <grammar>
    <xsl:apply-templates select="@*"/>
      <xsl:if test="//trex:data">
        <xsl:attribute name="datatypeLibrary">http://www.w3.org/2001/XMLSchema-datatypes</xsl:attribute>
      </xsl:if>
    <xsl:apply-templates/>
  </grammar>
</xsl:template>

<xsl:template match="trex:string">
  <value>
    <xsl:value-of select="."/>
  </value>
</xsl:template>

<xsl:template match="trex:string[@whiteSpace='preserve']">
  <value type="string">
    <xsl:value-of select="."/>
  </value>
</xsl:template>

<xsl:template match="trex:anyString">
  <text>
   <xsl:apply-templates select="@*|node()"/>
  </text>
</xsl:template>

<xsl:template match="trex:*">
  <xsl:element name="{local-name()}">
    <xsl:apply-templates select="@*|node()"/>
  </xsl:element>
</xsl:template>

<xsl:template match="@*">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="@combine[.='group']">
  <xsl:attribute name="combine">interleave</xsl:attribute>
</xsl:template>

<xsl:template match="*">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="comment()|processing-instruction()">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="trex:include[not(parent::trex:grammar)]">
  <externalRef>
   <xsl:apply-templates select="@*|node()"/>
  </externalRef>
</xsl:template>

<xsl:template match="trex:ref[@parent='true']">
  <parentRef>
   <xsl:apply-templates select="node()"/>
  </parentRef>
</xsl:template>

<xsl:template match="trex:data/@type">
  <xsl:attribute name="type">
    <xsl:choose>
      <xsl:when test="contains(.,':')">
        <xsl:value-of select="substring-after(.,':')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
</xsl:template>

<xsl:template match="trex:data/@ns">
 <xsl:attribute name="datatypeLibrary">
   <xsl:value-of select="."/>
 </xsl:attribute>
</xsl:template>

</xsl:stylesheet>
