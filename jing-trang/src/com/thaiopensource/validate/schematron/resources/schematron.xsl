<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
	xmlns:sch="http://www.ascc.net/xml/schematron"
        xmlns:loc="http://www.thaiopensource.com/ns/location"
	xmlns:saxon="http://icl.com/saxon"
        xmlns:xj="http://xml.apache.org/xslt/java">

<!--
TODO:
Implement subject
Implement diagnostic
-->

<xsl:param name="phase" select="'#ALL'"/>

<xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>

<xsl:output indent="yes"/>

<xsl:template match="/">
  <axsl:stylesheet version="1.0">
    <axsl:template match="/">
      <result>
        <axsl:apply-templates select="/" mode="all"/>
      </result>
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
    <axsl:template match="*|/" mode="all">
      <axsl:apply-templates select="*" mode="all"/>
    </axsl:template>
    <xsl:call-template name="define-location"/>
  </axsl:stylesheet>
</xsl:template>

<xsl:template match="*" mode="pattern">
  <xsl:variable name="pattern-index" select="position()"/>
  <xsl:variable name="last" select="last()"/>
  <xsl:variable name="not-last" select="not(position()=$last)"/>
  <xsl:for-each select="sch:rule">
    <xsl:choose>
      <xsl:when test="@context">
	<axsl:template match="{@context}" mode="M{$pattern-index}" priority="{1 + (1 div position())}"
                       name="R{$pattern-index}.{position()}">
          <xsl:call-template name="location"/>
	  <xsl:apply-templates select="*" mode="assertion"/>
	  <xsl:if test="$not-last">
	     <axsl:apply-templates select="." mode="M{$pattern-index + 1}"/>
	  </xsl:if>
	</axsl:template>
	<axsl:template match="{@context}" mode="all" priority="{($last + 1 - $pattern-index) + (1 div position())}">
          <xsl:call-template name="location"/>
	  <axsl:call-template name="R{$pattern-index}.{position()}"/>
          <axsl:apply-templates select="*" mode="all"/>
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
    <xsl:if test="$not-last">
       <axsl:apply-templates select="." mode="M{$pattern-index + 1}"/>
    </xsl:if>
  </axsl:template>
</xsl:template>

<xsl:template match="sch:extends" mode="assertion">
  <axsl:call-template name="A{normalize-space(@rule)}"/>
</xsl:template>

<xsl:template match="sch:report" mode="assertion">
  <axsl:if test="{@test}">
    <xsl:call-template name="location"/>
    <report>
      <xsl:call-template name="assertion"/>
    </report>
  </axsl:if>
</xsl:template>

<xsl:template match="sch:assert" mode="assertion">
  <axsl:if test="not({@test})">
    <xsl:call-template name="location"/>
    <failed-assertion>
      <xsl:call-template name="assertion"/>
    </failed-assertion>
  </axsl:if>
</xsl:template>

<xsl:template name="assertion">
   <xsl:copy-of select="@role|@test|@icon|@id|@xml:lang"/>
   <axsl:call-template name="location"/>
   <xsl:if test="* or normalize-space(text())">
     <statement>
       <xsl:apply-templates/>
     </statement>
   </xsl:if>
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

<xsl:template match="sch:value-of">
  <axsl:value-of select="{@select}">
    <xsl:call-template name="location"/>
  </axsl:value-of>
</xsl:template>

<xsl:template match="*" mode="assertion"/>

<xsl:variable name="saxon"
              select="function-available('saxon:lineNumber')
                      and function-available('saxon:systemId')"/>

<!-- The JDK 1.4 version of Xalan is buggy and gets an exception if we try
     to use these extension functions, so detect this version and don't use it. -->
<xsl:variable name="xalan"
              xmlns:xalan="http://xml.apache.org/xalan"
              select="function-available('xj:org.apache.xalan.lib.NodeInfo.lineNumber')
                      and function-available('xj:org.apache.xalan.lib.NodeInfo.systemId')
                      and function-available('xalan:checkEnvironment')
                      and not(contains(xalan:checkEnvironment()//item[@key='version.xalan2'],
                                       'Xalan Java 2.2'))"/>

<xsl:template name="define-location">
  <axsl:template name="location">
    <xsl:choose>
      <xsl:when test="$saxon">
	<axsl:attribute name="line-number">
	  <axsl:value-of select="saxon:lineNumber()"/>
	</axsl:attribute>
	<axsl:attribute name="system-id">
	  <axsl:value-of select="saxon:systemId()"/>
	</axsl:attribute>
      </xsl:when>
      <xsl:when test="$xalan">
	<axsl:attribute name="line-number">
	  <axsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.lineNumber()"/>
	</axsl:attribute>
	<axsl:attribute name="system-id">
	  <axsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.systemId()"/>
	</axsl:attribute>
      </xsl:when>
    </xsl:choose>
  </axsl:template>
</xsl:template>

<xsl:template name="location">
  <xsl:choose>
    <xsl:when test="$saxon">
      <xsl:attribute name="loc:line-number">
	<xsl:value-of select="saxon:lineNumber()"/>
      </xsl:attribute>
      <xsl:attribute name="loc:system-id">
	<xsl:value-of select="saxon:systemId()"/>
      </xsl:attribute>
    </xsl:when>
    <xsl:when test="$xalan">
      <xsl:attribute name="loc:line-number">
	<xsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.lineNumber()"/>
      </xsl:attribute>
      <xsl:attribute name="loc:system-id">
	<xsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.systemId()"/>
      </xsl:attribute>
    </xsl:when>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
