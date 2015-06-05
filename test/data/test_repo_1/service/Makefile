# Client name with package: GenomeFeatureComparatorClient
# Client file path:         GenomeFeatureComparatorClient.py
# Impl name with package:   GenomeFeatureComparatorImpl
# Impl file path:           GenomeFeatureComparatorImpl.py
# Server name with package: GenomeFeatureComparatorServer
# Server file path:         GenomeFeatureComparatorServer.py
# Asynchronous support:     yes

TARGET ?= /kb/deployment
DIR = $(shell pwd)
BIN = $(TARGET)/bin
SERVICE_NAME = GenomeFeatureComparator
ASYNC_JOB_SCRIPT_NAME = run_GenomeFeatureComparator_async_job.sh

all:
	@echo "Nothing to compile"

deploy: deploy-client deploy-service deploy-scripts

undeploy:
	@echo "Nothing to undeploy"

deploy-client:
	@echo "No deployment for client"

deploy-service:
	@echo "No deployment for service"

deploy-scripts:
	mkdir -p $(TARGET)/services/$(SERVICE_NAME)
	cp -a . $(TARGET)/services/$(SERVICE_NAME)/
	# cp -a $(TARGET)/lib/biokbase $(TARGET)/services/$(SERVICE_NAME)/	
	mkdir -p $(BIN)
	echo '#!/bin/bash' > $(BIN)/$(ASYNC_JOB_SCRIPT_NAME)
	echo 'export KB_TOP=$(TARGET)' >> $(BIN)/$(ASYNC_JOB_SCRIPT_NAME)
	echo 'export KB_RUNTIME=/kb/runtime' >> $(BIN)/$(ASYNC_JOB_SCRIPT_NAME)
	echo 'export PATH=$$KB_RUNTIME/bin:$$KB_TOP/bin:$$PATH' >> $(BIN)/$(ASYNC_JOB_SCRIPT_NAME)
	echo 'export PYTHONPATH=$$KB_TOP/lib:$$PYTHONPATH' >> $(BIN)/$(ASYNC_JOB_SCRIPT_NAME)
	echo 'cd $(TARGET)/services/$(SERVICE_NAME)' >> $(BIN)/$(ASYNC_JOB_SCRIPT_NAME)
	echo 'python GenomeFeatureComparatorServer.py $$1 $$2 $$3' >> $(BIN)/$(ASYNC_JOB_SCRIPT_NAME)

test: test-client test-service test-scripts

test-client:
	@echo "No tests for client"

test-service:
	@echo "No tests for service"

test-scripts:
	@echo "No tests for scripts"

clean:
	@echo "No clean is necessary"
