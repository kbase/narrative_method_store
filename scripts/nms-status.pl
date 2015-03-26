#!/usr/bin/env perl
use strict;
use warnings;
use Getopt::Long;
use Data::Dumper;
use JSON;

use Bio::KBase::NarrativeMethodStore::Client;

my $DESCRIPTION = "
nms-status - get the status of the narrative_method_store
  --url = set the url (optional)
";

my $help = '';
my $url = 'https://kbase.us/services/narrative_method_store';
my $opt = GetOptions (
        "help|h" => \$help,
        "url=s" => \$url
        );

if($help) {
     print $DESCRIPTION;
     exit 0;
}

if ($url eq "localhost") {
    $url = 'http://localhost:7125';
}
if ($url eq "dev") {
    $url = 'https://narrative-dev.kbase.us/services/narrative_method_store';
}
if ($url eq "ci") {
    $url = 'https://ci.kbase.us/services/narrative_method_store';
}
if ($url eq "next") {
    $url = 'https://next.kbase.us/services/narrative_method_store';
}
if ($url eq "sandbox") {
    $url = 'https://narrative-sandbox.kbase.us/services/narrative_method_store';
}

print STDERR "rpc_url=>".$url."/rpc\n";
my $nms = Bio::KBase::NarrativeMethodStore::Client->new($url."/rpc");

print to_json($nms->status(),{utf8 => 1, pretty => 1});

exit 0;
