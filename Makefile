PANDOC=pandoc
SED=sed
AWK=awk
DATE=date

all: vnu.1

vnu.1: usage.md
	{ $(SED) '/^## OPTIONS/q' $< ; \
		$(SED) -n '/^## Options/,/^## Web-based checking/{/^## Options/d;/^## Web-based checking/d;p;}' README.md | \
		$(SED) 's/####/####    /'; } \
		| $(PANDOC) --standalone --from markdown -f markdown-smart --to man \
		-M title:"vnu(1) vnu $$(date +"%y.%m.%d")" \
		-M date:"$$($(DATE) +"%Y-%m-%d")" \
		| $(AWK) '!f && /.IP/ {f=1; next} 1' \
		> $@

clean:
	$(RM) vnu.1
