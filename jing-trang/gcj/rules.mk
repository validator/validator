all: $(PROG) test_$(PROG)

$(PROG): $(PROG).o $(RESOURCES)
	$(GCJ) $(GCJFLAGS) $(LDFLAGS) --main=$(MAIN) -o $@ $(PROG).o $(RESOURCES) $(LIBS)

test_$(PROG): $(PROG).o $(RESOURCES)
	$(GCJ) $(GCJFLAGS) $(LDFLAGS) --main=$(TEST_MAIN) -o $@ $(PROG).o $(RESOURCES) $(LIBS)

$(PROG).o: $(SOURCES)
	$(GCJ) $(GCJFLAGS) -c -o $@ $(SOURCES)

.resource.o:
	$(GCJ) $(GCJFLAGS) -c -o $@ --resource=`echo $@ | sed -e 's;src/;;' -e 's/.o$$//'` $<

$(RESOURCES): dirstamp

dirstamp:
	for d in $(DIRS); do test -d $$d || mkdir $$d; done
	@>$@

check: test_$(PROG)
	cd test; $(MAKE)

install: $(PROG)
	$(srcdir)/mkinstalldirs $(DESTDIR)$(bindir)
	$(srcdir)/mkinstalldirs $(DESTDIR)$(man1dir)
	$(INSTALL_PROGRAM) $(PROG) $(DESTDIR)$(bindir)/$(PROG)
	$(INSTALL_DATA) $(srcdir)/$(PROG).1 $(DESTDIR)$(man1dir)/$(PROG).1

uninstall:
	-rm -f $(DESTDIR)$(bindir)/$(PROG)
	-rm -f $(DESTDIR)$(man1dir)/$(PROG).1

clean:
	-rm -f dirstamp $(PROG) $(PROG).o $(RESOURCES)
	-rmdir `for d in $(DIRS); do echo $$d; done | sort -r`
	cd test; $(MAKE) clean

$(srcdir)/configure: configure.ac
	cd $(srcdir) && autoconf

Makefile: Makefile.in config.status
	./config.status

config.status: configure
	./config.status --recheck

distclean: clean
	-rm -f config.status config.cache config.log

.SUFFIXES: .resource
.PHONY: all check distclean clean install uninstall
