all:
	(if [ ! -f ../../build/dist/vnu.jar ]; then cd ../.. && ./build/build.py jar; fi)
	cat html5.html | java -cp ../../build/dist/vnu.jar nu.validator.spec.html5.Html5SpecBuilder -
	cat w3c-html5.html | java -cp ../../build/dist/vnu.jar nu.validator.spec.html5.Html5SpecBuilder -

update:
	curl https://html.spec.whatwg.org/ -o html5.html
	curl https://w3c.github.io/html/single-page.html -o w3c-html5.html
