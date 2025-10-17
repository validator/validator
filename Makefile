PANDOC=pandoc
SED=sed
AWK=awk
DATE=date

VNU_VERSION ?= $(shell $(DATE) +"%y.%m.%d")
MANPAGE_LAST_UPDATE_DATE ?= $(shell $(DATE) +"%Y-%m-%d")

all: vnu.1

vnu.1: docs/vnu-usage.md
	{ $(SED) '/^## OPTIONS/q' $< ; \
		$(SED) -n '/^## Options/,/^## Web-based checking/{/^## Options/d;/^## Web-based checking/d;p;}' README.md | \
		$(SED) 's/####/####    /'; } \
		| $(PANDOC) --standalone --from markdown -f markdown-smart --to man \
		-M title:"vnu(1) vnu $(VNU_VERSION)" \
		-M date:"$(MANPAGE_LAST_UPDATE_DATE)" \
		| $(AWK) '!f && /.IP/ {f=1; next} 1' \
		> $@

clean:
	$(RM) vnu.1
