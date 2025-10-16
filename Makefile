PANDOC=pandoc

all: vnu.1

vnu.1: usage.md
	$(PANDOC) $< \
		--standalone --from markdown -f markdown-smart --to man \
		-M title:"vnu(1) vnu $$(date +"%y.%m.%d")" \
		-M date:"$$(date +"%Y-%m-%d")" \
		-o $@

clean:
	$(RM) vnu.1
