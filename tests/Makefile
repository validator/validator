PYTHON ?= python3
PYTHONFLAGS ?=
CURL ?= curl
CURLFLAGS ?=
JAVA ?= java
JAVAFLAGS ?=
VNUJAR ?= vnu.jar

.PHONY: .FORCE clean

all: messages.json

clean:
	$(RM) vnu.jar

$(VNUJAR):
	cd $(CURDIR) && $(CURL) $(CURLFLAGS) -O -L \
		https://github.com/validator/validator/releases/download/latest/vnu.jar

messages.json: .FORCE $(VNUJAR)
	$(JAVA) $(JAVAFLAGS) -cp $(VNUJAR) nu.validator.client.TestRunner \
		--ignore=html-its --write-messages $@
	$(PYTHON) $(PYTHONFLAGS) -mjson.tool --sort-keys $@ > $@.tmp
	mv $@.tmp $@

test: .FORCE $(VNUJAR)
	$(JAVA) $(JAVAFLAGS) -cp $(VNUJAR) nu.validator.client.TestRunner \
