<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output encoding="iso-8859-1" method="html"/>

<xsl:template match="abstract">
  <div class="abstract">
    <h2>Abstract</h2>
    <xsl:apply-templates select="*"/>
  </div>
</xsl:template>

<xsl:template match="note">
  <div style="margin-left: .5in;margin-right: .5in" class="abstract">
    <xsl:apply-templates select="*"/>
  </div>
</xsl:template>

<xsl:template match="note/para[1]">
  <p><b>Note: </b><xsl:apply-templates/></p>
</xsl:template>


<xsl:template match="/">
 <html>
  <head>
   <title><xsl:value-of select="gcapaper/front/title"/></title>
  </head>
  <body>
   <h1><xsl:apply-templates select="gcapaper/front/title"/></h1>
   <xsl:for-each select="gcapaper/front/author">
     <h3>
       <xsl:value-of select="fname"/>
       <xsl:text> </xsl:text>
       <xsl:value-of select="surname"/>
       <xsl:text> (</xsl:text>
       <a href="mailto:{address/email}"><xsl:value-of select="address/email"/></a>
       <xsl:text>)</xsl:text>
     </h3>
   </xsl:for-each>
   <xsl:apply-templates select="gcapaper/front/abstract"/>
   <h2>Table of contents</h2>
   <xsl:for-each select="gcapaper/body/section/title">
      <xsl:if test="position()!=1"><br/></xsl:if>
      <a href="#section:{position()}"><xsl:value-of select="."/></a>
   </xsl:for-each>
   <xsl:apply-templates select="gcapaper/body"/>
   <h2>Acknowledgements</h2>
   <xsl:apply-templates select="gcapaper/rear/acknowl"/>
   <h2>References</h2>
   <ol>
    <xsl:for-each select="gcapaper/rear/bibliog/bibitem">
       <li>
         <xsl:if test="@id">
           <a name="{@id}"/>
         </xsl:if>
         <xsl:apply-templates select="."/>
       </li>
    </xsl:for-each>
   </ol>
  </body>
 </html>
</xsl:template>

<xsl:template match="section">
<div><xsl:apply-templates/></div>
</xsl:template>

<xsl:template match="section/title">
<h2>
  <a>
     <xsl:attribute name="name">
        <xsl:text>section:</xsl:text>
        <xsl:number count="section"/>
     </xsl:attribute>
  </a>
  <xsl:apply-templates/>
</h2>
</xsl:template>

<xsl:template match="para">
  <p><xsl:apply-templates/></p>
</xsl:template>

<xsl:template match="code">
  <code><xsl:apply-templates/></code>
</xsl:template>

<xsl:template match="i">
  <i><xsl:apply-templates/></i>
</xsl:template>

<xsl:template match="code.block">
  <pre><xsl:apply-templates/></pre>
</xsl:template>

<xsl:template match="bibref">
  <xsl:text>[</xsl:text>
  <a href="#{@refloc}">
  <xsl:for-each select="/gcapaper/rear/bibliog/bibitem[@id=current()/@refloc]">
    <xsl:number/>
  </xsl:for-each>
  </a>
  <xsl:text>]</xsl:text>
</xsl:template>

<xsl:template match="a">
  <a href="{@href}"><xsl:value-of select="@href"/></a>
</xsl:template>

</xsl:stylesheet>
