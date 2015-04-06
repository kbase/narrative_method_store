Narrative Method Store CLI Deployment Guide
------------------

The Narrative Method Store is packaged with a Command Line Interface (CLI) that is useful for checking the status of a Narrative Method Store deployment (e.g. version, last update, which narrative_method_specs repository is being used), retrieving specifications from the method store, and validating local specs.

The CLI can be deployed in either a standard KBase runtime, or locally within this repository directory.


### KBase Runtime Deployment

If you have a KBase Runtime set up (e.g. [dev_container](https://github.com/kbase/dev_container), deploying the CLI is relatively easy.  Checkout this repo into the dev_container/module directory, with dependencies (jars, kbapi_common).  Note that only kbapi_common is required for the CLI.  Then, to build the java libraries and CLI, and add the CLI to dev_containter/bin, change to this repo's root directory and type:

    make

To build and only deploy the CLI:

    make build-bin

To deploy the scripts to a Deployment destination

    make deploy-client TARGET=/kb/deployment

where you can set TARGET to the deployment destination.  Note that for the deployed CLI to work as expected, you need to deploy kbapi_common as well.


### Local Deployment

If you do not have a KBase Runtime, you can still build and run the narrative_method_store CLI.  You will need to have installed perl, which should be standard on Mac and Linux, [git](http://git-scm.com/book/en/v2/Getting-Started-Installing-Git) to checkout the repo, and `make` to build the CLI.  If you're on Linux, you probably know how to get these things.  If you're on mac, you'll need to download the XCode command line tools- [this may help](http://railsapps.github.io/xcode-command-line-tools.html). 

Open a terminal and checkout the code:

    git clone https://github.com/kbase/narrative_method_store.git
    cd narrative_method_store

Then run make:

    make build-nms-bin 

This will create a directory 'narrative_method_store/bin' with the CLI methods and a simple script to add that directory to your path, which you can call now:

    source bin/nms-env.sh
    
Everytime you start a new terminal, you'll have to rerun the line above to add the CLI to your path.  To avoid this, add the command above to your .bashrc or .bash_profile file.

Now the commands should be available anywhere, but they may not work unless you have two perl packages already installed.  You can install these packages like so:

    cpan install JSON::RPC::Client
    cpan install Exception::Class

You may have to configure some cpan options if this is your first time running cpan.  The default options should be fine (answer yes to all).  If you run into problems installing these modules, it may be because you do not have the right permissions to install into your system perl.  If this is the case, install them as:

    sudo cpan install JSON::RPC::Client
    sudo cpan install Exception::Class

Test to make sure the CLI is installed successfully.  Running:

    nms-version

should return something like this if it works:

	v0.2.5 - https://kbase.us/services/narrative_method_store

All the nms commands accept the -h option for printing help.  For instance:

    nms-version -h

    nms-version - get the version of the narrative_method_store service
      --url = set the url (optional)

If nothing worked, email Michael Sneddon - mwsneddon@lbl.gov

