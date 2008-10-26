<!-- Translate from RELAX Core to RELAX NG. -->
<!-- This does not apply the RELAX semantic of implicitly allowing
undeclared attributes. Only declared attributes are allowed. -->

<xsl:stylesheet
  version="1.0"
  xmlns="http://relaxng.org/ns/structure/0.9"
  xmlns:rlx="http://www.xml.gr.jp/xmlns/relaxCore"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="rlx">

<xsl:param name="labelSuffix" select="'.label'"/>
<xsl:param name="roleSuffix" select="'.role'"/>

<xsl:output encoding="iso-8859-1" indent="yes"/>

<xsl:strip-space elements="rlx:*"/>

<xsl:template match="/">
  <xsl:choose>
    <xsl:when test="function-available('msxsl:node-set')"
              xmlns:msxsl="urn:schemas-microsoft-com:xslt">
      <xsl:variable name="pass1">
        <xsl:apply-templates mode="pass1"/>
      </xsl:variable>
      <xsl:apply-templates select="msxsl:node-set($pass1)/*"/>
    </xsl:when>
    <xsl:when test="function-available('saxon:nodeSet')"
              xmlns:saxon="http://icl.com/saxon">
      <xsl:variable name="pass1">
        <xsl:apply-templates mode="pass1"/>
      </xsl:variable>
      <xsl:apply-templates select="saxon:nodeSet($pass1)/*"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*" mode="pass1">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="pass1"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="rlx:include" mode="pass1">
  <xsl:apply-templates select="document(@moduleLocation)/*/*" mode="pass1"/>
</xsl:template>

<xsl:template match="rlx:module">
 <grammar ns="{@targetNamespace}"
          datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
   <xsl:apply-templates/>
 </grammar>
</xsl:template>

<xsl:key name="tag" match="rlx:tag" use="@role"/>
<xsl:key name="elementRule" match="rlx:elementRule[@label]" use="@label"/>
<xsl:key name="elementRule" match="rlx:elementRule[not(@label)]" use="@role"/>

<xsl:template match="rlx:elementRule">
  <xsl:variable name="label">
    <xsl:choose>
      <xsl:when test="@label">
        <xsl:value-of select="@label"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@role"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="shared" select="key('elementRule',$label)"/>
  <!-- Is this the first that shares the label? -->
  <xsl:if test="count($shared[1]|.)=1">
    <define name="{$label}{$labelSuffix}">
      <choice>
	<xsl:for-each select="$shared">
	  <element>
	    <xsl:attribute name="name">
	      <xsl:choose>
		<xsl:when test="rlx:tag/@name">
		  <xsl:value-of select="rlx:tag/@name"/>
		</xsl:when>
		<xsl:when test="rlx:tag">
		  <xsl:value-of select="@label"/>
		</xsl:when>
		<xsl:when test="key('tag',@role)">
		  <xsl:value-of select="key('tag',@role)/@name"/>
		</xsl:when>
		<xsl:otherwise>
		  <xsl:value-of select="@role"/>
		</xsl:otherwise>
	      </xsl:choose>
	    </xsl:attribute>
	    <xsl:choose>
	      <xsl:when test="rlx:tag">
		<xsl:apply-templates select="rlx:tag/*"/>
	      </xsl:when>
	      <xsl:otherwise>
		<ref name="{@role}{$roleSuffix}"/>
	      </xsl:otherwise>
	    </xsl:choose>
	    <xsl:choose>
	      <xsl:when test="@type">
		<xsl:call-template name="dataType"/>
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:apply-templates/>
	      </xsl:otherwise>
	    </xsl:choose>
	  </element>
	</xsl:for-each>
      </choice>
    </define>
  </xsl:if>
</xsl:template>

<xsl:template match="rlx:elementRule/rlx:tag">
</xsl:template>

<xsl:template match="rlx:*[@occurs='?']">
  <optional>
    <xsl:apply-templates mode="noOccur" select="."/>
  </optional>
</xsl:template>

<xsl:template match="rlx:*[@occurs='*']">
  <zeroOrMore>
    <xsl:apply-templates mode="noOccur" select="."/>
  </zeroOrMore>
</xsl:template>

<xsl:template match="rlx:*[@occurs='+']">
  <oneOrMore>
    <xsl:apply-templates mode="noOccur" select="."/>
  </oneOrMore>
</xsl:template>

<xsl:template match="rlx:*">
  <xsl:apply-templates select="." mode="noOccur"/>
</xsl:template>

<xsl:template match="rlx:sequence" mode="noOccur">
  <group>
    <xsl:apply-templates/>
  </group>
</xsl:template>  

<xsl:template match="rlx:choice" mode="noOccur">
  <choice>
    <xsl:apply-templates/>
  </choice>
</xsl:template>  

<xsl:template match="rlx:mixed" mode="noOccur">
  <mixed>
    <xsl:apply-templates/>
  </mixed>
</xsl:template>  


<xsl:template match="rlx:tag">
  <define>
    <xsl:attribute name="name">
     <xsl:choose>
       <xsl:when test="@role">
	  <xsl:value-of select="@role"/>
       </xsl:when>
       <xsl:otherwise>
	  <xsl:value-of select="@name"/>
       </xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="$roleSuffix"/>
    </xsl:attribute>
    <empty/>
    <xsl:apply-templates/>
  </define>
</xsl:template>

<xsl:template match="rlx:none">
  <notAllowed/>
</xsl:template>

<xsl:template match="rlx:empty">
  <empty/>
</xsl:template>

<xsl:template match="rlx:attPool">
  <define name="{@role}{$roleSuffix}">
    <empty/>
    <xsl:apply-templates/>
  </define>
</xsl:template>

<xsl:template match="rlx:div">
  <div>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="rlx:attribute">
  <optional>
    <attribute name="{@name}">
      <xsl:call-template name="dataType"/>
    </attribute>
  </optional>
</xsl:template>

<xsl:template match="rlx:attribute[@required='true']">
  <attribute name="{@name}">
    <xsl:call-template name="dataType">
      <xsl:with-param name="isAttribute" select="true()"/>
    </xsl:call-template>
  </attribute>
</xsl:template>

<xsl:template match="rlx:attribute[@type='none']">
</xsl:template>

<xsl:template match="rlx:ref[@role]">
  <ref name="{@role}{$roleSuffix}"/>
</xsl:template>

<xsl:template match="rlx:ref[@label]" mode="noOccur">
  <ref name="{@label}{$labelSuffix}"/>
</xsl:template>

<xsl:key name="hedgeRule" match="rlx:hedgeRule" use="@label"/>

<xsl:template match="rlx:hedgeRule">
  <xsl:variable name="shared" select="key('hedgeRule',@label)"/>
  <xsl:if test="count(.|$shared[1])=1">
    <define name="{@label}{$labelSuffix}">
      <choice>
        <xsl:for-each select="$shared">
          <xsl:apply-templates/>
        </xsl:for-each>
      </choice>
    </define>
  </xsl:if>
</xsl:template>

<xsl:template match="rlx:interface">
   <xsl:variable name="shared" select="//rlx:interface"/>
   <xsl:if test="count($shared[1]|.)=1">
     <start>
      <choice>
	<xsl:for-each select="$shared">
	  <xsl:apply-templates/>
	</xsl:for-each>
      </choice>
     </start>
   </xsl:if>
</xsl:template>

<xsl:template match="rlx:export">
  <ref name="{@label}{$labelSuffix}"/>
</xsl:template>

<xsl:template match="rlx:hedgeRef" mode="noOccur">
  <ref name="{@label}{$labelSuffix}"/>
</xsl:template>

<xsl:template match="rlx:element" mode="noOccur">
  <element name="{@name}">
    <xsl:call-template name="dataType"/>
  </element>
</xsl:template>

<xsl:template name="dataType">
  <xsl:choose>
    <xsl:when test="@type='emptyString'">
      <empty/>
    </xsl:when>
    <xsl:when test="@type='none'">
      <notAllowed/>
    </xsl:when>
    <xsl:when test="rlx:enumeration">
      <choice>
	<xsl:for-each select="rlx:enumeration">
	  <value type="{../@type}">
	    <xsl:value-of select="@value"/>
	  </value>
	</xsl:for-each>
      </choice>
    </xsl:when>
    <xsl:when test="@type='binary' and rlx:encoding[@value='base64']">
      <data type="base64Binary">
        <xsl:apply-templates mode="facet"/>
      </data>
    </xsl:when>
    <xsl:when test="@type='binary' and rlx:encoding[@value='hex']">
      <data type="hexBinary">
        <xsl:apply-templates mode="facet"/>
      </data>
    </xsl:when>
    <xsl:when test="@type='uriReference'">
      <data type="anyURI">
        <xsl:apply-templates mode="facet"/>
      </data>
    </xsl:when>
    <xsl:when test="@type='timeDuration'">
      <data type="duration">
        <xsl:apply-templates mode="facet"/>
      </data>
    </xsl:when>
    <xsl:when test="@type='timeInstant'">
      <data type="dateTime">
        <xsl:apply-templates mode="facet"/>
      </data>
    </xsl:when>
    <xsl:when test="@type='ID'">
      <data type="NCName" key="ID">
        <xsl:apply-templates mode="facet"/>
      </data>
    </xsl:when>
    <xsl:when test="@type='IDREF'">
      <data type="NCName" keyRef="ID">
        <xsl:apply-templates mode="facet"/>
      </data>
    </xsl:when>
    <xsl:when test="@type='IDREFS'">
      <list>
        <data type="NCName" keyRef="ID">
          <xsl:apply-templates mode="facet"/>
        </data>
      </list>
    </xsl:when>
    <xsl:when test="@type">
      <data type="{@type}">
        <xsl:apply-templates mode="facet"/>
      </data>
    </xsl:when>
    <xsl:otherwise>
      <text/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="rlx:annotation">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="rlx:include">
  <xsl:message>The "include" element is only supported with MSXSL and SAXON.</xsl:message>
</xsl:template>

<xsl:template match="rlx:pattern|rlx:length|rlx:minLength|rlx:maxLength
                    |rlx:minExclusive|rlx:maxExclusive
                    |rlx:minInclusive|rlx:maxInclusive"
              mode="facet">
  <param name="{local-name()}">
    <xsl:value-of select="@value"/>
  </param>
</xsl:template>

<xsl:template match="rlx:scale">
  <param name="fractionDigits">
    <xsl:value-of select="@value"/>
  </param>
</xsl:template>

<xsl:template match="rlx:precision">
  <param name="totalDigits">
    <xsl:value-of select="@value"/>
  </param>
</xsl:template>

<xsl:template match="*" mode="facet">
</xsl:template>

</xsl:stylesheet>
