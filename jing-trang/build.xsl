<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output indent="yes"
	    encoding="utf-8"/>
	    
<xsl:variable name="build" select="'${build.dir}'"/>
<xsl:template match="/">
  <project>
    <property name="build.dir" value="${{basedir}}/build"/>
    <target name="dummy"/>
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
    <target name="jar" depends="dtdinst::jar,jing::jar,trang::jar"/>
    <target name="jing-jar" depends="jing::jar">
      <taskdef name="jing" classname="com.thaiopensource.relaxng.util.JingTask">
	<classpath>
	  <pathelement location="{$build}/jing.jar"/>
	</classpath>
      </taskdef>
    </target>

    <target name="test">
      <xsl:attribute name="depends">
	<xsl:text>init</xsl:text>
	<xsl:for-each select="modules/module">
	  <xsl:text>,</xsl:text>
	  <xsl:value-of select="."/>
	  <xsl:text>::test</xsl:text>
	</xsl:for-each>
      </xsl:attribute>
    </target>
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
      <javac destdir="{$build}/mod/{$name}/classes/main">
	<xsl:call-template name="javac-attributes"/>
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
      <xsl:value-of select="$name"/>
      <xsl:text>::compile-main</xsl:text>
      <xsl:for-each select="depends[@module]">
	<xsl:text>,</xsl:text>
	<xsl:value-of select="@module"/>
	<xsl:text>::compile-test</xsl:text>
      </xsl:for-each>
    </xsl:attribute>
    <mkdir dir="{$build}/mod/{$name}/classes/test"/>
    <xsl:if test="compile[@test]">
      <javac destdir="{$build}/mod/{$name}/classes/test">
	<xsl:call-template name="javac-attributes"/>
	<src>
	  <pathelement location="mod/{$name}/src/test"/>
	</src>
	<classpath>
	  <pathelement location="{$build}/mod/{$name}/classes/main"/>
	  <xsl:for-each select="depends[@module]">
	    <pathelement location="{$build}/mod/{@module}/classes/test"/>
	    <pathelement location="{$build}/mod/{@module}/classes/main"/>
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
  <target name="{$name}::test">
    <xsl:attribute name="depends">
      <xsl:text>dummy</xsl:text>
      <xsl:for-each select="test">
	<xsl:text>,</xsl:text>
	<xsl:value-of select="$name"/>
	<xsl:text>::test-</xsl:text>
	<xsl:value-of select="@name"/>
      </xsl:for-each>
    </xsl:attribute>
  </target>
  <xsl:apply-templates select="test">
    <xsl:with-param name="name" select="$name"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="test[@type='validate']">
  <xsl:param name="name"/>
  <xsl:call-template name="split-test">
    <xsl:with-param name="name" select="$name"/>
    <xsl:with-param name="class">com.thaiopensource.relaxng.util.TestDriver</xsl:with-param>
    <xsl:with-param name="split">test/split.xsl</xsl:with-param>
    <xsl:with-param name="app">jing</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template match="test[@type='convert']">
  <xsl:param name="name"/>
  <xsl:call-template name="split-test">
    <xsl:with-param name="name" select="$name"/>
    <xsl:with-param name="class">com.thaiopensource.relaxng.translate.test.CompactTestDriver</xsl:with-param>
    <xsl:with-param name="split">trang/test/compactsplit.xsl</xsl:with-param>
    <xsl:with-param name="app">trang</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="split-test">
  <xsl:param name="name"/>
  <xsl:param name="class"/>
  <xsl:param name="split"/>
  <xsl:param name="app"/>
  <xsl:variable name="runtestdir">
    <xsl:value-of select="$build"/>
    <xsl:text>/mod/</xsl:text>
    <xsl:value-of select="$name"/>
    <xsl:text>/test-</xsl:text>
    <xsl:value-of select="@name"/>
  </xsl:variable>
  <xsl:variable name="srctestdir">
    <xsl:text>mod/</xsl:text>
    <xsl:value-of select="$name"/>
    <xsl:text>/test</xsl:text>
  </xsl:variable>
  <target name="{$name}::test-{@name}"
	  depends="{$name}::compile-test,{$app}::jar,{$name}::split-{@name}">
    <java classname="{$class}"
	  fork="yes"
	  failonerror="yes">
      <arg value="{$runtestdir}/out.log"/>
      <arg value="{$runtestdir}"/>
      <xsl:if test="@output">
	<arg value="{@output}"/>
      </xsl:if>
      <classpath>
	<pathelement location="{$build}/{$app}.jar"/>
	<xsl:if test="$app = 'jing'">
	  <pathelement location="lib/xercesImpl.jar"/>
	  <pathelement location="lib/saxon.jar"/>
	</xsl:if>
      </classpath>
    </java>
  </target>
  <target name="{$name}::split-{@name}"
	  depends="{$name}::uptodate-split-{@name},jing-jar"
	  unless="{$name}::uptodate-split-{@name}">
    <xsl:if test="@schema">
      <jing rngfile="{@schema}">
	<xsl:if test="substring(@schema, string-length(@schema) - 3, 4) = '.rnc'">
	  <xsl:attribute name="compactsyntax">true</xsl:attribute>
	</xsl:if>
	<fileset dir="test" includes="{@name}test.xml"/>
      </jing>
    </xsl:if>
    <delete dir="{$runtestdir}"/>
    <mkdir dir="{$runtestdir}"/>
    <xsl:if test="@transform">
      <xslt style="{$srctestdir}/{@transform}"
	    in="{$srctestdir}/{@name}test.xml"
	    out="{$runtestdir}/{@name}test.xml"/>
      <!-- XXX Could validate intermediate result against a schema -->
    </xsl:if>
    <xslt style="{$split}"
	  out="{$runtestdir}/stamp">
      <xsl:attribute name="in">
	<xsl:choose>
	  <xsl:when test="@transform">
	    <xsl:value-of select="$runtestdir"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="$srctestdir"/>
	  </xsl:otherwise>
	</xsl:choose>
	<xsl:value-of select="concat('/',@name,'test.xml')"/>
      </xsl:attribute>
      <factory name="com.icl.saxon.TransformerFactoryImpl"/>
      <param name="dir" expression="{$runtestdir}"/>
    </xslt>
  </target>
  <target name="{$name}::uptodate-split-{@name}">
    <!-- XXX include split.xsl in source files -->
    <uptodate property="{$name}::uptodate-split-{@name}"
	      targetfile="{$runtestdir}/stamp"
	      srcfile="{$srctestdir}/{@name}test.xml"/>
  </target>
</xsl:template>

<xsl:template match="test[@type='java']">
  <xsl:param name="name"/>
  <target name="{$name}::test-{@name}" depends="{$name}::compile-test">
    <mkdir dir="{$build}/mod/{$name}/test-{@name}"/>
    <java classname="{@class}"
	  fork="yes"
	  failonerror="yes">
      <xsl:copy-of select="*"/>
      <classpath>
	<pathelement location="{$build}/mod/{$name}/classes/test"/>
	<pathelement location="{$build}/mod/{$name}/classes/main"/>
	<pathelement location="mod/{$name}/src/test"/>
	<pathelement location="mod/{$name}/src/main"/>
	<xsl:for-each select="../depends[@module]">
	  <pathelement location="{$build}/mod/{@module}/classes/test"/>
	  <pathelement location="{$build}/mod/{@module}/classes/main"/>
	  <pathelement location="mod/{@module}/src/test"/>
	  <pathelement location="mod/{@module}/src/main"/>
	</xsl:for-each>
	<xsl:for-each select="../depends[@lib]">
	  <pathelement location="lib/{@lib}.jar"/>
	</xsl:for-each>
      </classpath>
    </java>
  </target>
</xsl:template>

<xsl:template name="javac-attributes">
  <xsl:attribute name="includeAntRuntime">no</xsl:attribute>
  <xsl:choose>
    <xsl:when test="java5">
      <xsl:attribute name="source">1.5</xsl:attribute>
      <xsl:attribute name="target">1.5</xsl:attribute>
    </xsl:when>
    <xsl:otherwise>
      <xsl:attribute name="source">1.3</xsl:attribute>
      <xsl:attribute name="target">1.1</xsl:attribute>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
