all: javaparser wjavaparser

javaparser: main.cc gen/*.cc
	$(CXX) $(CXXFLAGS) -std=c++11 -Wall -fpermissive -o javaparser -I. main.cc gen/*.cc

wjavaparser: main.cc gen/*.cc
	$(CXX) $(CXXFLAGS) -std=c++11 -Wall -fpermissive -DWIDE_CHAR=1 -DJAVACC_CHAR_TYPE=wchar_t -o wjavaparser -I. main.cc gen/*.cc

gen/Java1.1.jj: Java1.1.jjt ../../../target/javacc.jar
	java -cp ../../../target/javacc.jar jjtree $(JAVACC_OPTS) $<

gen/*.cc: gen/Java1.1.jj ../../../target/javacc.jar
	java -cp ../../../target/javacc.jar javacc $(JAVACC_OPTS) $<

clean:
	/bin/rm -rf gen javaparser.* wjavaparser.*
