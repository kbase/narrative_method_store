#!/usr/bin/env perl
use strict;
use warnings;
use Getopt::Long;
use File::Slurp;
use JSON;
use File::Spec;
use File::Basename;
use File::Path qw(make_path);
use Cwd 'abs_path';

use Bio::KBase::NarrativeMethodStore::Client;

my $DESCRIPTION = "
nms-validate - validate local method, app, & type specifcations

  If no options are set, this will try to determine if you are in a narrative_method_specs or
  narrative_method_specs_sandbox repository.  If so, it will validate things based on the
  current directory- if you are in the root, everything will be validated.  If you are in
  methods or apps, for instance, only those will be validated.  If you are in a specific method
  or app, only that one will be validated.  All command line flags are optional.

    --url = set the url, defaults to production narrative_method_store

    --nms-path [path] = path to the narrative_method_specs or narrative_method_specs_sandbox local repo

    --method [id] = provide the method id to validate
    --app [id] = provide the app id to validate
    --type [id] = provide the type id to validate

    --out [dir] = if set, the output parse of the validation with the parsed information will be
                  saved to a folder in the specified directory

    --verbose = show warnings and other debug messages 

    --help = show this help message
";

my $help = '';
my $url = 'https://kbase.us/services/narrative_method_store';

my $nmspath;
my $method;
my $app;
my $category;
my $type;

my $outdir;
my $verbose=0;

my $opt = GetOptions (
        "help|h" => \$help,
        "nms-path|p=s" => \$nmspath,
        "method|m=s" => \$method,
        "app|a=s" => \$app,
        "category|c=s" => \$category,
        "type|t=s" => \$type,
        "out|o=s" => \$outdir,
        "verbose|v" => \$verbose,
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

my ($root, $targets) = whatShouldIDo($nmspath, $method, $app, $type);

print STDERR "rpc_url=>".$url."/rpc\n";
my $nms = Bio::KBase::NarrativeMethodStore::Client->new($url."/rpc");

if($outdir) {
	my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
	$year += 1900;
	$outdir .= "/validation-dump-"."$year-$mon-$mday-$hour-$min-$sec";
}
validateTargets($nms, $root, $targets, $verbose, $outdir);

exit 0;



sub validateTargets {
	my ($nms, $root, $targets, $verbose, $saveParse) = @_;

 	my $stats = {
 		methods=>{total=>0,failed=>0},
 		apps=>{total=>0,failed=>0},
 		types=>{total=>0,failed=>0}
 		#categories=>{total=>0,failed=>0}
 	};

	my $totalErrors = 0; my $totalWarnings = 0;
	foreach my $currType (keys %{$targets}) {
		for(my $k=0; $k<@{$targets->{$currType}}; $k++) {
			my ($localErrors, $localWarnings) = 
					validate($nms,$root,$currType,$targets->{$currType}->[$k],$verbose,$saveParse);
			$totalErrors += $localErrors;
			$totalWarnings += $localWarnings;
			$stats->{$currType}->{'total'}++;
			if($localErrors>0) {
				$stats->{$currType}->{'failed'}++;
			}
		}
	}


	print "\nValidation Summary:\n";
	print "    $totalErrors Error(s), $totalWarnings Warning(s)\n\n";
	printStat($stats, "methods", "Methods");
	printStat($stats, "apps", "Apps");
	printStat($stats, "types", "Types");
	#printStat($stats, "categories", "Categories");

	print "\n";

	if(!$verbose && $totalWarnings>0) {
		print "    Run with --verbose to view warnings.\n\n";
	}
};

sub printStat {
	my ($stats, $typeKey, $typeStr) = @_;
	if($stats->{$typeKey}->{'total'}>0) {
		if($stats->{$typeKey}->{'failed'}>0) {
			print "    ".$stats->{$typeKey}->{'failed'}." of ".$stats->{$typeKey}->{'total'}." $typeStr are invalid.\n";
		} else {
			print "    All ".$stats->{$typeKey}->{'total'}." $typeStr are valid.\n";
		}
	}
};



sub validate {
	my ($nms, $root, $type, $id, $verbose, $saveParse) = @_;

	my $path = $root."/".$type."/".$id;
	my $errors = 0; my $warnings = 0;
	if($type eq 'methods') {
		if($verbose) { print "[Method:$id]: validating, path=$path\n"; }
		my $result = validateMethod($nms, $id, $path);
		$errors = $errors + scalar @{$result->{'errors'}};
		$warnings = $warnings + scalar @{$result->{'warnings'}};
		printResult($result, "[Method:$id]:", $path, $verbose);
		if($saveParse) {
			if($verbose) { print "[Method:$id]: saving result in $saveParse/.\n"; }
			saveParse($result,$saveParse,"Method",$type,$id);
		}
	}
	elsif($type eq 'apps') {
		if($verbose) { print "[App:$id]: validating, path=$path\n"; }
		my $result = validateApp($nms, $id, $path);
		$errors = $errors + scalar @{$result->{'errors'}};
		$warnings = $warnings + scalar @{$result->{'warnings'}};
		printResult($result, "[App:$id]:", $path, $verbose);
		if($saveParse) {
			if($verbose) { print "[App:$id]: saving result in $saveParse/.\n"; }
			saveParse($result,$saveParse,"App",$type,$id);
		}
	}
	elsif($type eq 'types') {
		if($verbose) { print "[Type:$id]: validating, path=$path\n"; }
		my $result = validateType($nms, $id, $path);
		$errors = $errors + scalar @{$result->{'errors'}};
		$warnings = $warnings + scalar @{$result->{'warnings'}};
		printResult($result, "[Type:$id]:", $path, $verbose);
		if($saveParse) {
			if($verbose) { print "[Type:$id]: saving result in $saveParse/.\n"; }
			saveParse($result,$saveParse,"Type",$type,$id);
		}
	}
	#if($type eq 'categories') {
	#	if($verbose) { print "[Category:$id]: validating, path=$path\n"; }
	#	my $result = validateCategory($nms, $id, $path);
	#	$errors = $errors + scalar @{$result->{'errors'}};
	#	$warnings = $warnings + scalar @{$result->{'warnings'}};
	#	printResult($result, "[Category:$id]:", $path, $verbose);
	#}

	return ($errors,$warnings);
};

sub validateMethod {
	my ($nms, $id, $path) = @_;

	if (!(-d $path)) { return {errors=>["Method path '$path' is not a directory."]}; }
	if (!(-e $path.'/spec.json')) { return {errors=>["Method at '$path' does not have a spec.json file."]}; }
	if (!(-e $path.'/display.yaml')) { return {errors=>["Method at '$path' does not have a spec.json file."]}; }

	my $spec = read_file($path.'/spec.json');
	my $display = read_file($path.'/display.yaml');

	# pull all files from this directory
	my $extraFiles = gatherExtraFiles($path);
	#use Data::Dumper; print Dumper($extraFiles);
	my $result = $nms->validate_method({id=>$id, spec_json=>$spec, display_yaml=>$display, extra_files=>$extraFiles});
	if (!(-d $path.'/img')) { 
		my $mssg = "Method at '$path' does not have an 'img' directory.";
		if(defined $result->{'warnings'}) {
			push @{$result->{'warnings'}}, $mssg;
		} else {
			$result->{'warnings'} = [$mssg];
		}
	}
	return $result;
};

sub validateApp {
	my ($nms, $id, $path) = @_;

	if (!(-d $path)) { return {errors=>["App path '$path' is not a directory."]}; }
	if (!(-e $path.'/spec.json')) { return {errors=>["App at '$path' does not have a spec.json file."]}; }
	if (!(-e $path.'/display.yaml')) { return {errors=>["App at '$path' does not have a spec.json file."]}; }

	my $spec = read_file($path.'/spec.json');
	my $display = read_file($path.'/display.yaml');

	# pull all files from this directory
	my $extraFiles = gatherExtraFiles($path);

	my $result = $nms->validate_app({id=>$id, spec_json=>$spec, display_yaml=>$display, extra_files=>$extraFiles});
	if (!(-d $path.'/img')) { 
		my $mssg = "App at '$path' does not have an 'img' directory.";
		if(defined $result->{'warnings'}) {
			push @{$result->{'warnings'}}, $mssg;
		} else {
			$result->{'warnings'} = [$mssg];
		}
	}
	return $result;
};

sub validateType {
	my ($nms, $id, $path) = @_;

	if (!(-d $path)) { return {errors=>["Type path '$path' is not a directory."]}; }
	if (!(-e $path.'/spec.json')) { return {errors=>["Type at '$path' does not have a spec.json file."]}; }
	if (!(-e $path.'/display.yaml')) { return {errors=>["Type at '$path' does not have a spec.json file."]}; }

	my $spec = read_file($path.'/spec.json');
	my $display = read_file($path.'/display.yaml');

	# pull all files from this directory
	my $extraFiles = gatherExtraFiles($path);

	my $result = $nms->validate_type({id=>$id, spec_json=>$spec, display_yaml=>$display, extra_files=>$extraFiles});

	return $result;
};

sub gatherExtraFiles {
	my ($path) = @_;
	my $extraFiles = {};

	opendir(my $dh, $path) or return -1;
	my %foundDirs;
    while(readdir $dh) {
    	my $name = $_;
    	next if(-d $path.'/'.$name);
    	next if($name eq 'spec.json');
    	next if($name eq 'display.yaml');
    	if(-e $path.'/'.$name) {
    		$extraFiles->{$name}=read_file($path.'/'.$name);
    	}
    }
    closedir $dh;
    return $extraFiles;
};

sub printResult {
	my ($result, $prefix, $path, $verbose) = @_;

    my $spacer = ''; my $spaces = 70;
    for(my $s=length($prefix); $s<$spaces; $s++) { $spacer .= ' '; }
	if($result->{'is_valid'}){
		print $prefix.$spacer." valid ";

		if(defined $result->{'warnings'} && @{$result->{'warnings'}} >0) {
			if($verbose) {
				print "\n    ... but there were ".@{$result->{'warnings'}}." warning(s):\n";
				for(my $k=0; $k<(@{$result->{'warnings'}}); $k++) {
					my $w = $result->{'warnings'}[$k];
					$w =~ s/^/    [Warning $k]: /mg;
					print $w."\n";
				}
				print "\n";
			} else {
				print " (with ".@{$result->{'warnings'}}." warning(s))\n"; 
			}
		} else {
			print "\n";
		}
	} else {
		print "$prefix is not valid \n"; 
		for(my $k=0; $k<(@{$result->{'errors'}}); $k++) {
			my $w = $result->{'errors'}[$k];
			$w =~ s/^/  [Error $k]: /mg;
			print $w."\n";
		}
		print "\n";
	}

};


sub saveParse {
	my ($result, $outputdir, $prefix, $type, $id) = @_;

	make_path($outputdir,{error => \my $err});
	if(@$err) {
		print STDERR "Cannot write parse to $outputdir\n";
		for my $diag (@$err) {
          	my ($file, $message) = %$diag;
          	if ($file eq '') {
            	print "    $message\n";
	        }
	        else {
	            print "    problem unlinking $file: $message\n";
	        }
      	}
	} else {
		my $filename = $outputdir."/".$prefix."-".$id."-result.json";
		open(my $fh, '>', $filename);
		print $fh to_json( $result, { pretty => 1 } )."\n";
		close $fh;
	}


};





# based on current directory, deterimines where the root of the specs directory is
# and using the parameters, determines which methods/apps/types to validate and
# the paths to these files
sub whatShouldIDo {
	my ($root, $method, $app, $type) = @_;

	# get the current location
	my $cwd = abs_path();

	# sets up what we want to validate, 
	#   type= all | methods | apps | categories | types
	#   id= name of a specific method/app/category/type
	my $targetType = "all";
	my $targetId;

	if($root) {
		if(!isRoot($root)) {
			# error- root given is not valid
			print STDERR "The --nms-path option is not valid ('$root' is not a directory or valid specs repo)\n";
			print STDERR "The --nms-path parameter should provide the path to the narrative_method_specs (or \n";
			print STDERR "narrative_method_specs_sandbox) directory.\n";
			exit 1;
		}
	} else {
		if(isRoot('.')) {
			# current directory is root, we need to validate everything
			$root = '.';
		} elsif (isRoot('..')) {
			# we are in the apps/methods/types dir, validate a set of things based on where we are
			$root = '..';
			$targetType = basename($cwd);
		} elsif (isRoot('../..')) {
			# we are inside a specific app/method/type, validate only that specific thing
			$root = '../..';
			$targetType = basename(abs_path('../'));
			$targetId = basename(abs_path('.'));
		} else {
			my $hasSpecs = isRoot('narrative_method_specs');
			my $hasSpecsSandbox = isRoot('narrative_method_specs_sandbox');
			if($hasSpecs && $hasSpecsSandbox) {
				print STDERR "You are running from a directory that has both narrative_method_specs and narrative_method_specs_sandbox\n";
				print STDERR "and you did not indicate a root directory.  I'm not sure what you want to validate, so I won't do anything.\n";
				print STDERR "Either cd into one of those directories and try again, or give me the --nms-path option.\n";
				exit 1;
			} elsif($hasSpecs) {
				$root = 'narrative_method_specs';
			} elsif($hasSpecsSandbox) {
				$root = 'narrative_method_specs_sandbox';
			} else {
				print STDERR "Cannot determine where the narrative_method_specs or narrative_method_specs_sandbox repo is.\n";
				print STDERR "Either cd to the right repository to validate, or give me the --nms-path option.\n";
				exit 1;
			}
		}

	}

	my $targets = {apps=>[],methods=>[],types=>[]}; #,categories=>[]};
	if($method || $app || $type) {
		if($method) {
			if(-d $root."/methods/".$method) {
				push @{$targets->{methods}}, $method;
			} else {
				print STDERR "You asked to validate a Method named $method, but that method was not found here:\n";
				print STDERR "    $root/methods/\n";
				exit 1;
			}
		}
		if($app) {
			if(-d $root."/apps/".$app) {
				push @{$targets->{apps}}, $app;
			} else {
				print STDERR "You asked to validate an App named $app, but that App was not found here:\n";
				print STDERR "    $root/apps/\n";
				exit 1;
			}
		}
		if($type) {
			if(-d $root."/types/".$type) {
				push @{$targets->{types}}, $type;
			} else {
				print STDERR "You asked to validate a Type named $type, but that Type was not found here:\n";
				print STDERR "    $root/types/\n";
				exit 1;
			}
		}
	} else {
		$targets = buildTarget($root, $targetType, $targetId, $targets);
	}

	return ($root, $targets);
};

# given a simple target, expand it to the set of apps/methods/typs/categories to validate
sub buildTarget{
	my ($root,$type,$id,$targets) = @_;

	if($id && $type ne 'all') {
		push @{$targets->{$type}}, $id;
	} else {
		foreach my $currType (keys %{$targets}) {
			if($type eq 'all' || $type eq $currType) {
				opendir(my $dh, $root.'/'.$currType) or return -1;
			    while(readdir $dh) {
			    	my $name = $_;
			    	next if $name eq '.';
			    	next if $name eq '..';
			    	if(-d $root.'/'.$currType.'/'.$name) {
						push @{$targets->{$currType}}, $name;
			    	}
			    }
			}
		}
	}
	return $targets;
};

# determine if the given path points to the root of a narrative_method_specs directory / repo
sub isRoot {
	my ($path) = @_;
	if(! -d $path) { return 0; }

	opendir(my $dh, $path) or return -1;
	my %foundDirs;
    while(readdir $dh) {
    	my $name = $_;
    	next if $name eq '.';
    	next if $name eq '..';
    	if(-d $path.'/'.$name) {
    		$foundDirs{$name}=1;
    	}
    }
    closedir $dh;

    if( defined $foundDirs{apps} && 
    	defined $foundDirs{methods} &&
    	defined $foundDirs{types} &&
    	defined $foundDirs{categories}) {
    		return 1;
    }
    return 0;
};

