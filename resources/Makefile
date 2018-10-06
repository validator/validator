.PHONY: .FORCE

existing-rel-values: .FORCE
	curl -O http://microformats.org/wiki/existing-rel-values \
	  && curl http://microformats.org/wiki/existing-rel-values \
	  | grep -v www.google-analytics.com > $@
