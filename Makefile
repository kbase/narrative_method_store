
SERVICE_PORT = $(shell perl server_scripts/get_deploy_cfg.pm NarrativeMethodStore.port)
SERVICE = narrative_method_store
SERVICE_CAPS = NarrativeMethodStore
SPEC_FILE = NarrativeMethodStore.spec
CLIENT_JAR = NarrativeMethodStoreClient.jar
WAR = NarrativeMethodStoreService.war
URL = https://kbase.us/services/narrative_method_store/rpc

#End of user defined variables

GITCOMMIT := $(shell git rev-parse --short HEAD)
TAGS := $(shell git tag --contains $(GITCOMMIT))

TOP_DIR = $(shell python -c "import os.path as p; print p.abspath('../..')")

TOP_DIR_NAME = $(shell basename $(TOP_DIR))

DIR = $(shell pwd)

ANT ?= ant

# make sure our make test works
.PHONY : test

ANT_OPTIONS =

TESTCFG ?= test.cfg

default: build-libs build-docs build-bin


ifeq ($(TOP_DIR_NAME), dev_container)

##############################
#### INSIDE DEV CONTAINER ####
include $(TOP_DIR)/tools/Makefile.common
include $(TOP_DIR)/tools/Makefile.common.rules

DEPLOY_RUNTIME ?= /kb/runtime
JAVA_HOME ?= $(DEPLOY_RUNTIME)/java
TARGET ?= /kb/deployment
SERVICE_DIR ?= $(TARGET)/services/$(SERVICE)
GLASSFISH_HOME ?= $(DEPLOY_RUNTIME)/glassfish3
SERVICE_USER ?= kbase

ASADMIN = $(GLASSFISH_HOME)/glassfish/bin/asadmin

SRC_PERL = $(wildcard scripts/*.pl)
BIN_PERL = $(addprefix $(BIN_DIR)/,$(basename $(notdir $(SRC_PERL))))

build-libs:
	$(ANT) compile $(ANT_OPTIONS)

build-bin: $(BIN_PERL)
##############################

else

###############################
#### OUTSIDE DEV CONTAINER ####
ANT_OPTIONS = -Djardir=submodules/jars/lib/jars

build-libs: submodule-init
	$(ANT) compile $(ANT_OPTIONS)

build-bin: build-nms-bin

deploy-cfg:
###############################

endif


submodule-init:
	git submodule init
	git submodule update
	#$(MAKE) -C submodules/module_builder

build-docs:
	mkdir -p docs
	$(ANT) javadoc $(ANT_OPTIONS)
	-pod2html --infile=lib/Bio/KBase/$(SERVICE_CAPS)/Client.pm --outfile=docs/$(SERVICE_CAPS).html
	rm -f pod2htm?.tmp
	cp $(SPEC_FILE) docs/.

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

build-java-client:
	$(ANT) compile_client $(ANT_OPTIONS)

compile: compile-typespec compile-typespec-java

compile-typespec-java:
	kb-sdk compile $(SPEC_FILE) \
		--out . \
		--java \
		--javasrc src \
		--javapackage us.kbase \
		--javasrv \
		--url $(URL)

compile-typespec:
	kb-sdk compile \
		--plclname Bio::KBase::$(SERVICE_CAPS)::Client \
		--pyclname biokbase.$(SERVICE).client \
		--jsclname javascript/$(SERVICE_CAPS)/Client \
		--out lib \
		--url $(URL) \
		$(SPEC_FILE)

build-classpath-list:
	$(ANT) build_classpath_list $(ANT_OPTIONS)


test: test-client test-service test-scripts

test-client:

test-service:
	test/cfg_to_runner.py $(TESTCFG) "$(ANT_OPTIONS)"
	test/run_tests.sh

test-scripts:
	



deploy: deploy-client deploy-service

deploy-client: deploy-client-libs deploy-docs deploy-perl-scripts

deploy-client-libs:
	if ["$(TARGET)" -eq ""]; \
	  then \
	  	 echo "Error makefile variable TARGET must be defined to deploy-client-libs"; \
	  	 exit 1; \
	fi;
	mkdir -p $(TARGET)/lib/
	cp dist/client/$(CLIENT_JAR) $(TARGET)/lib/
	cp -rv lib/* $(TARGET)/lib/
	echo $(GITCOMMIT) > $(TARGET)/lib/$(SERVICE).clientdist
	echo $(TAGS) >> $(TARGET)/lib/$(SERVICE).clientdist

deploy-docs:
	@if ["$(SERVICE_DIR)" -eq ""]; \
	  then \
	  	echo "Error makefile variable SERVICE_DIR must be defined to deploy-docs"; \
	  	exit 1; \
	fi;
	mkdir -p $(SERVICE_DIR)/webroot
	cp  -r docs/* $(SERVICE_DIR)/webroot/.

deploy-service: deploy-service-libs deploy-service-scripts deploy-cfg

deploy-service-libs:
	@if ["$(SERVICE_DIR)" -eq ""]; \
	  then \
	  	echo "Error makefile variable SERVICE_DIR must be defined to deploy-service-libs"; \
	  	exit 1; \
	fi;
	$(ANT) buildwar $(ANT_OPTIONS)
	mkdir -p $(SERVICE_DIR)
	cp dist/$(WAR) $(SERVICE_DIR)
	mkdir $(SERVICE_DIR)/webapps
	cp dist/$(WAR) $(SERVICE_DIR)/webapps/root.war
	echo $(GITCOMMIT) > $(SERVICE_DIR)/$(SERVICE).serverdist
	echo $(TAGS) >> $(SERVICE_DIR)/$(SERVICE).serverdist

deploy-service-scripts:
	@if ["$(TARGET)" -eq ""]; \
	  then \
	  	echo "Error makefile variable TARGET must be defined to deploy-service-scripts"; \
	  	exit 1; \
	fi;
	@if ["$(ASADMIN)" -eq ""]; \
	  then \
	  	echo "Error makefile variable ASADMIN must be defined to deploy-service-scripts"; \
	  	exit 1; \
	fi;
	cp server_scripts/glassfish_administer_service.py $(SERVICE_DIR)
	cp server_scripts/jetty.xml $(SERVICE_DIR)
	server_scripts/build_server_control_scripts.py $(SERVICE_DIR) $(WAR)\
		$(TARGET) $(JAVA_HOME) deploy.cfg $(ASADMIN) $(SERVICE_CAPS)\
		$(SERVICE_PORT)
undeploy:
	@if ["$(SERVICE_DIR)" -eq ""]; \
	  then \
	  	echo "Error makefile variable SERVICE_DIR must be defined to undeploy"; \
	  	exit 1; \
	fi;
	@if ["$(TARGET)" -eq ""]; \
	  then \
	  	echo "Error makefile variable TARGET must be defined to undeploy"; \
	  	exit 1; \
	fi;
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
