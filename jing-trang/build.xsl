<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output indent="yes"
	    encoding="utf-8"/>
	    
<xsl:variable name="build" select="'${build.dir}'"/>
<xsl:template match="/">
  <project>
    <property name="build.dir" value="${{basedir}}/build"/>
    <target name="init">
      <mkdir dir="{$build}"/>
    </target>
    <xsl:for-each select="modules/module">
      <xsl:apply-templates select="document(concat('mod/', .,'/mod.xml'), .)/module">
	<xsl:with-param name="name" select="string(.)"/>
      </xsl:apply-templates>
    </xsl:for-each>
    <target name="gen">
      <xsl:attribute name="depends">
	<xsl:text>init</xsl:text>
	<xsl:for-each select="modules/module">
	  <xsl:for-each select="document(concat('mod/', .,'/mod.xml'), .)/module/ant/@precompile">
	    <xsl:text>,</xsl:text>
	    <xsl:value-of select="."/>
	  </xsl:for-each>
	</xsl:for-each>
      </xsl:attribute>
    </target>
    <target name="compile" depends="jing::compile-main,trang::compile-main"/>
    <target name="jar" depends="jing::jar,trang::jar"/>
  </project>
</xsl:template>


<xsl:template match="*"/>

<xsl:template match="module">
  <xsl:param name="name"/>
  <xsl:copy-of select="ant/*"/>
  <target name="{$name}::compile-main">
    <xsl:attribute name="depends">
      <xsl:text>init</xsl:text>
      <xsl:if test="ant/@precompile">
	<xsl:text>,</xsl:text>
	<xsl:value-of select="ant/@precompile"/>
      </xsl:if>
      <xsl:for-each select="depends[@module]">
	<xsl:text>,</xsl:text>
	<xsl:value-of select="@module"/>
	<xsl:text>::compile-main</xsl:text>
      </xsl:for-each>
    </xsl:attribute>
    <mkdir dir="{$build}/mod/{$name}/classes/main"/>
    <xsl:if test="compile">
      <javac destdir="{$build}/mod/{$name}/classes/main"
	     includeAntRuntime="no">
	<src>
	  <pathelement location="mod/{$name}/src/main"/>
	  <xsl:if test="ant/@precompile">
	    <pathelement location="{$build}/mod/{$name}/gensrc/main"/>
	  </xsl:if>
	</src>
	<classpath>
	  <xsl:for-each select="depends[@module]">
	    <pathelement location="{$build}/mod/{@module}/classes/main"/>
	  </xsl:for-each>
	  <xsl:for-each select="depends[@lib]">
	    <pathelement location="lib/{@lib}.jar"/>
	  </xsl:for-each>
	</classpath>
      </javac>
    </xsl:if>
  </target>
  <target name="{$name}::compile-test">
    <xsl:attribute name="depends">
      <xsl:value-of select="concat($name, '::compile-main')"/>
      <xsl:for-each select="depends[@module]">
	<xsl:text>,</xsl:text>
	<xsl:value-of select="@module"/>
	<xsl:text>::compile-test</xsl:text>
      </xsl:for-each>
    </xsl:attribute>
    <mkdir dir="{$build}/mod/{$name}/classes/test"/>
    <xsl:if test="compile[@test]">
      <javac destdir="{$build}/mod/{$name}/classes/test"
	     includeAntRuntime="no">
	<src>
	  <pathelement location="mod/{$name}/src/test"/>
	</src>
	<classpath>
	  <pathelement location="{$build}/mod/{$name}/classes/main"/>
	  <xsl:for-each select="depends[@module]">
	    <pathelement location="{$build}/mod/{@module}/classes/main"/>
	    <pathelement location="{$build}/mod/{@module}/classes/test"/>
	  </xsl:for-each>
	  <xsl:for-each select="depends[@lib]">
	    <pathelement location="lib/{@lib}.jar"/>
	  </xsl:for-each>
	</classpath>
      </javac>
    </xsl:if>
  </target>
  <target name="{$name}::jar" depends="{$name}::compile-main">
    <jar jarfile="{$build}/{$name}.jar">
      <xsl:copy-of select="jar/*"/>
      <xsl:if test="compile">
	<fileset dir="{$build}/mod/{$name}/classes/main"/>
	<fileset dir="mod/{$name}/src/main" includes="**/resources/*"/>
      </xsl:if>
      <xsl:for-each select="depends[@module]">
	<fileset dir="{$build}/mod/{@module}/classes/main"/>
	<fileset dir="mod/{@module}/src/main" includes="**/resources/*"/>
      </xsl:for-each>
    </jar>
  </target>
</xsl:template>

</xsl:stylesheet>
