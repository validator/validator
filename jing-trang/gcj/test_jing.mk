srcdir=@srcdir@
XSLTPROC=xsltproc

check: split/stamp
	../test_jing test.log split

prepped.xml: $(srcdir)/spectest.xml $(srcdir)/prep.xsl
	$(XSLTPROC) -o $@ --stringparam dir split $(srcdir)/prep.xsl $(srcdir)/spectest.xml 2>/dev/null

split/stamp: prepped.xml   $(srcdir)/exslt.xsl
	-mkdir split
	-mkdir `$(XSLTPROC) $(srcdir)/dir.xsl prepped.xml 2>/dev/null`
	$(XSLTPROC) $(srcdir)/exslt.xsl prepped.xml 2>/dev/null
	@>$@

clean:
	-rm -fr split prepped.xml
