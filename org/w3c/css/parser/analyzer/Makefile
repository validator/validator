# Note that we will filter \u005c in simple \ as some IDEs have trouble with this.
CssParser.java: CssParser.jj .FORCE
	java -cp ../../../../../javacc/target/javacc.jar javacc CssParser.jj
	cat CssParser.java \
	  | sed -e 's/^}/  }/g; $$d' -e 's/\\u005c/\\/g' > C && mv -f C CssParser.java && \
	  echo '}' >> CssParser.java
.FORCE:
