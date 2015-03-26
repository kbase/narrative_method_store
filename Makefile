#port is now set in deploy.cfg
SERVICE_PORT = $(shell perl server_scripts/get_deploy_cfg.pm NarrativeMethodStore.port)
SERVICE = narrative_method_store
SERVICE_CAPS = NarrativeMethodStore
SPEC_FILE = NarrativeMethodStore.spec
CLIENT_JAR = NarrativeMethodStoreClient.jar
WAR = NarrativeMethodStoreService.war
URL = https://kbase.us/services/narrative_method_store/rpc
DEFAULT_SCRIPT_URL = $(URL)
DEV_SCRIPT_URL = http://dev19.berkeley.kbase.us:$(SERVICE_PORT)/rpc

#End of user defined variables

GITCOMMIT := $(shell git rev-parse --short HEAD)
TAGS := $(shell git tag --contains $(GITCOMMIT))

TOP_DIR = $(shell python -c "import os.path as p; print p.abspath('../..')")

TOP_DIR_NAME = $(shell basename $(TOP_DIR))

DIR = $(shell pwd)

ifeq ($(TOP_DIR_NAME), dev_container)
ifeq ($(KB_TOP),) #only include if we've also sourced the user-env.sh
default:
    $(error KB_TOP not set - did you forget to source user-env.sh?)
else
include $(TOP_DIR)/tools/Makefile.common
default: build-bin build-docs build-bin
endif
else
default:
    $(error Only the build-nms-bin target is supported outside of the dev_container)
endif

DEPLOY_RUNTIME ?= /kb/runtime
JAVA_HOME ?= $(DEPLOY_RUNTIME)/java
TARGET ?= /kb/deployment
SERVICE_DIR ?= $(TARGET)/services/$(SERVICE)
GLASSFISH_HOME ?= $(DEPLOY_RUNTIME)/glassfish3
SERVICE_USER ?= kbase

ASADMIN = $(GLASSFISH_HOME)/glassfish/bin/asadmin

ANT = ant


SRC_PERL = $(wildcard scripts/*.pl)
BIN_PERL = $(addprefix $(BIN_DIR)/,$(basename $(notdir $(SRC_PERL))))

# make sure our make test works
.PHONY : test


# fake deploy-cfg target for when this is run outside the dev_container
deploy-cfg:

SCRIPTBINDESTINATION = $(DIR)/bin
ifeq ($(TOP_DIR_NAME), dev_container)
include $(TOP_DIR)/tools/Makefile.common.rules
SCRIPTBINDESTINATION = $(TOP_DIR)/bin
endif

build-libs:
	$(ANT) compile

build-bin: $(BIN_PERL)

# make the nms-* scripts within the repo outside of the KBase Runtime
build-nms-bin:
	mkdir -p bin
	cp -r scripts/simpledeploy/common/Bio lib/.
	./scripts/simpledeploy/wrap_nms_scripts.sh $(DIR)/scripts/nms-version.pl bin/nms-version $(DIR)/lib
	./scripts/simpledeploy/wrap_nms_scripts.sh $(DIR)/scripts/nms-status.pl bin/nms-status $(DIR)/lib
	./scripts/simpledeploy/wrap_nms_scripts.sh $(DIR)/scripts/nms-validate.pl bin/nms-validate $(DIR)/lib
	./scripts/simpledeploy/wrap_nms_scripts.sh $(DIR)/scripts/nms-listapps.pl bin/nms-listapps $(DIR)/lib
	./scripts/simpledeploy/wrap_nms_scripts.sh $(DIR)/scripts/nms-listmethods.pl bin/nms-listmethods $(DIR)/lib
	./scripts/simpledeploy/wrap_nms_scripts.sh $(DIR)/scripts/nms-getapp.pl bin/nms-getapp $(DIR)/lib
	./scripts/simpledeploy/wrap_nms_scripts.sh $(DIR)/scripts/nms-getmethod.pl bin/nms-getmethod $(DIR)/lib
	echo "export PATH=$(DIR)/bin:\$$PATH" > bin/nms-env.sh

build-docs: build-libs
	@#$(ANT) javadoc
	pod2html --infile=lib/Bio/KBase/$(SERVICE_CAPS)/Client.pm --outfile=docs/$(SERVICE_CAPS).html
	rm -f pod2htm?.tmp
	cp $(SPEC_FILE) docs/.

compile: compile-typespec compile-typespec-java

compile-java-client:
	@# $(ANT) compile_client

compile-typespec-java:
	gen_java_types -S -o . -u $(URL) $(SPEC_FILE)
	rm -f lib/*.jar

compile-typespec:
	mkdir -p lib/biokbase/$(SERVICE)
	touch lib/biokbase/__init__.py # do not include code in biokbase/__init__.py
	touch lib/biokbase/$(SERVICE)/__init__.py 
	mkdir -p lib/javascript/$(SERVICE)
	compile_typespec \
		--client Bio::KBase::$(SERVICE_CAPS)::Client \
		--py biokbase.$(SERVICE).client \
		--js javascript/$(SERVICE_CAPS)/Client \
		--url $(URL) \
		$(SPEC_FILE) lib
	rm -f lib/*Server.p* #should be no perl/py server files in our lib dir
	rm -f lib/*Impl.p*   #should be no perl/py impl files in our lib dir


test: test-client test-service test-scripts

test-client: test-service
	@# $(ANT) test_client_import

test-service:
	test/cfg_to_runner.py $(TESTCFG)
	test/run_tests.sh

test-scripts:
	

deploy: deploy-client deploy-service

deploy-client: deploy-client-libs deploy-docs deploy-perl-scripts

deploy-client-libs:
	mkdir -p $(TARGET)/lib/
	cp dist/client/$(CLIENT_JAR) $(TARGET)/lib/
	cp -rv lib/* $(TARGET)/lib/
	echo $(GITCOMMIT) > $(TARGET)/lib/$(SERVICE).clientdist
	echo $(TAGS) >> $(TARGET)/lib/$(SERVICE).clientdist

deploy-docs:
	mkdir -p $(SERVICE_DIR)/webroot
	cp  -r docs/* $(SERVICE_DIR)/webroot/.

deploy-service: deploy-service-libs deploy-service-scripts deploy-cfg

deploy-service-libs:
	$(ANT) buildwar
	mkdir -p $(SERVICE_DIR)
	cp dist/$(WAR) $(SERVICE_DIR)
	echo $(GITCOMMIT) > $(SERVICE_DIR)/$(SERVICE).serverdist
	echo $(TAGS) >> $(SERVICE_DIR)/$(SERVICE).serverdist

deploy-service-scripts:
	cp server_scripts/glassfish_administer_service.py $(SERVICE_DIR)
	server_scripts/build_server_control_scripts.py $(SERVICE_DIR) $(WAR)\
		$(TARGET) $(JAVA_HOME) deploy.cfg $(ASADMIN) $(SERVICE_CAPS)\
		$(SERVICE_PORT)

deploy-upstart:
	echo "# $(SERVICE) service" > /etc/init/$(SERVICE).conf
	echo "# NOTE: stop $(SERVICE) does not work" >> /etc/init/$(SERVICE).conf
	echo "# Use the standard stop_service script as the $(SERVICE_USER) user" >> /etc/init/$(SERVICE).conf
	echo "#" >> /etc/init/$(SERVICE).conf
	echo "# Make sure to set up the $(SERVICE_USER) user account" >> /etc/init/$(SERVICE).conf
	echo "# shell> groupadd kbase" >> /etc/init/$(SERVICE).conf
	echo "# shell> useradd -r -g $(SERVICE_USER) $(SERVICE_USER)" >> /etc/init/$(SERVICE).conf
	echo "#" >> /etc/init/$(SERVICE).conf
	echo "start on runlevel [23]" >> /etc/init/$(SERVICE).conf 
	echo "stop on runlevel [!23]" >> /etc/init/$(SERVICE).conf 
	echo "pre-start exec chown -R $(SERVICE_USER) $(TARGET)/services/$(SERVICE)" >> /etc/init/$(SERVICE).conf 
	echo "exec su kbase -c '$(TARGET)/services/$(SERVICE)/start_service'" >> /etc/init/$(SERVICE).conf 

undeploy:
	-rm -rf $(SERVICE_DIR)
	-rm -rfv $(TARGET)/lib/Bio/KBase/$(SERVICE)
	-rm -rfv $(TARGET)/lib/biokbase/$(SERVICE)
	-rm -rfv $(TARGET)/lib/javascript/$(SERVICE) 
	-rm -rfv $(TARGET)/lib/$(CLIENT_JAR)

clean:
	$(ANT) clean
	-rm -rf docs/javadoc
	-rm -f docs/$(SERVICE_CAPS).html
	-rm -f docs/$(SPEC_FILE)
	-rm -f lib/Bio/KBase/Exceptions.pm
	-rm -f test/run_tests.sh
	-rm -rf bin
	@#TODO remove lib once files are generated on the fly
