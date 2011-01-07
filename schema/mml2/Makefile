BASE=http://www.w3.org/Math/RelaxNG/mathml3-restricted
FILES=mathml2-common.rnc mathml2-content.rnc mathml2-presentation.rnc mathml2-strict-content.rnc mathml2.rnc
CURL=curl
CURLFLAGS=
PERL=perl
PERLFLAGS=
PATCH=patch
PATCHFLAGS=

all: $(FILES) patch-vnu
	$(PATCH) $(PATCHFLAGS) < patch-vnu

mathml2-common.rnc:
	$(CURL) $(CURLFLAGS) $(BASE)/mathml3-restricted-common.rnc \
	  | $(PERL) $(PERLFLAGS) -pi -e 's/\r\n/\n/g' > $@

mathml2-content.rnc:
	$(CURL) $(CURLFLAGS) $(BASE)/mathml3-restricted-content.rnc \
	  | $(PERL) $(PERLFLAGS) -pi -e 's/\r\n/\n/g' > $@

mathml2-presentation.rnc:
	$(CURL) $(CURLFLAGS) $(BASE)/mathml3-restricted-presentation.rnc \
	  | $(PERL) $(PERLFLAGS) -pi -e 's/\r\n/\n/g' > $@

mathml2-strict-content.rnc:
	$(CURL) $(CURLFLAGS) $(BASE)/mathml3-restricted-strict-content.rnc \
	  | $(PERL) $(PERLFLAGS) -pi -e 's/\r\n/\n/g' > $@

mathml2.rnc:
	$(CURL) $(CURLFLAGS) $(BASE)/mathml3-restricted.rnc \
	  | $(PERL) $(PERLFLAGS) -pi -e 's/\r\n/\n/g' > $@

clean:
	$(RM) $(FILES)
