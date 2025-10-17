PANDOC=pandoc
SED=sed
AWK=awk
DATE=date

VNU_VERSION ?= $(shell $(DATE) +"%y.%m.%d")
MANPAGE_LAST_UPDATE_DATE ?= $(shell $(DATE) +"%Y-%m-%d")

MANPAGES = $(patsubst %.md,%,$(wildcard *.1.md))

.PHONY: all manpages clean

all: manpages

manpages: $(MANPAGES)

%.1: %.1.md
	$(PANDOC) --standalone --from markdown -f markdown-smart --to man \
		-M title:"$*(1) $* $(VNU_VERSION)" \
		-M date:"$(MANPAGE_LAST_UPDATE_DATE)" $< \
		| $(AWK) '!f && /.IP/ {f=1; next} 1' \
		> $@

clean:
	$(RM) $(MANPAGES)
