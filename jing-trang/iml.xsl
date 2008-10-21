<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output indent="yes"
	    encoding="utf-8"/>

<xsl:param name="name"/>

<xsl:template match="module">
  <module relativePaths="true" type="JAVA_MODULE" version="4">
    <component name="NewModuleRootManager" LANGUAGE_LEVEL="JDK_1_5"
	       inherit-compiler-output="false">
      <exclude-output />
      <output url="file://$MODULE_DIR$/../../build/{$name}/classes/main"/>
      <output-test url="file://$MODULE_DIR$/../../build/{$name}/classes/test"/>
      <xsl:if test="ant/@precompile">
	<content url="file://$MODULE_DIR$/../../build/{$name}/gensrc">
	  <sourceFolder url="file://$MODULE_DIR$/../../build/{$name}/gensrc/main"
			isTestSource="false" />
	</content>
      </xsl:if>
      <content url="file://$MODULE_DIR$/src/">
	<sourceFolder url="file://$MODULE_DIR$/src/main" isTestSource="false" />
	<xsl:if test="compile[@test]">
	  <sourceFolder url="file://$MODULE_DIR$/src/test" isTestSource="true" />
	</xsl:if>
      </content>
      <orderEntry type="inheritedJdk" />
      <orderEntry type="sourceFolder" forTests="false" />
      <xsl:apply-templates select="depends"/>
      <orderEntryProperties />
    </component>
  </module>
</xsl:template>

<xsl:template match="depends[@module]">
  <orderEntry type="module" module-name="{@module}" />
</xsl:template>

<xsl:template match="depends[@lib]">
  <orderEntry type="library" name="{@lib}" level="project"/>
</xsl:template>

</xsl:stylesheet>
