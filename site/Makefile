HTML2MARKDOWN=html2text
PERL=perl
PERLFLAGS=
FMT=fmt
FMTFLAGS=-80
EXPAND=expand
EXPANDFLAGS=
BUILDSTATUS=\[\!\[Build Status\]\(http://goo.gl/q852Kn\)\]\(http://goo.gl/EWWeWZ)

all: ../README.md

../README.md: ../index.html
	$(HTML2MARKDOWN) $(HTML2MARKDOWNFLAGS) $< \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/;s/Usage · Options · Web-based checking\n//' \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/;s/Standalone · Servlet · Client · Options\n//' \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/; s/(\s+\n)+/\n\n/g' \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/; s/(\n\n\n)+/\n/g' \
	    | $(FMT) $(FMTFLAGS) \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/; s/(\[[0-9]+\]) [*] /$$1\n\n  * /g' \
	    | $(FMT) $(FMTFLAGS) \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/; s/ [*]\n  /\n  * /g' \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/; s/ +(\[[0-9]+\]:)\s+/\n   $$1 /g' \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/; s/(\n\n\n)+/\n\n/g' \
	    | $(PERL) $(PERLFLAGS) -pe 's|(# The Nu Markup Checker \(v.Nu\))|$$1 $(BUILDSTATUS)|g' \
	    | $(PERL) $(PERLFLAGS) -pe 'undef $$/; s/\s+(\[1\]:)\s+/\n   $$1 /' \
	    | $(PERL) $(PERLFLAGS) -pe 's|      java -cp|        java -cp|' \
	    | $(PERL) $(PERLFLAGS) -pe 's|    export|        export|' \
	    | $(PERL) $(PERLFLAGS) -pe 's|    git clone|        git clone|' \
	    | $(PERL) $(PERLFLAGS) -pe 's|    cd validator|        cd validator|' \
	    | $(PERL) $(PERLFLAGS) -pe 's|    python|        python|' \
	    | $(EXPAND) $(EXPANDFLAGS) > $@
