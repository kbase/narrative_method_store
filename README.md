**master:** [![Build Status](https://travis-ci.org/kbase/narrative_method_store.svg?branch=master)](https://travis-ci.org/kbase/narrative_method_store) | **staging:** [![Build Status](https://travis-ci.org/kbase/narrative_method_store.svg?branch=staging)](https://travis-ci.org/kbase/narrative_method_store) | **develop:** [![Build Status](https://travis-ci.org/kbase/narrative_method_store.svg?branch=develop)](https://travis-ci.org/kbase/narrative_method_store)

Narrative Method Store
======================

A KBase service for dynamically listing and accessing specifications and documentation for Narrative methods, apps, types, and categories of methods/apps.  The Narrative Interface loads available methods/apps from this service.  Specifications and documentation are pulled from a github repo named [narrative_method_specs](https://github.com/kbase/narrative_method_specs).  Updates to specifications and documentation on the configured branch are reflected by this service within some referesh rate, usually set to every 1-2 minutes.  Additional documentation on how to construct a specification can be found in the narrative_method_specs repository.

The Narrative Method Store has a CLI for developers and documenters to check the status of the method store, list and fetch specifications and documentation, and validate local changes to specifications/documentation.  Instructions on deploying the CLI can be found at [docs/CLI_Deployment_Guide.md](docs/CLI_Deployment_Guide.md).

The Narrative Method Store can be deployed within a standard KBase runtime environment with the standard KBase deployment process. Briefly, from within the dev_container, clone this repo into the modules directory.  Rebuild any environment variables by running the dev_container bootstrap script and sourcing the user-env.sh file.  Within this repo directory, run `make`, optionally `make test`, and finally `make deploy TARGET=[deployment_directory]`.

Test requirements:

* The `mongod` executable must be on the path

To run tests:

* Copy `test.cfg.example` to `test.cfg` and make any adjustments necessary
* `ant test`
