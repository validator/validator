<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://icl.com/saxon"
  extension-element-prefixes="saxon">

<xsl:output method="text"/>

<xsl:template match="/">
  <xsl:variable name="prepped">
    <xsl:apply-templates select="*"/>
  </xsl:variable>
  <xsl:apply-templates select="saxon:node-set($prepped)/documents/*" mode="output"/>
</xsl:template>

<xsl:template match="document" mode="output">
  <saxon:output href="{@href}" method="{@method}">
    <xsl:if test="@dtd">
      <xsl:value-of select="@dtd" disable-output-escaping="yes"/>
    </xsl:if>
    <xsl:copy-of select="node()"/>
  </saxon:output>
</xsl:template>

<xsl:template match="dir" mode="output">
  <xsl:value-of select="substring(File:mkdir(File:new(@name)),0,0)" 
                xmlns:File="java:java.io.File"/>
</xsl:template>

</xsl:stylesheet>

