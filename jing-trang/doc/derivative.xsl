<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output encoding="iso-8859-1"/>

<xsl:template match="haskell">
  <xsl:choose>
    <xsl:when test="@name">
      <a name="haskell_{@name}">
        <pre class="haskell"><xsl:apply-templates/></pre>
      </a>
    </xsl:when>
    <xsl:otherwise>
      <pre class="haskell"><xsl:apply-templates/></pre>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="name">
 <a href="#haskell_{.}"><code><xsl:value-of select="."/></code></a>
</xsl:template>

<xsl:template match="/|*|@*|comment()">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="@xml:space"/>

<xsl:template match="h1">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="h2|h3|h4">
  <xsl:copy>
  <a name="{translate(.,' ','_')}"/>
  <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="h2|h3|h4" mode="toc">
  <xsl:apply-templates select="." mode="indent"/>
  <a href="#{translate(.,' ','_')}">
   <xsl:apply-templates/>
  </a>
  <br/>
</xsl:template>

<xsl:template match="h2" mode="indent"></xsl:template>
<xsl:template match="h3" mode="indent">&#160;&#160;</xsl:template>
<xsl:template match="h4" mode="indent">&#160;&#160;&#160;&#160;</xsl:template>

<xsl:template match="body">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:copy-of select="*[not(self::div)]"/>
    <div>
      <h2>Table of contents</h2>
      <xsl:apply-templates mode="toc" select="div//h2|div//h3|div//h4"/>
    </div>
    <hr/>
    <xsl:apply-templates select="div"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
