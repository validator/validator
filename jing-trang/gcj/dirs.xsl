<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>

<xsl:param name="dir" select="'.'"/>

<xsl:template match="/">
  <xsl:apply-templates select="//testCase"/>
</xsl:template>

<xsl:template match="testCase">
  <xsl:variable name="b" select="concat($dir, '/', format-number(position(),'000'))"/>
  <xsl:call-template name="mkdir">
    <xsl:with-param name="dir" select="$b"/>
  </xsl:call-template>
  <xsl:apply-templates select="dir">
    <xsl:with-param name="base" select="$b"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="dir">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', @name)"/>
  <xsl:call-template name="mkdir">
    <xsl:with-param name="dir" select="$d"/>
  </xsl:call-template>
  <xsl:apply-templates select="dir">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template name="mkdir">
  <xsl:param name="dir"/>
  <xsl:value-of select="$dir"/>
  <xsl:text>
</xsl:text>
</xsl:template>

</xsl:stylesheet>
