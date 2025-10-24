<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="document"/>

<xsl:output method="text"/>

<xsl:template match="dir">
  <xsl:value-of select="@name"/>
  <xsl:text> </xsl:text>
</xsl:template>

</xsl:stylesheet>
