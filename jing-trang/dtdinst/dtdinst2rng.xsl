<?xml version="1.0"?>
<!-- TODO: Deal with fixed attributes properly. -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:a="http://relaxng.org/ns/compatibility/annotations/0.9"
  xmlns="http://relaxng.org/ns/structure/0.9">

<xsl:param name="element-prefix" select="'element.'"/>
<xsl:param name="attlist-prefix" select="'attlist.'"/>

<xsl:strip-space elements="*"/>

<xsl:key name="param"
         match="flag[not(parent::overridden)]
                |nameSpec[not(parent::overridden)]
                |modelGroup[not(parent::overridden)]
                |attributeGroup[not(parent::overridden)]
                |enumGroup[not(parent::overridden)]
                |externalId[not(parent::overridden)]
                |datatype[not(parent::overridden)]
                |param[not(parent::overridden)]
                |attributeDefault[not(parent::overridden)]"
         use="@name"/>

<xsl:template match="comment">
  <xsl:text>
</xsl:text>
  <xsl:comment><xsl:value-of select="."/></xsl:comment>
</xsl:template>

<xsl:template match="processingInstruction">
  <xsl:text>
</xsl:text>
  <xsl:processing-instruction name="{@target}">
    <xsl:value-of select="."/>
  </xsl:processing-instruction>
</xsl:template>

<xsl:template match="doctype">
  <grammar datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
    <xsl:apply-templates/>
  </grammar>
</xsl:template>

<xsl:template match="modelGroup|datatype">
  <define name="{@name}">
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

<xsl:template match="include">
  <empty/>
</xsl:template>

<xsl:template match="ignore">
  <notAllowed/>
</xsl:template>

<xsl:template match="nameSpec"/>

<xsl:template match="modelGroupRef|datatypeRef|attributeGroupRef">
  <ref name="{@name}"/>
</xsl:template>

<xsl:template match="element">
  <xsl:variable name="name">
    <xsl:apply-templates select="*[1]"/>
  </xsl:variable>
  <define name="{$element-prefix}{$name}">
    <element name="{$name}">
      <ref name="{$attlist-prefix}{$name}"/>
      <xsl:apply-templates select="*[2]"/>
    </element>
  </define>
  <define name="{$attlist-prefix}{$name}" combine="interleave">
    <empty/>
  </define>
</xsl:template>

<xsl:template match="elementRef">
  <ref>
    <xsl:attribute name="name">
      <xsl:value-of select="$element-prefix"/>
      <xsl:apply-templates/>
    </xsl:attribute>
  </ref>
</xsl:template>

<xsl:template match="attribute">
  <xsl:variable name="name">
    <xsl:apply-templates select="*[1]"/>
  </xsl:variable>
  <xsl:apply-templates mode="required" select="*[3]">
    <xsl:with-param name="content">
      <attribute name="{$name}">
	<xsl:apply-templates mode="default-value" select="*[3]"/>
	<xsl:apply-templates select="*[2]"/>
      </attribute>
    </xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="default|fixed|implied" mode="required">
  <xsl:param name="content"/>
  <optional>
    <xsl:copy-of select="$content"/>
  </optional>
</xsl:template>

<xsl:template match="required" mode="required">
  <xsl:param name="content"/>
  <xsl:copy-of select="$content"/>
</xsl:template>

<xsl:template match="attributeDefaultRef" mode="required">
  <xsl:param name="content"/>
  <xsl:apply-templates mode="required" select="key('param',@name)/*">
    <xsl:with-param name="content" select="$content"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="default|fixed" mode="default-value">
  <xsl:attribute name="a:defaultValue">
    <xsl:value-of select="."/>
  </xsl:attribute>
</xsl:template>

<xsl:template match="implied|required" mode="default-value"/>

<xsl:template match="attributeDefaultRef" mode="default-value">
  <xsl:apply-templates mode="default-value" select="key('param',@name)/*"/>
</xsl:template>

<xsl:template match="attlist">
  <xsl:variable name="name">
    <xsl:apply-templates select="*[1]"/>
  </xsl:variable>
  <define name="{$attlist-prefix}{$name}" combine="interleave">
    <xsl:apply-templates select="*[position()&gt;1]"/>
  </define>
</xsl:template>

<xsl:template match="nameSpecRef">
  <xsl:apply-templates select="key('param',@name)/*"/>
</xsl:template>

<xsl:template match="name">
  <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="param|overridden|internalEntity|ignoredSection|default"/>

<xsl:template match="cdata|pcdata">
  <text/>
</xsl:template>

<xsl:template match="tokenized">
  <xsl:choose>
    <xsl:when test="@name">
      <data type="{@name}"/>
    </xsl:when>
    <xsl:otherwise>
      <choice>
        <xsl:apply-templates/>
      </choice>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="enum">
  <value><xsl:value-of select="."/></value>
</xsl:template>

<xsl:template match="choice[not(*)]">
  <notAllowed/>
</xsl:template>

<xsl:template match="sequence[not(*)]">
  <empty/>
</xsl:template>

<xsl:template match="choice">
  <choice>
    <xsl:apply-templates/>
  </choice>
</xsl:template>

<xsl:template match="sequence">
  <group>
    <xsl:apply-templates/>
  </group>
</xsl:template>

<xsl:template match="oneOrMore">
  <oneOrMore><xsl:apply-templates/></oneOrMore>
</xsl:template>

<xsl:template match="zeroOrMore">
  <zeroOrMore><xsl:apply-templates/></zeroOrMore>
</xsl:template>

<xsl:template match="optional">
  <optional><xsl:apply-templates/></optional>
</xsl:template>

</xsl:stylesheet>
