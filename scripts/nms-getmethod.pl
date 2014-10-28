#!/usr/bin/env perl
use strict;
use warnings;
use Getopt::Long;
use Data::Dumper;
use JSON;

use Bio::KBase::NarrativeMethodStore::Client;

my $DESCRIPTION = "
nms-getmethod - get full method details from the narrative_method_store
  --url = set the url (optional)
  args = method_id
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

print to_json($nms->get_method_full_info({"ids"=>[$id]}),{utf8 => 1, pretty => 1});

exit 0;