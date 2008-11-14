<stylesheet version="1.0"
	    xmlns="http://www.w3.org/1999/XSL/Transform"
	    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	    xmlns:xsltc="http://www.thaiopensource.com/ns/xsltc">

<template match="*">
  <copy>
    <copy-of select="@*"/>
    <apply-templates select="node()"/>
  </copy>
</template>

<template match="xsl:*[@xsltc:remove]"/>

</stylesheet>
