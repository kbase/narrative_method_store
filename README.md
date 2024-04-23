Narrative Method Store
======================

A KBase service for dynamically listing and accessing specifications and documentation for Narrative methods, apps, types, and categories of methods/apps.  The Narrative Interface loads available methods/apps from this service.  Specifications and documentation are pulled from a github repo named [narrative_method_specs](https://github.com/kbase/narrative_method_specs).  Updates to specifications and documentation on the configured branch are reflected by this service within some referesh rate, usually set to every 1-2 minutes.  Additional documentation on how to construct a specification can be found in the narrative_method_specs repository.

Test requirements:

* The `mongod` executable must be on the path

To run tests:

* Copy `test.cfg.example` to `test.cfg` and make any adjustments necessary
* `./gradlew test`
