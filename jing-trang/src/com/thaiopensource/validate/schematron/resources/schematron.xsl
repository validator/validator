<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
	xmlns:sch="http://www.ascc.net/xml/schematron"
        xmlns:o="http://www.thaiopensource.com/schematron-output"
        xmlns:loc="http://www.thaiopensource.com/location"
        exclude-result-prefixes="sch">

<xsl:param name="phase" select="'#ALL'"/>

<xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>

<xsl:output indent="yes"/>

<xsl:template match="/">
  <axsl:stylesheet version="1.0">
    <axsl:template match="/">
      <o:result>
        <axsl:apply-templates select="*"/>
      </o:result>
    </axsl:template>
    <xsl:choose>
      <xsl:when test="$phase='#ALL'">
	<xsl:apply-templates select="*/sch:pattern" mode="pattern"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:for-each select="*/sch:phase[normalize-space(@id)=normalize-space($phase)]/sch:active">
	  <xsl:variable name="id" select="normalize-space(current()/@pattern)"/>
	  <xsl:apply-templates select="/*/sch:pattern[normalize-space(@id)=$id]" mode="pattern"/>
	</xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
    <axsl:template match="*">
      <axsl:apply-templates select="*"/>
    </axsl:template>
    <xsl:call-template name="location"/>
  </axsl:stylesheet>
</xsl:template>

<xsl:template match="*" mode="pattern">
  <xsl:variable name="pattern-index" select="position()"/>
  <xsl:variable name="last" select="last()"/>
  <xsl:variable name="notLast" select="not(position()=$last)"/>
  <xsl:for-each select="sch:rule">
    <xsl:choose>
      <xsl:when test="@context">
	<axsl:template match="{@context}" name="R{$pattern-index}.{position()}"
                       priority="{($last + 1 - $pattern-index) + (1 div position())}"
                       xsl:use-attribute-sets="location">
	  <xsl:apply-templates select="*" mode="assertion"/>
	  <xsl:if test="$notLast">
	     <axsl:apply-templates select="." mode="M{$pattern-index + 1}"/>
	  </xsl:if>
	</axsl:template>
	<axsl:template match="{@context}" mode="M{$pattern-index}" priority="{1 + (1 div position())}"
           xsl:use-attribute-sets="location">
	   <axsl:call-template name="R{$pattern-index}.{position()}"/>
	</axsl:template>
      </xsl:when>
      <xsl:otherwise>
        <axsl:template name="A{normalize-space(@id)}">
	  <xsl:apply-templates select="*" mode="assertion"/>
        </axsl:template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:for-each>
  <axsl:template match="*" mode="M{$pattern-index}">
    <xsl:if test="$notLast">
       <axsl:apply-templates select="." mode="M{$pattern-index + 1}"/>
    </xsl:if>
  </axsl:template>
</xsl:template>

<xsl:template match="sch:extends" mode="assertion">
  <axsl:call-template name="A{normalize-space(@rule)}"/>
</xsl:template>

<xsl:template match="sch:report" mode="assertion">
  <axsl:if test="{@test}"  xsl:use-attribute-sets="location">
    <o:report axsl:use-attribute-sets="location">
      <xsl:apply-templates/>
    </o:report>
  </axsl:if>
</xsl:template>

<xsl:template match="sch:assert" mode="assertion">
  <axsl:if test="not({@test})"  xsl:use-attribute-sets="location">
    <o:assertionFailed axsl:use-attribute-sets="location">
      <xsl:apply-templates/>
    </o:assertionFailed>
  </axsl:if>
</xsl:template>

<xsl:template match="sch:name">
  <xsl:choose>
    <xsl:when test="@path">
      <axsl:value-of select="name({@path})"/>
    </xsl:when>
    <xsl:otherwise>
      <axsl:value-of select="name()"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*" mode="assertion"/>

<xsl:template name="location">
  <axsl:attribute-set name="location"
         xmlns:saxon="http://icl.com/saxon"
         xmlns:xj="http://xml.apache.org/xalan/java">
    <axsl:attribute name="line-number">
      <xsl:choose>
	<xsl:when test="function-available('saxon:lineNumber')">
	  <axsl:value-of select="saxon:lineNumber()"/>
	</xsl:when>
	<xsl:when test="function-available('xj:org.apache.xalan.lib.NodeInfo.lineNumber')">
	  <axsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.lineNumber()"/>
	</xsl:when>
	<xsl:otherwise>-1</xsl:otherwise>
      </xsl:choose>
    </axsl:attribute>
    <axsl:attribute name="column-number">
      <xsl:choose>
	<xsl:when test="function-available('xj:org.apache.xalan.lib.NodeInfo.columnNumber')">
	  <axsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.columnNumber()"/>
	</xsl:when>
	<xsl:otherwise>-1</xsl:otherwise>
      </xsl:choose>
    </axsl:attribute>
    <axsl:attribute name="system-id">
      <xsl:choose>
	<xsl:when test="function-available('saxon:systemId')">
	  <axsl:value-of select="saxon:systemId()"/>
	</xsl:when>
	<xsl:when test="function-available('xj:org.apache.xalan.lib.NodeInfo.systemId')">
	  <axsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.systemId()"/>
	</xsl:when>
	<xsl:otherwise/>
      </xsl:choose>
    </axsl:attribute>
    <axsl:attribute name="public-id">
      <xsl:choose>
	<xsl:when test="function-available('xj:org.apache.xalan.lib.NodeInfo.publicId')">
	  <axsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.publicId()"/>
	</xsl:when>
	<xsl:otherwise/>
      </xsl:choose>
    </axsl:attribute>
  </axsl:attribute-set>
</xsl:template>

<xsl:attribute-set name="location" xmlns:saxon="http://icl.com/saxon"
              xmlns:xj="http://xml.apache.org/xslt/java">
  <xsl:attribute name="loc:line-number">
    <xsl:choose>
      <xsl:when test="function-available('saxon:lineNumber')">
	<xsl:value-of select="saxon:lineNumber()"/>
      </xsl:when>
      <xsl:when test="function-available('xj:org.apache.xalan.lib.NodeInfo.lineNumber')">
	<xsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.lineNumber()"/>
      </xsl:when>
      <xsl:otherwise>-1</xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
  <xsl:attribute name="loc:column-number">
    <xsl:choose>
      <xsl:when test="function-available('xj:org.apache.xalan.lib.NodeInfo.columnNumber')">
	<xsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.columnNumber()"/>
      </xsl:when>
      <xsl:otherwise>-1</xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
  <xsl:attribute name="loc:system-id">
    <xsl:choose>
      <xsl:when test="function-available('saxon:systemId')">
	<xsl:value-of select="saxon:systemId()"/>
      </xsl:when>
      <xsl:when test="function-available('xj:org.apache.xalan.lib.NodeInfo.systemId')">
	<xsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.systemId()"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:attribute>
  <xsl:attribute name="loc:public-id">
    <xsl:choose>
      <xsl:when test="function-available('xj:org.apache.xalan.lib.NodeInfo.publicId')">
	<xsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.publicId()"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:attribute>
</xsl:attribute-set>

</xsl:stylesheet>
