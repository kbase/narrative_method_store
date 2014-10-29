#!/usr/bin/env perl
use strict;
use warnings;
use Getopt::Long;
use Data::Dumper;
use JSON;

use Bio::KBase::NarrativeMethodStore::Client;

my $DESCRIPTION = "
nms-getapp - get full app details from the narrative_method_store
  --url = set the url (optional)
  --spec,-s = get spec instead
  args = app_id
";

my $help = '';
my $getspec = '';
my $url = 'https://kbase.us/services/narrative_method_store';
my $opt = GetOptions (
        "help|h" => \$help,
        "spec|s" => \$getspec,
        "url=s" => \$url
        );

if($help) {
     print $DESCRIPTION;
     exit 0;
}

if ($url eq "dev") {
    $url = 'http://dev19.berkeley.kbase.us/narrative_method_store'
}

my $n_args = $#ARGV+1;

print STDERR "rpc_url=>".$url."/rpc\n";
my $nms = Bio::KBase::NarrativeMethodStore::Client->new($url."/rpc");

my $id = '';
if ($n_args==1) {
    $id = $ARGV[0];
} else {
    print STDERR "incorrect number of args, expecting 1.\n";
    exit 1;
}
if ($getspec) {
     print to_json($nms->get_app_spec({"ids"=>[$id]}),{utf8 => 1, pretty => 1});
} else {
     print to_json($nms->get_app_full_info({"ids"=>[$id]}),{utf8 => 1, pretty => 1});
}
exit 0;