<!-- XSLT stylesheet to convert nrl.xml into gcapaper format. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output doctype-public="-//GCA//DTD GCAPAP-X DTD 20021024 Vers 6.2//EN"
	    doctype-system="gcapaper.dtd"/>

<xsl:variable name="indent" select="'  '"/>

<xsl:template match="html">
  <gcapaper>
    <front>
      <title><xsl:value-of select="head/title"/></title>
      <author>
	<fname>James</fname>
	<surname>Clark</surname>
	<address>
	  <affil>Thai Open Source Software Center</affil>
	  <country>Thailand</country>
	  <email>jjc@thaiopensource.com</email>
	  <web>http://www.thaiopensource.com</web>
	</address>
<bio>
<para>James Clark has been involved with SGML and XML for more than 10
years, both in contributing to standards and in creating open source
software. James was technical lead of the XML WG during the creation
of the XML 1.0 Recommendation. He was editor of the XPath and XSLT
Recommendations. He was the main author of the DSSSL (ISO 10179)
standard. Currently, he is chair of the OASIS RELAX NG TC and editor
of the RELAX NG specification. The open source software that James has
written includes SGML parsers (sgmls and SP), a DSSSL implementation
(Jade), XML parsers (expat and XP), an XPath/XSLT processor (XT), a
RELAX NG validator (Jing), a schema conversion tool (Trang), and an
XML mode for GNU Emacs (nXML mode). Prior to his involvement with SGML
and XML, James wrote the GNU groff typesetting system. James read
Mathematics and Philosophy at Merton College, Oxford, where he
obtained First Class Honours. James lives in Thailand, where he runs
the Thai Open Source Software Center.</para>
</bio>
      </author>
      <abstract>
	<xsl:apply-templates select="body/abstract/*"/>
      </abstract>
    </front>
    <body>
      <xsl:for-each select="body/div[h2 and not(h2 = 'Acknowledgements')]">
        <section id="{translate(normalize-space(h2),' ','_')}">
	  <xsl:apply-templates select="*"/>
	</section>
      </xsl:for-each>
    </body>
    <rear>
      <acknowl>
	<xsl:apply-templates select="body/div[h2 = 'Acknowledgements']/p"/>
      </acknowl>
      <bibliog>
	<xsl:for-each select="//bibentry">
	  <xsl:sort select="@name"/>
	  <bibitem id="{@name}">
	    <bib><xsl:value-of select="@name"/></bib>
	    <pub><xsl:apply-templates/></pub>
	  </bibitem>
	</xsl:for-each>
      </bibliog>
    </rear>
  </gcapaper>
</xsl:template>

<xsl:template match="url">
  <a href="{.}"/>
</xsl:template>

<xsl:template match="bib">
  <xsl:apply-templates/>
  <bibref refloc="{@ref}"/>
</xsl:template>

<xsl:template match="h2">
  <title><xsl:apply-templates/></title>
</xsl:template>

<xsl:template match="dl">
  <deflist>
    <xsl:for-each select="dt">
      <def.item>
	<def.term>
	  <xsl:value-of select="."/>
	</def.term>
	<def>
	  <para>
	    <xsl:apply-templates select="following::dd[1]/node()"/>
	  </para>
	</def>
      </def.item>
    </xsl:for-each>
  </deflist>
</xsl:template>

<xsl:template match="ul">
  <randlist>
    <xsl:apply-templates/>
  </randlist>
</xsl:template>

<xsl:template match="ol">
  <seqlist>
    <xsl:apply-templates/>
  </seqlist>
</xsl:template>

<xsl:template match="ol/li|ul/li">
  <li>
    <para>
      <xsl:apply-templates/>
    </para>
  </li>
</xsl:template>

<xsl:template match="var|em">
  <i>
    <xsl:apply-templates/>
  </i>
</xsl:template>

<xsl:template match="code">
  <xsl:text>&#x2018;</xsl:text>
    <xsl:apply-templates/>
  <xsl:text>&#x2019;</xsl:text>
</xsl:template>

<xsl:template match="pre">
  <code.block>
    <xsl:apply-templates/>
  </code.block>
</xsl:template>

<xsl:template match="p">
  <para>
    <xsl:apply-templates/>
  </para>
</xsl:template>

<xsl:template match="*">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="xml">
  <code.block>
    <xsl:apply-templates select="*" mode="print">
      <xsl:with-param name="top" select="true()"/>
    </xsl:apply-templates>
  </code.block>
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

<xsl:template match="href">
  <xref refloc="{translate(normalize-space(),' ','_')}"/>
</xsl:template>

</xsl:stylesheet>

