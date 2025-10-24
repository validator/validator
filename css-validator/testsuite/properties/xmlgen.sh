#!/bin/sh

if [ ! -f xmlgen.sh ]; then
    echo "You must run this script in its directory"
    exit 0
fi

for level in css1 css21 css2 css3 ; do
    cat >../xml/prop-$level.xml <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!--
test markup also accepts 3 properties: warning, profile and medium
like <test profile="css2" medium="all" warning="0">
where warning means warninglevel in use (0 => normal, no => none).
-->
<testsuite>
EOF
done

for kind in positive ; do
    if [ ! -d $kind ]; then
	echo "Directory $kind not present"
	exit 0
    fi

    for property in `ls $kind | grep -v CVS` ; do
	echo "Working on $property"
	for level in `ls $kind/$property | grep -v CVS` ; do
	    echo "Level for $property is $level"
	    echo "  <type title=\"Valid_$property\">" >> ../xml/prop-$level.xml
	    for tst in `ls $kind/$property/$level | grep -v CVS` ; do
# FIXME check form the test (comment?) what is the intent and expected
# result.
		cat >> ../xml/prop-$level.xml <<EOF
    <test profile="$level" warning="no">
      <file>testsuite/properties/$kind/$property/$level/$tst</file>
      <description>Valid $property level $level</description>
      <result valid="true" />
    </test>
EOF
	    done
	    echo "  </type>" >> ../xml/prop-$level.xml	    
	done
    done
done

for level in css1 css21 css2 css3 ; do
    cat >> ../xml/prop-$level.xml <<EOF
<!--
test markup also accepts 3 properties: warning, profile and medium
like <test profile="css2" medium="all" warning="0">
where warning means warninglevel in use (0 => none).
-->
</testsuite>
EOF
done
