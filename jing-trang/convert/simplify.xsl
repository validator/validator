<?xml version="1.0"?>
<!-- Simplifies the output from from-relax.xsl. -->
<xsl:stylesheet
  version="1.0"
  xmlns="http://relaxng.org/ns/structure/0.9"
  xmlns:rng="http://relaxng.org/ns/structure/0.9"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="rng">

<xsl:output encoding="iso-8859-1" indent="yes"/>

<xsl:strip-space elements="rng:*"/>

<xsl:template match="/|*|comment()">
  <xsl:copy>
   <xsl:copy-of select="@*"/>
   <xsl:apply-templates select="node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="rng:choice[count(*)=1]">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="rng:empty[count(../rng:*) != 1]">
</xsl:template>

<xsl:template match="rng:mixed[rng:empty and count(rng:*)=1]">
  <text/>
</xsl:template>

</xsl:stylesheet>
