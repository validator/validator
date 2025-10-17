PANDOC=pandoc
SED=sed
AWK=awk
DATE=date

VNU_VERSION ?= $(shell $(DATE) +"%y.%m.%d")
MANPAGE_LAST_UPDATE_DATE ?= $(shell $(DATE) +"%Y-%m-%d")

.PHONY: all manpages clean

all: manpages vnu-usage.md

manpages: vnu.1 vnu-client.1 vnu-server.1

vnu.1: vnu.1.md
	$(PANDOC) --standalone --from markdown -f markdown-smart --to man $< \
		-M title:"vnu(1) vnu $(VNU_VERSION)" \
		-M date:"$(MANPAGE_LAST_UPDATE_DATE)" \
		| $(AWK) '!f && /.IP/ {f=1; next} 1' \
		> $@

vnu-usage.md: vnu.1.md
	$(SED) -n "1,/^# OPTIONS$$/{ p; d; }" $< > $@
	echo '\nFor details on all options and usage, try the "--help" option or see:\n\n  https://validator.github.io/validator/' >> $@

vnu-client.1: vnu-client.1.md
	$(PANDOC) --standalone --from markdown -f markdown-smart --to man \
		-M title:"vnu-client(1) vnu-client $(VNU_VERSION)" \
		-M date:"$(MANPAGE_LAST_UPDATE_DATE)" $< \
		| $(AWK) '!f && /.IP/ {f=1; next} 1' \
		> $@

vnu-server.1: vnu-server.1.md
	$(PANDOC) --standalone --from markdown -f markdown-smart --to man \
		-M title:"vnu-server(1) vnu-server $(VNU_VERSION)" \
		-M date:"$(MANPAGE_LAST_UPDATE_DATE)" $< \
		| $(AWK) '!f && /.IP/ {f=1; next} 1' \
		> $@

clean:
	$(RM) vnu.1 vnu-client.1 vnu-server.1 vnu-usage.md
