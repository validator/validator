<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exsl="http://exslt.org/common"
  extension-element-prefixes="exsl">

<xsl:output method="text"/>

<xsl:param name="dir" select="'.'"/>

<xsl:template match="/">
  <xsl:apply-templates select="//testCase"/>
</xsl:template>

<xsl:variable name="incorrectSchemaName" select="'i.rng'"/>
<xsl:variable name="correctSchemaName" select="'c.rng'"/>
<xsl:variable name="invalidInstanceSuffix" select="'.i.xml'"/>
<xsl:variable name="validInstanceSuffix" select="'.v.xml'"/>

<xsl:template match="testCase">
  <xsl:variable name="b" select="concat($dir, '/', format-number(position(),'000'))"/>
  <xsl:variable name="f">
    <xsl:choose>
      <xsl:when test="correct">
        <xsl:value-of select="$correctSchemaName"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$incorrectSchemaName"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <exsl:document href="{concat($b, '/', $f)}" method="xml">
    <xsl:for-each select="correct|incorrect">
      <xsl:call-template name="copy"/>
    </xsl:for-each>
  </exsl:document>
  <xsl:apply-templates select="resource|dir">
    <xsl:with-param name="base" select="$b"/>
  </xsl:apply-templates>
  <!-- Don't combine so that position is correct. -->
  <xsl:apply-templates select="valid|invalid">
    <xsl:with-param name="base" select="$b"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="valid">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', position(), $validInstanceSuffix)"/>
  <exsl:document href="{$d}" method="xml">
    <xsl:call-template name="copy"/>
  </exsl:document>
</xsl:template>

<xsl:template match="invalid">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', position(), $invalidInstanceSuffix)"/>
  <exsl:document href="{$d}" method="xml">
    <xsl:call-template name="copy"/>
  </exsl:document>
</xsl:template>

<xsl:template match="resource">
  <xsl:param name="base"/>
  <xsl:choose>
    <xsl:when test="*">
      <exsl:document href="{$base}/{@name}" method="xml">
	<xsl:call-template name="copy"/>
      </exsl:document>
    </xsl:when>
    <xsl:otherwise>
      <exsl:document href="{$base}/{@name}" method="text" encoding="utf-8">
	<xsl:value-of select="."/>
      </exsl:document>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="copy">
  <xsl:if test="@dtd">
    <xsl:value-of select="@dtd" disable-output-escaping="yes"/>
  </xsl:if>
  <xsl:copy-of select="node()"/>
</xsl:template>

<xsl:template match="dir">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', @name)"/>
  <xsl:apply-templates select="resource|dir">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

</xsl:stylesheet>
