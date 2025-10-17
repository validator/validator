<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output encoding="utf-8"/>

<xsl:param name="dir" select="'.'"/>

<xsl:template match="/*">
  <documents>
    <dir name="{$dir}"/>
    <xsl:apply-templates select="//testCase"/>
  </documents>
</xsl:template>

<xsl:variable name="incorrectSchemaName" select="'i'"/>
<xsl:variable name="correctSchemaName" select="'c'"/>
<xsl:variable name="xmlSuffix" select="'.rng'"/>
<xsl:variable name="compactSuffix" select="'.rnc'"/>
<xsl:variable name="xsdSuffix" select="'.xsd'"/>
<xsl:variable name="xmlDir" select="'xml'"/>
<xsl:variable name="xsdDir" select="'xsd'"/>
<xsl:variable name="compactDir" select="'compact'"/>
<xsl:variable name="outDir" select="'out'"/>

<xsl:template match="testCase">
  <xsl:variable name="b" select="concat($dir, '/', format-number(position(),'000'))"/>
  <dir name="{$b}"/>
  <dir name="{concat($b, '/', $outDir)}"/>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$b"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="xml//resource|xsd//resource">
  <xsl:param name="base"/>
  <document href="{$base}/{@name}" method="xml">
    <xsl:copy-of select="node()"/>
  </document>
</xsl:template>

<xsl:template match="xml//correct">
  <xsl:param name="base"/>
  <document href="{$base}/{$correctSchemaName}{$xmlSuffix}" method="xml">
    <xsl:copy-of select="node()"/>
  </document>
</xsl:template>

<xsl:template match="xsd//correct">
  <xsl:param name="base"/>
  <document href="{$base}/{$correctSchemaName}{$xsdSuffix}" method="xml">
    <xsl:copy-of select="node()"/>
  </document>
</xsl:template>

<xsl:template match="compact//incorrect">
  <xsl:param name="base"/>
  <document href="{$base}/{$incorrectSchemaName}{$compactSuffix}" method="text" encoding="utf-8">
    <xsl:value-of select="."/>
  </document>
</xsl:template>

<xsl:template match="compact//correct">
  <xsl:param name="base"/>
  <document href="{$base}/{$correctSchemaName}{$compactSuffix}" method="text" encoding="utf-8">
    <xsl:value-of select="."/>
  </document>
</xsl:template>

<xsl:template match="compact//resource">
  <xsl:param name="base"/>
  <document href="{$base}/{@name}" method="text" encoding="utf-8">
    <xsl:value-of select="."/>
  </document>
</xsl:template>

<xsl:template match="compact">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', $compactDir)"/>
  <dir name="{$d}"/>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="xml">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', $xmlDir)"/>
  <dir name="{$d}"/>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="xsd">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', $xsdDir)"/>
  <dir name="{$d}"/>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="dir">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', @name)"/>
  <dir name="{$d}"/>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

</xsl:stylesheet>
