srcdir=@srcdir@
XSLTPROC=xsltproc

check: spec-check mns-check nrl-check xsd-check

spec-check: spec-split/stamp
	../test_jing spec-test.log spec-split

spec-prepped.xml: $(srcdir)/spectest.xml $(srcdir)/prep.xsl
	$(XSLTPROC) -o $@ --stringparam dir spec-split $(srcdir)/prep.xsl $(srcdir)/spectest.xml 2>/dev/null

spec-split/stamp: spec-prepped.xml   $(srcdir)/exslt.xsl
	-mkdir spec-split
	-mkdir `$(XSLTPROC) $(srcdir)/dir.xsl spec-prepped.xml 2>/dev/null`
	$(XSLTPROC) $(srcdir)/exslt.xsl spec-prepped.xml 2>/dev/null
	@>$@

mns-check: mns-split/stamp
	../test_jing mns-test.log mns-split

mns-prepped.xml: $(srcdir)/mnstest.xml $(srcdir)/prep.xsl
	$(XSLTPROC) -o $@ --stringparam dir mns-split $(srcdir)/prep.xsl $(srcdir)/mnstest.xml 2>/dev/null

mns-split/stamp: mns-prepped.xml   $(srcdir)/exslt.xsl
	-mkdir mns-split
	-mkdir `$(XSLTPROC) $(srcdir)/dir.xsl mns-prepped.xml 2>/dev/null`
	$(XSLTPROC) $(srcdir)/exslt.xsl mns-prepped.xml 2>/dev/null
	@>$@

nrl-check: nrl-split/stamp
	../test_jing nrl-test.log nrl-split

nrl-prepped.xml: $(srcdir)/nrltest.xml $(srcdir)/prep.xsl
	$(XSLTPROC) -o $@ --stringparam dir nrl-split $(srcdir)/prep.xsl $(srcdir)/nrltest.xml 2>/dev/null

nrl-split/stamp: nrl-prepped.xml   $(srcdir)/exslt.xsl
	-mkdir nrl-split
	-mkdir `$(XSLTPROC) $(srcdir)/dir.xsl nrl-prepped.xml 2>/dev/null`
	$(XSLTPROC) $(srcdir)/exslt.xsl nrl-prepped.xml 2>/dev/null
	@>$@

xsd-check: xsd-split/stamp
	../test_jing xsd-test.log xsd-split

xsd-test-suite.xml: $(srcdir)/xsdtest.xml $(srcdir)/xsdtest.xsl
	$(XSLTPROC) -o $@ $(srcdir)/xsdtest.xsl $(srcdir)/xsdtest.xml

xsd-prepped.xml: xsd-test-suite.xml $(srcdir)/prep.xsl
	$(XSLTPROC) -o $@ --stringparam dir xsd-split $(srcdir)/prep.xsl xsd-test-suite.xml 2>/dev/null

xsd-split/stamp: xsd-prepped.xml   $(srcdir)/exslt.xsl
	-mkdir xsd-split
	-mkdir `$(XSLTPROC) $(srcdir)/dir.xsl xsd-prepped.xml `
	$(XSLTPROC) $(srcdir)/exslt.xsl xsd-prepped.xml 2>/dev/null
	@>$@

clean:
	-rm -fr spec-split spec-prepped.xml
	-rm -fr xsd-split xsd-prepped.xml xsd-test-suite.xml
	-rm -fr mns-split mns-prepped.xml mns-test-suite.xml
	-rm -fr nrl-split nrl-prepped.xml nrl-test-suite.xml

.PHONY: clean check spec-check xsd-check mns-check nrl-check
