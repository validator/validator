<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exsl="http://exslt.org/common"
  extension-element-prefixes="exsl">

<xsl:output method="text"/>

<xsl:template match="document">
  <exsl:document href="{@href}" method="{@method}">
     <xsl:if test="@dtd">
       <xsl:value-of select="@dtd" disable-output-escaping="yes"/>
     </xsl:if>
     <xsl:copy-of select="node()"/>
  </exsl:document>
</xsl:template>

</xsl:stylesheet>
