<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:variable name="indent" select="'  '"/>

<xsl:output encoding="iso-8859-1"/>

<xsl:template match="head">
  <xsl:apply-templates/>
<style type="text/css">
body {
  padding-left: 3em;
  padding-top: 3em;
  margin: 0;
  font-family: sans-serif;
  color: black;
  background: white;
  line-length: 20pt;
  max-width: 40em;
}

h1, h2 {
  color: #005A9C;
  background: white;
}

h1 { font-size: 140% }
h2 { font-size: 120% }
pre { margin-left: 2em }
</style>

</xsl:template>

<xsl:template match="h2">
  <h2><a name="{translate(normalize-space(),' ','_')}"/>
<xsl:for-each select=".."><xsl:number/></xsl:for-each><xsl:text> </xsl:text><xsl:apply-templates/></h2>
</xsl:template>

<xsl:template match="h2" mode="toc">
  &#160;&#160;<xsl:for-each select=".."><xsl:number/></xsl:for-each><xsl:text> </xsl:text>
  <a href="#{translate(.,' ','_')}"><xsl:apply-templates/></a>
  <br/>
</xsl:template>

<xsl:template match="div[@class='back']/div/h2">
  <h2><a name="{translate(.,' ','_')}"/>
  <xsl:for-each select=".."><xsl:number format="A"/></xsl:for-each><xsl:text> </xsl:text><xsl:apply-templates/></h2>
</xsl:template>

<xsl:template match="div[@class='back']/div/h2" mode="toc">
  &#160;&#160;<xsl:for-each select=".."><xsl:number format="A"/></xsl:for-each><xsl:text> </xsl:text>
  <a href="#{translate(.,' ','_')}"><xsl:apply-templates/></a>
  <br/>
</xsl:template>

<xsl:template match="h1">
  <h1><xsl:apply-templates/></h1>
  <xsl:copy-of select="../div[@class='titlepage']"/>
  <xsl:if test="../abstract">
    <h2>Abstract</h2>
    <xsl:apply-templates select="../abstract/*"/>
  </xsl:if>
  <h2>Contents</h2>
  <xsl:apply-templates select="../../h2|../div/div/h2" mode="toc"/>
  <hr/>
</xsl:template>

<xsl:template match="abstract"/>

<xsl:template match="div[@class='titlepage']"/>

<xsl:template match="*">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="code">
  <xsl:text>'</xsl:text>
  <span class="code">
    <xsl:apply-templates/>
  </span>
  <xsl:text>'</xsl:text>
</xsl:template>

<xsl:template match="xml">
  <pre>
    <xsl:apply-templates select="*" mode="print">
      <xsl:with-param name="top" select="true()"/>
    </xsl:apply-templates>
  </pre>
</xsl:template>

<xsl:template name="makeIndent">
  <xsl:param name="n" select="0"/>
  <xsl:if test="$n">
     <xsl:text> </xsl:text>
     <xsl:call-template name="makeIndent">
        <xsl:with-param name="n" select="$n - 1"/>
     </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="print">
  <xsl:param name="totalIndent" select="''"/>
  <xsl:param name="top" select="false()"/>
  <xsl:value-of select="$totalIndent"/>
  <xsl:text>&lt;</xsl:text>
  <xsl:value-of select="name()"/>
  <xsl:variable name="attributeIndent">
    <xsl:call-template name="makeIndent">
      <xsl:with-param name="n" select="string-length(name()) + 2"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:apply-templates select="@*" mode="print">
    <xsl:with-param name="totalIndent" select="concat($totalIndent, $attributeIndent)"/>
  </xsl:apply-templates>
  <xsl:if test="$top and namespace::*[local-name() != 'xml']">
    <xsl:choose>
      <xsl:when test="@*">
        <xsl:call-template name="newline"/>
        <xsl:value-of select="$totalIndent"/>
        <xsl:value-of select="$attributeIndent"/>
      </xsl:when>
      <xsl:otherwise><xsl:text> </xsl:text></xsl:otherwise>
    </xsl:choose>
    <xsl:variable name="defaultNamespace" select="namespace::*[not(local-name())]"/>
    <xsl:if test="$defaultNamespace">
      <xsl:text>xmlns=&quot;</xsl:text>
      <xsl:value-of select="$defaultNamespace"/>
      <xsl:text>&quot;</xsl:text>
    </xsl:if>
    <xsl:for-each select="namespace::*[local-name() and local-name() != 'xml']">
      <xsl:if test="position() != 1 or $defaultNamespace">
        <xsl:call-template name="newline"/>
        <xsl:value-of select="$totalIndent"/>
        <xsl:value-of select="$attributeIndent"/>
      </xsl:if>
      <xsl:text>xmlns:</xsl:text>
      <xsl:value-of select="local-name()"/>
      <xsl:text>=&quot;</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&quot;</xsl:text>
    </xsl:for-each>
  </xsl:if>
  <xsl:choose>
    <xsl:when test="not(*) and normalize-space()">
      <xsl:text>&gt;</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&lt;/</xsl:text>
      <xsl:value-of select="name()"/>
      <xsl:text>&gt;</xsl:text>
    </xsl:when>
    <xsl:when test="not(*)">/&gt;</xsl:when>
    <xsl:otherwise>
      <xsl:text>&gt;</xsl:text>
      <xsl:call-template name="newline"/>
      <xsl:apply-templates mode="print">
	<xsl:with-param name="totalIndent" select="concat($totalIndent, $indent)"/>
      </xsl:apply-templates>
      <xsl:value-of select="$totalIndent"/>
      <xsl:text>&lt;/</xsl:text>
      <xsl:value-of select="name()"/>
      <xsl:text>&gt;</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:call-template name="newline"/>
</xsl:template>

<xsl:template match="text()" mode="print">
  <xsl:if test="normalize-space()">
    <xsl:value-of select="."/>
  </xsl:if>
</xsl:template>

<xsl:template match="@*[1]" mode="print">
  <xsl:text> </xsl:text>
  <xsl:value-of select="name()"/>
  <xsl:text>=&quot;</xsl:text>
  <xsl:value-of select="."/>
  <xsl:text>&quot;</xsl:text>
</xsl:template>

<xsl:template match="@*" mode="print">
  <xsl:param name="totalIndent" select="''"/>
  <xsl:call-template name="newline"/>
  <xsl:value-of select="$totalIndent"/>
  <xsl:value-of select="name()"/>
  <xsl:text>=&quot;</xsl:text>
  <xsl:value-of select="."/>
  <xsl:text>&quot;</xsl:text>
</xsl:template>

<xsl:template name="newline"><xsl:text>&#xA;</xsl:text></xsl:template>

<xsl:template match="bibliography">
  <xsl:for-each select="bibentry">
    <xsl:sort select="."/>
    <p><a name="{@name}"/>
      <xsl:apply-templates mode="bib"/>
    </p>
  </xsl:for-each>
</xsl:template>

<xsl:template match="url" mode="bib">
  <a href="{.}"><xsl:value-of select="."/></a>
</xsl:template>

<xsl:template match="bib">
  <a href="#{@ref}"><xsl:value-of select="."/></a>
</xsl:template>

<xsl:template match="href">
  <a href="#{translate(normalize-space(),' ','_')}">
    <xsl:for-each select="//h2[normalize-space()=normalize-space(current())]/..">
      <xsl:number/>
    </xsl:for-each>
    <xsl:text> </xsl:text>
    <xsl:value-of select="."/>
  </a>
</xsl:template>

</xsl:stylesheet>
