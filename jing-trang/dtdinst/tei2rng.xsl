<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://icl.com/saxon"
  extension-element-prefixes="saxon"
  xmlns:t="http://www.thaiopensource.com/ns/annotations"
  xmlns:a="http://relaxng.org/ns/compatibility/annotations/0.9"
  xmlns="http://relaxng.org/ns/structure/0.9">

<xsl:import href="dtdinst2rng.xsl"/>

<xsl:output indent="yes" encoding="iso-8859-1"/>

<xsl:param name="out-suffix" select="'.rng'"/>
<xsl:param name="element-prefix" select="''"/>
<xsl:param name="attlist-prefix" select="'al.'"/>

<xsl:key name="override" match="overridden[not(duplicate)]" use="*/@name"/>

<xsl:template match="doctype">
  <grammar datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
    <xsl:apply-templates/>
  </grammar>
</xsl:template>

<xsl:template match="modelGroup|datatype">
  <define name="{@name}">
    <xsl:if test="key('override',@name)">
      <xsl:call-template name="condition"/>
    </xsl:if>
    <xsl:apply-templates/>
  </define>
</xsl:template>

<xsl:template match="attributeGroup">
  <define name="{@name}">
    <xsl:if test="not(*)">
      <empty/>
    </xsl:if>
    <xsl:apply-templates/>
  </define>
</xsl:template>

<xsl:template match="default|fixed|implied" mode="required">
  <xsl:param name="content"/>
  <optional>
    <xsl:for-each select="ancestor::attributeGroup">
      <xsl:if test="key('override',@name)">
	<xsl:call-template name="condition"/>
      </xsl:if>
    </xsl:for-each>
    <xsl:copy-of select="$content"/>
  </optional>
</xsl:template>

<xsl:template match="attributeDefaultRef[@name='INHERITED']"
              mode="default-value">
  <xsl:attribute name="t:inherited">true</xsl:attribute>
</xsl:template>

<xsl:template match="flag">
  <xsl:if test="starts-with(@name,'TEI.')
                and @name != 'TEI.2'
                and @name != 'TEI.general'
                and @name != 'TEI.singleBase'">
    <define name="{@name}">
      <xsl:apply-templates/>
    </define>
  </xsl:if>
</xsl:template>

<!-- Define the marked section keywords as ignore in teikey2. -->
<xsl:template match="overridden[flag[starts-with(@name,'TEI.') 
                                     and not(@name='TEI.XML')
                                     and not(@name='TEI.general')
                                     and ignore]]">
  <define name="{flag/@name}">
    <ref name="IGNORE"/>
  </define>
</xsl:template>

<xsl:template match="externalIdRef">
  <xsl:variable name="f"
      select="concat(key('param',@name)/@system,$out-suffix)"/>
  <include href="{$f}"/>
  <saxon:output href="{$f}">
    <grammar datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
      <xsl:if test="$f = concat('tei2.dtd', $out-suffix)">
        <xsl:call-template name="main"/>
      </xsl:if>
      <xsl:if test="$f = concat('teikey2.ent', $out-suffix)">
        <xsl:call-template name="key"/>
      </xsl:if>
      <xsl:apply-templates/>
    </grammar>
  </saxon:output>
</xsl:template>

<xsl:template match="element">
  <xsl:variable name="name">
    <xsl:apply-templates select="*[1]"/>
  </xsl:variable>
  <define name="{$element-prefix}{$name}">
    <xsl:call-template name="condition"/>
    <element name="{$name}">
      <ref name="{$attlist-prefix}{$name}"/>
      <xsl:apply-templates select="*[2]"/>
    </element>
  </define>
</xsl:template>

<xsl:template name="condition">
  <xsl:variable name="flag"
    select="ancestor::includedSection[starts-with(@flag,'TEI.')
                                      and @flag != 'TEI.2'
                                      and @flag != 'TEI.mixed'
                                      and @flag != 'TEI.general'
                                      and @flag != 'TEI.singleBase'][1]/@flag"/>
  <xsl:if test="$flag">
    <ref name="{$flag}"/>
  </xsl:if>
</xsl:template>

<!-- This puts the definition of gram in the 'right' place (in the
dictonaries module. -->

<xsl:template match="ignoredSection[@flag='gram'][ancestor::externalIdRef[@name='TEI.dictionaries.dtd']]">
  <xsl:apply-templates select="//element[nameSpecRef[@name='n.gram']]"/>
  <xsl:apply-templates select="//attlist[nameSpecRef[@name='n.gram']]"/>
</xsl:template>

<!-- This makes things work when TEI.mixed is turned on. -->

<xsl:template match="ignoredSection[@flag='TEI.mixed'
                                    and contains(., 'component')]">
<xsl:text>
</xsl:text>
<xsl:comment>
  <xsl:value-of select="substring-before(substring-after(.,'&lt;!--'),'--&gt;')"/>
</xsl:comment>
<define name="component" combine="choice">
  <ref name="TEI.mixed"/>
  <xsl:apply-templates select="key('param','component')/*"/>
</define>
</xsl:template>

<xsl:template name="main">
  <start>
    <choice>
      <ref name="TEI.2"/>
      <ref name="teiCorpus.2"/>
    </choice>
  </start>
  <define name="TEI...end"><notAllowed/></define>
  <define name="dictScrap"><notAllowed/></define>
  <define name="att" combine="choice"><notAllowed/></define>
  <define name="gi" combine="choice"><notAllowed/></define>
  <define name="tag" combine="choice"><notAllowed/></define>
  <define name="val" combine="choice"><notAllowed/></define>
</xsl:template>

<xsl:template name="key">
  <define name="IGNORE"><notAllowed/></define>
  <define name="INCLUDE"><empty/></define>
</xsl:template>

<xsl:template match="include">
  <ref name="INCLUDE"/>
</xsl:template>

<xsl:template match="ignore">
  <ref name="IGNORE"/>
</xsl:template>

</xsl:stylesheet>
