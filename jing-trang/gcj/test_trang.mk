srcdir=@srcdir@
XSLTPROC=xsltproc
FIXCR=sed -e "s/`echo x | tr x '\015'`/\\&\#xD;/g"

check: compact-check xsd-check

compact-check: compact-split/stamp
	../test_trang xsd.log compact-split xml

xsd-check: xsd-split/stamp
	../test_trang compact.log xsd-split xsd

compact-prepped.xml: $(srcdir)/compacttest.xml $(srcdir)/prep.xsl
	$(XSLTPROC) --stringparam dir compact-split $(srcdir)/prep.xsl $(srcdir)/compacttest.xml | $(FIXCR) >$@

compact-split/stamp: compact-prepped.xml $(srcdir)/exslt.xsl $(srcdir)/dir.xsl
	-mkdir `$(XSLTPROC) $(srcdir)/dir.xsl compact-prepped.xml`
	$(XSLTPROC) $(srcdir)/exslt.xsl compact-prepped.xml
	@for f in compact-split/*/xml/c.rng; do \
	  $(FIXCR) $$f >tem; mv tem $$f; \
	done
# Work around another bug in xsltproc
	@f=`grep -l ' foo="val"' compact-split/*/xml/c.rng`; \
	if [ -f "$$f" ] ; then \
	  sed -e 's/ foo=/ rng:foo=/' $$f >tem; mv tem $$f; \
	fi
	@>$@

xsd-prepped.xml: $(srcdir)/toxsdtest.xml $(srcdir)/prep.xsl
	$(XSLTPROC) -o $@ --stringparam dir xsd-split $(srcdir)/prep.xsl $(srcdir)/toxsdtest.xml

xsd-split/stamp: xsd-prepped.xml $(srcdir)/exslt.xsl $(srcdir)/dir.xsl
	-mkdir `$(XSLTPROC) $(srcdir)/dir.xsl xsd-prepped.xml`
	$(XSLTPROC) $(srcdir)/exslt.xsl xsd-prepped.xml
	@>$@

clean:
	-rm -fr compact-split xsd-split compact-prepped.xml xsd-prepped.xml

.PHONY: check xsd-check compact-check clean
