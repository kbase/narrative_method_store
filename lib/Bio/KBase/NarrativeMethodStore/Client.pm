package Bio::KBase::NarrativeMethodStore::Client;

use JSON::RPC::Client;
use POSIX;
use strict;
use Data::Dumper;
use URI;
use Bio::KBase::Exceptions;
my $get_time = sub { time, 0 };
eval {
    require Time::HiRes;
    $get_time = sub { Time::HiRes::gettimeofday() };
};


# Client version should match Impl version
# This is a Semantic Version number,
# http://semver.org
our $VERSION = "0.1.0";

=head1 NAME

Bio::KBase::NarrativeMethodStore::Client

=head1 DESCRIPTION





=cut

sub new
{
    my($class, $url, @args) = @_;
    
    if (!defined($url))
    {
	$url = 'https://kbase.us/services/narrative_method_store/rpc';
    }

    my $self = {
	client => Bio::KBase::NarrativeMethodStore::Client::RpcClient->new,
	url => $url,
	headers => [],
    };

    chomp($self->{hostname} = `hostname`);
    $self->{hostname} ||= 'unknown-host';

    #
    # Set up for propagating KBRPC_TAG and KBRPC_METADATA environment variables through
    # to invoked services. If these values are not set, we create a new tag
    # and a metadata field with basic information about the invoking script.
    #
    if ($ENV{KBRPC_TAG})
    {
	$self->{kbrpc_tag} = $ENV{KBRPC_TAG};
    }
    else
    {
	my ($t, $us) = &$get_time();
	$us = sprintf("%06d", $us);
	my $ts = strftime("%Y-%m-%dT%H:%M:%S.${us}Z", gmtime $t);
	$self->{kbrpc_tag} = "C:$0:$self->{hostname}:$$:$ts";
    }
    push(@{$self->{headers}}, 'Kbrpc-Tag', $self->{kbrpc_tag});

    if ($ENV{KBRPC_METADATA})
    {
	$self->{kbrpc_metadata} = $ENV{KBRPC_METADATA};
	push(@{$self->{headers}}, 'Kbrpc-Metadata', $self->{kbrpc_metadata});
    }

    if ($ENV{KBRPC_ERROR_DEST})
    {
	$self->{kbrpc_error_dest} = $ENV{KBRPC_ERROR_DEST};
	push(@{$self->{headers}}, 'Kbrpc-Errordest', $self->{kbrpc_error_dest});
    }


    my $ua = $self->{client}->ua;	 
    my $timeout = $ENV{CDMI_TIMEOUT} || (30 * 60);	 
    $ua->timeout($timeout);
    bless $self, $class;
    #    $self->_validate_version();
    return $self;
}




=head2 ver

  $return = $obj->ver()

=over 4

=item Parameter and return types

=begin html

<pre>
$return is a string

</pre>

=end html

=begin text

$return is a string


=end text

=item Description

Returns the current running version of the NarrativeMethodStore.

=back

=cut

sub ver
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 0)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function ver (received $n, expecting 0)");
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.ver",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'ver',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method ver",
					    status_line => $self->{client}->status_line,
					    method_name => 'ver',
				       );
    }
}



=head2 status

  $return = $obj->status()

=over 4

=item Parameter and return types

=begin html

<pre>
$return is a NarrativeMethodStore.Status
Status is a reference to a hash where the following keys are defined:
	git_spec_url has a value which is a string
	git_spec_branch has a value which is a string
	git_spec_commit has a value which is a string
	update_interval has a value which is a string

</pre>

=end html

=begin text

$return is a NarrativeMethodStore.Status
Status is a reference to a hash where the following keys are defined:
	git_spec_url has a value which is a string
	git_spec_branch has a value which is a string
	git_spec_commit has a value which is a string
	update_interval has a value which is a string


=end text

=item Description

Simply check the status of this service to see what Spec repository it is
using, and what commit it is on

=back

=cut

sub status
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 0)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function status (received $n, expecting 0)");
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.status",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'status',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method status",
					    status_line => $self->{client}->status_line,
					    method_name => 'status',
				       );
    }
}



=head2 list_categories

  $categories, $methods, $apps, $types = $obj->list_categories($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ListCategoriesParams
$categories is a reference to a hash where the key is a string and the value is a NarrativeMethodStore.Category
$methods is a reference to a hash where the key is a string and the value is a NarrativeMethodStore.MethodBriefInfo
$apps is a reference to a hash where the key is a string and the value is a NarrativeMethodStore.AppBriefInfo
$types is a reference to a hash where the key is a string and the value is a NarrativeMethodStore.TypeInfo
ListCategoriesParams is a reference to a hash where the following keys are defined:
	load_methods has a value which is a NarrativeMethodStore.boolean
	load_apps has a value which is a NarrativeMethodStore.boolean
	load_types has a value which is a NarrativeMethodStore.boolean
boolean is an int
Category is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	parent_ids has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ListCategoriesParams
$categories is a reference to a hash where the key is a string and the value is a NarrativeMethodStore.Category
$methods is a reference to a hash where the key is a string and the value is a NarrativeMethodStore.MethodBriefInfo
$apps is a reference to a hash where the key is a string and the value is a NarrativeMethodStore.AppBriefInfo
$types is a reference to a hash where the key is a string and the value is a NarrativeMethodStore.TypeInfo
ListCategoriesParams is a reference to a hash where the following keys are defined:
	load_methods has a value which is a NarrativeMethodStore.boolean
	load_apps has a value which is a NarrativeMethodStore.boolean
	load_types has a value which is a NarrativeMethodStore.boolean
boolean is an int
Category is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	parent_ids has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url


=end text

=item Description



=back

=cut

sub list_categories
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_categories (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to list_categories:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'list_categories');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_categories",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_categories',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_categories",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_categories',
				       );
    }
}



=head2 get_category

  $return = $obj->get_category($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.GetCategoryParams
$return is a reference to a list where each element is a NarrativeMethodStore.Category
GetCategoryParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
Category is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	parent_ids has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetCategoryParams
$return is a reference to a list where each element is a NarrativeMethodStore.Category
GetCategoryParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
Category is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	parent_ids has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string


=end text

=item Description



=back

=cut

sub get_category
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_category (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_category:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_category');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.get_category",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_category',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_category",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_category',
				       );
    }
}



=head2 list_methods

  $return = $obj->list_methods($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodBriefInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodBriefInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string


=end text

=item Description



=back

=cut

sub list_methods
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_methods (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to list_methods:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'list_methods');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_methods",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_methods',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_methods",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_methods',
				       );
    }
}



=head2 list_methods_full_info

  $return = $obj->list_methods_full_info($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodFullInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodFullInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url


=end text

=item Description



=back

=cut

sub list_methods_full_info
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_methods_full_info (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to list_methods_full_info:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'list_methods_full_info');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_methods_full_info",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_methods_full_info',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_methods_full_info",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_methods_full_info',
				       );
    }
}



=head2 list_methods_spec

  $return = $obj->list_methods_spec($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodSpec
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
boolean is an int
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodSpec
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
boolean is an int
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string


=end text

=item Description



=back

=cut

sub list_methods_spec
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_methods_spec (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to list_methods_spec:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'list_methods_spec');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_methods_spec",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_methods_spec',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_methods_spec",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_methods_spec',
				       );
    }
}



=head2 list_method_ids_and_names

  $return = $obj->list_method_ids_and_names()

=over 4

=item Parameter and return types

=begin html

<pre>
$return is a reference to a hash where the key is a string and the value is a string

</pre>

=end html

=begin text

$return is a reference to a hash where the key is a string and the value is a string


=end text

=item Description



=back

=cut

sub list_method_ids_and_names
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 0)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_method_ids_and_names (received $n, expecting 0)");
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_method_ids_and_names",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_method_ids_and_names',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_method_ids_and_names",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_method_ids_and_names',
				       );
    }
}



=head2 list_apps

  $return = $obj->list_apps($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppBriefInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppBriefInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string


=end text

=item Description



=back

=cut

sub list_apps
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_apps (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to list_apps:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'list_apps');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_apps",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_apps',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_apps",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_apps',
				       );
    }
}



=head2 list_apps_full_info

  $return = $obj->list_apps_full_info($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppFullInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppFullInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url


=end text

=item Description



=back

=cut

sub list_apps_full_info
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_apps_full_info (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to list_apps_full_info:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'list_apps_full_info');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_apps_full_info",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_apps_full_info',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_apps_full_info",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_apps_full_info',
				       );
    }
}



=head2 list_apps_spec

  $return = $obj->list_apps_spec($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppSpec
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
boolean is an int

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppSpec
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
boolean is an int


=end text

=item Description



=back

=cut

sub list_apps_spec
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_apps_spec (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to list_apps_spec:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'list_apps_spec');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_apps_spec",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_apps_spec',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_apps_spec",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_apps_spec',
				       );
    }
}



=head2 list_app_ids_and_names

  $return = $obj->list_app_ids_and_names()

=over 4

=item Parameter and return types

=begin html

<pre>
$return is a reference to a hash where the key is a string and the value is a string

</pre>

=end html

=begin text

$return is a reference to a hash where the key is a string and the value is a string


=end text

=item Description



=back

=cut

sub list_app_ids_and_names
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 0)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_app_ids_and_names (received $n, expecting 0)");
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_app_ids_and_names",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_app_ids_and_names',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_app_ids_and_names",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_app_ids_and_names',
				       );
    }
}



=head2 list_types

  $return = $obj->list_types($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.TypeInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ListParams
$return is a reference to a list where each element is a NarrativeMethodStore.TypeInfo
ListParams is a reference to a hash where the following keys are defined:
	limit has a value which is an int
	offset has a value which is an int
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string


=end text

=item Description



=back

=cut

sub list_types
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function list_types (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to list_types:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'list_types');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.list_types",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'list_types',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method list_types",
					    status_line => $self->{client}->status_line,
					    method_name => 'list_types',
				       );
    }
}



=head2 get_method_brief_info

  $return = $obj->get_method_brief_info($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.GetMethodParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodBriefInfo
GetMethodParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetMethodParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodBriefInfo
GetMethodParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string


=end text

=item Description



=back

=cut

sub get_method_brief_info
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_method_brief_info (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_method_brief_info:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_method_brief_info');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.get_method_brief_info",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_method_brief_info',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_method_brief_info",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_method_brief_info',
				       );
    }
}



=head2 get_method_full_info

  $return = $obj->get_method_full_info($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.GetMethodParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodFullInfo
GetMethodParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetMethodParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodFullInfo
GetMethodParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url


=end text

=item Description



=back

=cut

sub get_method_full_info
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_method_full_info (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_method_full_info:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_method_full_info');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.get_method_full_info",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_method_full_info',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_method_full_info",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_method_full_info',
				       );
    }
}



=head2 get_method_spec

  $return = $obj->get_method_spec($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.GetMethodParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodSpec
GetMethodParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
boolean is an int
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetMethodParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodSpec
GetMethodParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
boolean is an int
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string


=end text

=item Description



=back

=cut

sub get_method_spec
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_method_spec (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_method_spec:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_method_spec');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.get_method_spec",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_method_spec',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_method_spec",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_method_spec',
				       );
    }
}



=head2 get_app_brief_info

  $return = $obj->get_app_brief_info($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.GetAppParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppBriefInfo
GetAppParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetAppParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppBriefInfo
GetAppParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string


=end text

=item Description



=back

=cut

sub get_app_brief_info
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_app_brief_info (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_app_brief_info:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_app_brief_info');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.get_app_brief_info",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_app_brief_info',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_app_brief_info",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_app_brief_info',
				       );
    }
}



=head2 get_app_full_info

  $return = $obj->get_app_full_info($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.GetAppParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppFullInfo
GetAppParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetAppParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppFullInfo
GetAppParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url


=end text

=item Description



=back

=cut

sub get_app_full_info
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_app_full_info (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_app_full_info:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_app_full_info');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.get_app_full_info",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_app_full_info',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_app_full_info",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_app_full_info',
				       );
    }
}



=head2 get_app_spec

  $return = $obj->get_app_spec($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.GetAppParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppSpec
GetAppParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
boolean is an int

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetAppParams
$return is a reference to a list where each element is a NarrativeMethodStore.AppSpec
GetAppParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
boolean is an int


=end text

=item Description



=back

=cut

sub get_app_spec
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_app_spec (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_app_spec:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_app_spec');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.get_app_spec",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_app_spec',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_app_spec",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_app_spec',
				       );
    }
}



=head2 get_type_info

  $return = $obj->get_type_info($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.GetTypeParams
$return is a reference to a list where each element is a NarrativeMethodStore.TypeInfo
GetTypeParams is a reference to a hash where the following keys are defined:
	type_names has a value which is a reference to a list where each element is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetTypeParams
$return is a reference to a list where each element is a NarrativeMethodStore.TypeInfo
GetTypeParams is a reference to a hash where the following keys are defined:
	type_names has a value which is a reference to a list where each element is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string


=end text

=item Description



=back

=cut

sub get_type_info
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function get_type_info (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to get_type_info:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'get_type_info');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.get_type_info",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'get_type_info',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method get_type_info",
					    status_line => $self->{client}->status_line,
					    method_name => 'get_type_info',
				       );
    }
}



=head2 validate_method

  $return = $obj->validate_method($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ValidateMethodParams
$return is a NarrativeMethodStore.ValidationResults
ValidateMethodParams is a reference to a hash where the following keys are defined:
	id has a value which is a string
	spec_json has a value which is a string
	display_yaml has a value which is a string
	extra_files has a value which is a reference to a hash where the key is a string and the value is a string
ValidationResults is a reference to a hash where the following keys are defined:
	is_valid has a value which is a NarrativeMethodStore.boolean
	errors has a value which is a reference to a list where each element is a string
	warnings has a value which is a reference to a list where each element is a string
	app_full_info has a value which is a NarrativeMethodStore.AppFullInfo
	app_spec has a value which is a NarrativeMethodStore.AppSpec
	method_full_info has a value which is a NarrativeMethodStore.MethodFullInfo
	method_spec has a value which is a NarrativeMethodStore.MethodSpec
	type_info has a value which is a NarrativeMethodStore.TypeInfo
boolean is an int
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ValidateMethodParams
$return is a NarrativeMethodStore.ValidationResults
ValidateMethodParams is a reference to a hash where the following keys are defined:
	id has a value which is a string
	spec_json has a value which is a string
	display_yaml has a value which is a string
	extra_files has a value which is a reference to a hash where the key is a string and the value is a string
ValidationResults is a reference to a hash where the following keys are defined:
	is_valid has a value which is a NarrativeMethodStore.boolean
	errors has a value which is a reference to a list where each element is a string
	warnings has a value which is a reference to a list where each element is a string
	app_full_info has a value which is a NarrativeMethodStore.AppFullInfo
	app_spec has a value which is a NarrativeMethodStore.AppSpec
	method_full_info has a value which is a NarrativeMethodStore.MethodFullInfo
	method_spec has a value which is a NarrativeMethodStore.MethodSpec
	type_info has a value which is a NarrativeMethodStore.TypeInfo
boolean is an int
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string


=end text

=item Description



=back

=cut

sub validate_method
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function validate_method (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to validate_method:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'validate_method');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.validate_method",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'validate_method',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method validate_method",
					    status_line => $self->{client}->status_line,
					    method_name => 'validate_method',
				       );
    }
}



=head2 validate_app

  $return = $obj->validate_app($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ValidateAppParams
$return is a NarrativeMethodStore.ValidationResults
ValidateAppParams is a reference to a hash where the following keys are defined:
	id has a value which is a string
	spec_json has a value which is a string
	display_yaml has a value which is a string
	extra_files has a value which is a reference to a hash where the key is a string and the value is a string
ValidationResults is a reference to a hash where the following keys are defined:
	is_valid has a value which is a NarrativeMethodStore.boolean
	errors has a value which is a reference to a list where each element is a string
	warnings has a value which is a reference to a list where each element is a string
	app_full_info has a value which is a NarrativeMethodStore.AppFullInfo
	app_spec has a value which is a NarrativeMethodStore.AppSpec
	method_full_info has a value which is a NarrativeMethodStore.MethodFullInfo
	method_spec has a value which is a NarrativeMethodStore.MethodSpec
	type_info has a value which is a NarrativeMethodStore.TypeInfo
boolean is an int
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ValidateAppParams
$return is a NarrativeMethodStore.ValidationResults
ValidateAppParams is a reference to a hash where the following keys are defined:
	id has a value which is a string
	spec_json has a value which is a string
	display_yaml has a value which is a string
	extra_files has a value which is a reference to a hash where the key is a string and the value is a string
ValidationResults is a reference to a hash where the following keys are defined:
	is_valid has a value which is a NarrativeMethodStore.boolean
	errors has a value which is a reference to a list where each element is a string
	warnings has a value which is a reference to a list where each element is a string
	app_full_info has a value which is a NarrativeMethodStore.AppFullInfo
	app_spec has a value which is a NarrativeMethodStore.AppSpec
	method_full_info has a value which is a NarrativeMethodStore.MethodFullInfo
	method_spec has a value which is a NarrativeMethodStore.MethodSpec
	type_info has a value which is a NarrativeMethodStore.TypeInfo
boolean is an int
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string


=end text

=item Description



=back

=cut

sub validate_app
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function validate_app (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to validate_app:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'validate_app');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.validate_app",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'validate_app',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method validate_app",
					    status_line => $self->{client}->status_line,
					    method_name => 'validate_app',
				       );
    }
}



=head2 validate_type

  $return = $obj->validate_type($params)

=over 4

=item Parameter and return types

=begin html

<pre>
$params is a NarrativeMethodStore.ValidateTypeParams
$return is a NarrativeMethodStore.ValidationResults
ValidateTypeParams is a reference to a hash where the following keys are defined:
	id has a value which is a string
	spec_json has a value which is a string
	display_yaml has a value which is a string
	extra_files has a value which is a reference to a hash where the key is a string and the value is a string
ValidationResults is a reference to a hash where the following keys are defined:
	is_valid has a value which is a NarrativeMethodStore.boolean
	errors has a value which is a reference to a list where each element is a string
	warnings has a value which is a reference to a list where each element is a string
	app_full_info has a value which is a NarrativeMethodStore.AppFullInfo
	app_spec has a value which is a NarrativeMethodStore.AppSpec
	method_full_info has a value which is a NarrativeMethodStore.MethodFullInfo
	method_spec has a value which is a NarrativeMethodStore.MethodSpec
	type_info has a value which is a NarrativeMethodStore.TypeInfo
boolean is an int
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.ValidateTypeParams
$return is a NarrativeMethodStore.ValidationResults
ValidateTypeParams is a reference to a hash where the following keys are defined:
	id has a value which is a string
	spec_json has a value which is a string
	display_yaml has a value which is a string
	extra_files has a value which is a reference to a hash where the key is a string and the value is a string
ValidationResults is a reference to a hash where the following keys are defined:
	is_valid has a value which is a NarrativeMethodStore.boolean
	errors has a value which is a reference to a list where each element is a string
	warnings has a value which is a reference to a list where each element is a string
	app_full_info has a value which is a NarrativeMethodStore.AppFullInfo
	app_spec has a value which is a NarrativeMethodStore.AppSpec
	method_full_info has a value which is a NarrativeMethodStore.MethodFullInfo
	method_spec has a value which is a NarrativeMethodStore.MethodSpec
	type_info has a value which is a NarrativeMethodStore.TypeInfo
boolean is an int
AppFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	categories has a value which is a reference to a list where each element is a string
	icon has a value which is a NarrativeMethodStore.Icon
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
Suggestions is a reference to a hash where the following keys are defined:
	related_methods has a value which is a reference to a list where each element is a string
	next_methods has a value which is a reference to a list where each element is a string
	related_apps has a value which is a reference to a list where each element is a string
	next_apps has a value which is a reference to a list where each element is a string
Icon is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
AppSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.AppBriefInfo
	steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps
AppBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	header has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
AppSteps is a reference to a hash where the following keys are defined:
	step_id has a value which is a string
	method_id has a value which is a string
	input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
	description has a value which is a string
AppStepInputMapping is a reference to a hash where the following keys are defined:
	step_source has a value which is a string
	is_from_input has a value which is a NarrativeMethodStore.boolean
	from has a value which is a string
	to has a value which is a string
MethodFullInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	suggestions has a value which is a NarrativeMethodStore.Suggestions
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
	publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication
Publication is a reference to a hash where the following keys are defined:
	pmid has a value which is a string
	display_text has a value which is a string
	link has a value which is a NarrativeMethodStore.url
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	replacement_text has a value which is a string
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
	job_id_output_field has a value which is a string
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	icon has a value which is a NarrativeMethodStore.Icon
	categories has a value which is a reference to a list where each element is a string
	loading_error has a value which is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	description has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	disabled has a value which is a NarrativeMethodStore.boolean
	ui_class has a value which is a string
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
	tab_options has a value which is a NarrativeMethodStore.TabOptions
	textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
	is_output_name has a value which is a NarrativeMethodStore.boolean
	placeholder has a value which is a string
	min_int has a value which is an int
	max_int has a value which is an int
	min_float has a value which is a float
	max_float has a value which is a float
	regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher
RegexMatcher is a reference to a hash where the following keys are defined:
	regex has a value which is a string
	error_text has a value which is a string
	match has a value which is a NarrativeMethodStore.boolean
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
	placeholder has a value which is a string
IntSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is an int
	max has a value which is an int
	step has a value which is an int
FloatSliderOptions is a reference to a hash where the following keys are defined:
	min has a value which is a float
	max has a value which is a float
CheckboxOptions is a reference to a hash where the following keys are defined:
	checked_value has a value which is an int
	unchecked_value has a value which is an int
DropdownOptions is a reference to a hash where the following keys are defined:
	options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption
DropdownOption is a reference to a hash where the following keys are defined:
	value has a value which is a string
	display has a value which is a string
RadioOptions is a reference to a hash where the following keys are defined:
	id_order has a value which is a reference to a list where each element is a string
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
TabOptions is a reference to a hash where the following keys are defined:
	tab_id_order has a value which is a reference to a list where each element is a string
	tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
	tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string
TextSubdataOptions is a reference to a hash where the following keys are defined:
	placeholder has a value which is a string
	multiselection has a value which is a NarrativeMethodStore.boolean
	show_src_obj has a value which is a NarrativeMethodStore.boolean
	allow_custom has a value which is a NarrativeMethodStore.boolean
	subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection
SubdataSelection is a reference to a hash where the following keys are defined:
	constant_ref has a value which is a reference to a list where each element is a string
	parameter_id has a value which is a string
	subdata_included has a value which is a reference to a list where each element is a string
	path_to_subdata has a value which is a reference to a list where each element is a string
	selection_id has a value which is a string
	selection_description has a value which is a reference to a list where each element is a string
	description_template has a value which is a string
FixedMethodParameter is a reference to a hash where the following keys are defined:
	ui_name has a value which is a string
	description has a value which is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_class has a value which is a string
	python_function has a value which is a string
	kb_service_url has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string
	script_module has a value which is a string
	script_name has a value which is a string
	script_has_files has a value which is a NarrativeMethodStore.boolean
	kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
	kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
	output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
	script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
	script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping
ServiceMethodInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_argument_position has a value which is an int
	target_property has a value which is a string
	target_type_transform has a value which is a string
AutoGeneratedValue is a reference to a hash where the following keys are defined:
	prefix has a value which is a string
	symbols has a value which is an int
	suffix has a value which is a string
ServiceMethodOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	service_method_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
OutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptInputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
	target_property has a value which is a string
	target_type_transform has a value which is a string
ScriptOutputMapping is a reference to a hash where the following keys are defined:
	input_parameter has a value which is a string
	script_output_path has a value which is a reference to a list where each element is a string
	constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
	narrative_system_variable has a value which is a string
	target_property has a value which is a string
	target_type_transform has a value which is a string
TypeInfo is a reference to a hash where the following keys are defined:
	type_name has a value which is a string
	name has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	icon has a value which is a NarrativeMethodStore.ScreenShot
	view_method_ids has a value which is a reference to a list where each element is a string
	import_method_ids has a value which is a reference to a list where each element is a string
	landing_page_url_prefix has a value which is a string
	loading_error has a value which is a string


=end text

=item Description



=back

=cut

sub validate_type
{
    my($self, @args) = @_;

# Authentication: none

    if ((my $n = @args) != 1)
    {
	Bio::KBase::Exceptions::ArgumentValidationError->throw(error =>
							       "Invalid argument count for function validate_type (received $n, expecting 1)");
    }
    {
	my($params) = @args;

	my @_bad_arguments;
        (ref($params) eq 'HASH') or push(@_bad_arguments, "Invalid type for argument 1 \"params\" (value was \"$params\")");
        if (@_bad_arguments) {
	    my $msg = "Invalid arguments passed to validate_type:\n" . join("", map { "\t$_\n" } @_bad_arguments);
	    Bio::KBase::Exceptions::ArgumentValidationError->throw(error => $msg,
								   method_name => 'validate_type');
	}
    }

    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
	method => "NarrativeMethodStore.validate_type",
	params => \@args,
    });
    if ($result) {
	if ($result->is_error) {
	    Bio::KBase::Exceptions::JSONRPC->throw(error => $result->error_message,
					       code => $result->content->{error}->{code},
					       method_name => 'validate_type',
					       data => $result->content->{error}->{error} # JSON::RPC::ReturnObject only supports JSONRPC 1.1 or 1.O
					      );
	} else {
	    return wantarray ? @{$result->result} : $result->result->[0];
	}
    } else {
        Bio::KBase::Exceptions::HTTP->throw(error => "Error invoking method validate_type",
					    status_line => $self->{client}->status_line,
					    method_name => 'validate_type',
				       );
    }
}



sub version {
    my ($self) = @_;
    my $result = $self->{client}->call($self->{url}, $self->{headers}, {
        method => "NarrativeMethodStore.version",
        params => [],
    });
    if ($result) {
        if ($result->is_error) {
            Bio::KBase::Exceptions::JSONRPC->throw(
                error => $result->error_message,
                code => $result->content->{code},
                method_name => 'validate_type',
            );
        } else {
            return wantarray ? @{$result->result} : $result->result->[0];
        }
    } else {
        Bio::KBase::Exceptions::HTTP->throw(
            error => "Error invoking method validate_type",
            status_line => $self->{client}->status_line,
            method_name => 'validate_type',
        );
    }
}

sub _validate_version {
    my ($self) = @_;
    my $svr_version = $self->version();
    my $client_version = $VERSION;
    my ($cMajor, $cMinor) = split(/\./, $client_version);
    my ($sMajor, $sMinor) = split(/\./, $svr_version);
    if ($sMajor != $cMajor) {
        Bio::KBase::Exceptions::ClientServerIncompatible->throw(
            error => "Major version numbers differ.",
            server_version => $svr_version,
            client_version => $client_version
        );
    }
    if ($sMinor < $cMinor) {
        Bio::KBase::Exceptions::ClientServerIncompatible->throw(
            error => "Client minor version greater than Server minor version.",
            server_version => $svr_version,
            client_version => $client_version
        );
    }
    if ($sMinor > $cMinor) {
        warn "New client version available for Bio::KBase::NarrativeMethodStore::Client\n";
    }
    if ($sMajor == 0) {
        warn "Bio::KBase::NarrativeMethodStore::Client version is $svr_version. API subject to change.\n";
    }
}

=head1 TYPES



=head2 Status

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
git_spec_url has a value which is a string
git_spec_branch has a value which is a string
git_spec_commit has a value which is a string
update_interval has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
git_spec_url has a value which is a string
git_spec_branch has a value which is a string
git_spec_commit has a value which is a string
update_interval has a value which is a string


=end text

=back



=head2 boolean

=over 4



=item Description

@range [0,1]


=item Definition

=begin html

<pre>
an int
</pre>

=end html

=begin text

an int

=end text

=back



=head2 url

=over 4



=item Definition

=begin html

<pre>
a string
</pre>

=end html

=begin text

a string

=end text

=back



=head2 username

=over 4



=item Definition

=begin html

<pre>
a string
</pre>

=end html

=begin text

a string

=end text

=back



=head2 email

=over 4



=item Definition

=begin html

<pre>
a string
</pre>

=end html

=begin text

a string

=end text

=back



=head2 Category

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
tooltip has a value which is a string
description has a value which is a string
parent_ids has a value which is a reference to a list where each element is a string
loading_error has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
tooltip has a value which is a string
description has a value which is a string
parent_ids has a value which is a reference to a list where each element is a string
loading_error has a value which is a string


=end text

=back



=head2 Icon

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
url has a value which is a NarrativeMethodStore.url

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
url has a value which is a NarrativeMethodStore.url


=end text

=back



=head2 MethodBriefInfo

=over 4



=item Description

Minimal information about a method suitable for displaying the method in a menu or navigator.


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
subtitle has a value which is a string
tooltip has a value which is a string
icon has a value which is a NarrativeMethodStore.Icon
categories has a value which is a reference to a list where each element is a string
loading_error has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
subtitle has a value which is a string
tooltip has a value which is a string
icon has a value which is a NarrativeMethodStore.Icon
categories has a value which is a reference to a list where each element is a string
loading_error has a value which is a string


=end text

=back



=head2 ScreenShot

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
url has a value which is a NarrativeMethodStore.url

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
url has a value which is a NarrativeMethodStore.url


=end text

=back



=head2 Publication

=over 4



=item Description

Publication info can get complicated.  To keep things simple, we only allow a few things now:
pmid - pubmed id, if present, we can use this id to pull all publication info we want
display_text - what is shown to the user if there is no pubmed id, or if the pubmed id is not valid
link - a link to the paper, also not needed if pmid is valid, but could be used if pubmed is down


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
pmid has a value which is a string
display_text has a value which is a string
link has a value which is a NarrativeMethodStore.url

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
pmid has a value which is a string
display_text has a value which is a string
link has a value which is a NarrativeMethodStore.url


=end text

=back



=head2 Suggestions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
related_methods has a value which is a reference to a list where each element is a string
next_methods has a value which is a reference to a list where each element is a string
related_apps has a value which is a reference to a list where each element is a string
next_apps has a value which is a reference to a list where each element is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
related_methods has a value which is a reference to a list where each element is a string
next_methods has a value which is a reference to a list where each element is a string
related_apps has a value which is a reference to a list where each element is a string
next_apps has a value which is a reference to a list where each element is a string


=end text

=back



=head2 MethodFullInfo

=over 4



=item Description

Full information about a method suitable for displaying a method landing page.


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
contact has a value which is a NarrativeMethodStore.email
subtitle has a value which is a string
tooltip has a value which is a string
description has a value which is a string
technical_description has a value which is a string
suggestions has a value which is a NarrativeMethodStore.Suggestions
icon has a value which is a NarrativeMethodStore.Icon
categories has a value which is a reference to a list where each element is a string
screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
kb_contributors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
contact has a value which is a NarrativeMethodStore.email
subtitle has a value which is a string
tooltip has a value which is a string
description has a value which is a string
technical_description has a value which is a string
suggestions has a value which is a NarrativeMethodStore.Suggestions
icon has a value which is a NarrativeMethodStore.Icon
categories has a value which is a reference to a list where each element is a string
screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
publications has a value which is a reference to a list where each element is a NarrativeMethodStore.Publication


=end text

=back



=head2 WidgetSpec

=over 4



=item Description

specify the input / ouput widgets used for rendering


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
input has a value which is a string
output has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
input has a value which is a string
output has a value which is a string


=end text

=back



=head2 RegexMatcher

=over 4



=item Description

regex - regular expression in javascript syntax
error_text - message displayed if the input does not statisfy this constraint
match - set to 1 to check if the input matches this regex, set to 0 to check
        if input does not match this regex.  default is 1


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
regex has a value which is a string
error_text has a value which is a string
match has a value which is a NarrativeMethodStore.boolean

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
regex has a value which is a string
error_text has a value which is a string
match has a value which is a NarrativeMethodStore.boolean


=end text

=back



=head2 TextOptions

=over 4



=item Description

valid_ws_types  - list of valid ws types that can be used for input
validate_as     - int | float | nonnumeric | none
is_output_name  - true if the user is specifying an output name, false otherwise, default is false


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
valid_ws_types has a value which is a reference to a list where each element is a string
validate_as has a value which is a string
is_output_name has a value which is a NarrativeMethodStore.boolean
placeholder has a value which is a string
min_int has a value which is an int
max_int has a value which is an int
min_float has a value which is a float
max_float has a value which is a float
regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
valid_ws_types has a value which is a reference to a list where each element is a string
validate_as has a value which is a string
is_output_name has a value which is a NarrativeMethodStore.boolean
placeholder has a value which is a string
min_int has a value which is an int
max_int has a value which is an int
min_float has a value which is a float
max_float has a value which is a float
regex_constraint has a value which is a reference to a list where each element is a NarrativeMethodStore.RegexMatcher


=end text

=back



=head2 TextAreaOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
n_rows has a value which is an int
placeholder has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
n_rows has a value which is an int
placeholder has a value which is a string


=end text

=back



=head2 IntSliderOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
min has a value which is an int
max has a value which is an int
step has a value which is an int

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
min has a value which is an int
max has a value which is an int
step has a value which is an int


=end text

=back



=head2 FloatSliderOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
min has a value which is a float
max has a value which is a float

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
min has a value which is a float
max has a value which is a float


=end text

=back



=head2 CheckboxOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
checked_value has a value which is an int
unchecked_value has a value which is an int

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
checked_value has a value which is an int
unchecked_value has a value which is an int


=end text

=back



=head2 DropdownOption

=over 4



=item Description

value is what is passed from the form, display is how the selection is
shown to the user


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
value has a value which is a string
display has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
value has a value which is a string
display has a value which is a string


=end text

=back



=head2 DropdownOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
options has a value which is a reference to a list where each element is a NarrativeMethodStore.DropdownOption


=end text

=back



=head2 RadioOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id_order has a value which is a reference to a list where each element is a string
ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id_order has a value which is a reference to a list where each element is a string
ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string


=end text

=back



=head2 TabOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
tab_id_order has a value which is a reference to a list where each element is a string
tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
tab_id_order has a value which is a reference to a list where each element is a string
tab_id_to_tab_name has a value which is a reference to a hash where the key is a string and the value is a string
tab_id_to_param_ids has a value which is a reference to a hash where the key is a string and the value is a reference to a list where each element is a string


=end text

=back



=head2 SubdataSelection

=over 4



=item Description

Information about a subdata selection
    constant_ref - can be set as a fixed reference(s) to data objects
                   so that the dropdown is always populated with a particular
                   WS object - useful for say populating based on an ontology
                   or some other library of default terms, such as compounds
    parameter_id - pick the terms from a user specified parameter in the same
                   method
    path_to_subdata - specific path to a list or map that should be used to
                   populate the fields
    selection_id - If the path_to_subdata is to a list of objects, use this to
                   specify which field of that object should be used as the
                   primary ID
    selection_description - Use this to specify (if the subdata is a list or map)
                    which fields should be included as a short description of
                    the selection.  For features, for instance, this may include
                    the feature function, or feature aliases.
    description_template - Defines how the description of items is rendered using
                    Handlebar templates (use the name of items in the 
                    selection_description list as variable names)


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
constant_ref has a value which is a reference to a list where each element is a string
parameter_id has a value which is a string
subdata_included has a value which is a reference to a list where each element is a string
path_to_subdata has a value which is a reference to a list where each element is a string
selection_id has a value which is a string
selection_description has a value which is a reference to a list where each element is a string
description_template has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
constant_ref has a value which is a reference to a list where each element is a string
parameter_id has a value which is a string
subdata_included has a value which is a reference to a list where each element is a string
path_to_subdata has a value which is a reference to a list where each element is a string
selection_id has a value which is a string
selection_description has a value which is a reference to a list where each element is a string
description_template has a value which is a string


=end text

=back



=head2 TextSubdataOptions

=over 4



=item Description

Defines a parameter field that allows autocomplete based on 
subdata of an existing object.  For instance, selection of feature ids
from a Genome object.  It will appear as a text field with dropdown
similar to selection of other WS data objects.
    placeholder - placeholder text to display in the field
    multiselection - if true, then multiple selections are allowed in
                     a single input field.  This will override the
                     allow_multiple option (which allows user addition)
                     of additional fields.  If true, then this parameter
                     will return a list. Default= false
    show_src_obj - if true, then the dropdown will indicate the ids along
                   with some text indicating what data object the subdata
                   was retrieved from. Default=true
    allow_custom - if true, then user specified inputs not found in the
                   list are accepted.  if false, users can only select from
                   the valid list of selections. Default=false


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
placeholder has a value which is a string
multiselection has a value which is a NarrativeMethodStore.boolean
show_src_obj has a value which is a NarrativeMethodStore.boolean
allow_custom has a value which is a NarrativeMethodStore.boolean
subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
placeholder has a value which is a string
multiselection has a value which is a NarrativeMethodStore.boolean
show_src_obj has a value which is a NarrativeMethodStore.boolean
allow_custom has a value which is a NarrativeMethodStore.boolean
subdata_selection has a value which is a NarrativeMethodStore.SubdataSelection


=end text

=back



=head2 MethodParameter

=over 4



=item Description

Description of a method parameter.

id - id of the parameter, must be unique within the method
ui_name - short name that is displayed to the user
short_hint - short phrase or sentence describing the parameter
description - longer and more technical description of the parameter
field_type - one of: text | textarea | textsubdata | intslider | floatslider | checkbox |
             dropdown | radio | tab | file
allow_mutiple - only supported for field_type text, allows entry of a list
                instead of a single value, default is 0
                if set, the number of starting boxes will be either 1 or the
                number of elements in the default_values list
optional - set to true to make the field optional, default is 0
advanced - set to true to make this an advanced option, default is 0
           if an option is advanced, it should also be optional or have
           a default value
disabled   - set to true to disable user input, default is 0
           if disabled, a default value should be provided

ui_class  - input | output | parameter
           value is autogenerated based on the specification which determines
           if it is an input parameter, output parameter, or just plain old parameter
           (input is generally an input data object, output is an output data object, 
           and plain old parameter is more or less numbers, fixed selections, etc)

@optional text_options textarea_options intslider_options floatslider_options
@optional checkbox_options dropdown_options radio_options tab_options


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
ui_name has a value which is a string
short_hint has a value which is a string
description has a value which is a string
field_type has a value which is a string
allow_multiple has a value which is a NarrativeMethodStore.boolean
optional has a value which is a NarrativeMethodStore.boolean
advanced has a value which is a NarrativeMethodStore.boolean
disabled has a value which is a NarrativeMethodStore.boolean
ui_class has a value which is a string
default_values has a value which is a reference to a list where each element is a string
text_options has a value which is a NarrativeMethodStore.TextOptions
textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
radio_options has a value which is a NarrativeMethodStore.RadioOptions
tab_options has a value which is a NarrativeMethodStore.TabOptions
textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
ui_name has a value which is a string
short_hint has a value which is a string
description has a value which is a string
field_type has a value which is a string
allow_multiple has a value which is a NarrativeMethodStore.boolean
optional has a value which is a NarrativeMethodStore.boolean
advanced has a value which is a NarrativeMethodStore.boolean
disabled has a value which is a NarrativeMethodStore.boolean
ui_class has a value which is a string
default_values has a value which is a reference to a list where each element is a string
text_options has a value which is a NarrativeMethodStore.TextOptions
textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
radio_options has a value which is a NarrativeMethodStore.RadioOptions
tab_options has a value which is a NarrativeMethodStore.TabOptions
textsubdata_options has a value which is a NarrativeMethodStore.TextSubdataOptions


=end text

=back



=head2 FixedMethodParameter

=over 4



=item Description

a fixed parameter that does not appear in the method input forms, but is informational for users in describing
a backend parameter that cannot be changed (e.g. if a service picks a fixed parameter for say Blast)


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
ui_name has a value which is a string
description has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
ui_name has a value which is a string
description has a value which is a string


=end text

=back



=head2 AutoGeneratedValue

=over 4



=item Description

prefix - optional string concatenated before generated part
symbols - number of generated characters, optional, default is 8
suffix - optional string concatenated after generated part
@optional prefix symbols suffix


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
prefix has a value which is a string
symbols has a value which is an int
suffix has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
prefix has a value which is a string
symbols has a value which is an int
suffix has a value which is a string


=end text

=back



=head2 ServiceMethodInputMapping

=over 4



=item Description

input_parameter - parameter_id, if not specified then one of 'constant_value' or 
    'narrative_system_variable' should be set.
constant_value - constant value, could be even map/array, if not specified then 'input_parameter' or
    'narrative_system_variable' should be set.
narrative_system_variable - name of internal narrative framework property, currently only these names are
    supported: 'workspace', 'token', 'user_id'; if not specified then one of 'input_parameter' or
    'constant_value' should be set.
generated_value - automatically generated value; it could be used as independent mode or when another mode 
    finished with empty value (for example in case 'input_parameter' is defined but value of this
    parameter is left empty by user); so this mode has lower priority when used with another mode.
target_argument_position - position of argument in RPC-method call, optional field, default value is 0.
target_property - name of field inside structure that will be send as arguement. Optional field,
    in case this field is not defined (or null) whole object will be sent as method argument instead of
    wrapping it by structure with inner property defined by 'target_property'.
target_type_transform - none/string/int/float/ref, optional field, default is 'none' (it's in plans to
    support list<type>, mapping<type> and tuple<t1,t2,...> transformations).
@optional input_parameter constant_value narrative_system_variable generated_value 
@optional target_argument_position target_property target_type_transform


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
target_argument_position has a value which is an int
target_property has a value which is a string
target_type_transform has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
target_argument_position has a value which is an int
target_property has a value which is a string
target_type_transform has a value which is a string


=end text

=back



=head2 ServiceMethodOutputMapping

=over 4



=item Description

input_parameter - parameter_id, if not specified then one of 'constant_value' or 
    'narrative_system_variable' should be set.
service_method_output_path - list of properties and array element positions defining JSON-path traversing
    through which we can find necessary value. 
constant_value - constant value, could be even map/array, if not specified then 'input_parameter' or
    'narrative_system_variable' should be set.
narrative_system_variable - name of internal narrative framework property, currently only these names are
    supported: 'workspace', 'token', 'user_id'; if not specified then one of 'input_parameter' or
    'constant_value' should be set.
target_property - name of field inside structure that will be send as arguement. Optional field,
    in case this field is not defined (or null) whole object will be sent as method argument instead of
    wrapping it by structure with inner property defined by 'target_property'.
target_type_transform - none/string/int/float/list<type>/mapping<type>/ref, optional field, default is 
    no transformation.
@optional input_parameter service_method_output_path constant_value narrative_system_variable 
@optional target_property target_type_transform


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
service_method_output_path has a value which is a reference to a list where each element is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
target_property has a value which is a string
target_type_transform has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
service_method_output_path has a value which is a reference to a list where each element is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
target_property has a value which is a string
target_type_transform has a value which is a string


=end text

=back



=head2 OutputMapping

=over 4



=item Description

This structure should be used in case narrative method doesn't run any back-end code. 
See docs for ServiceMethodOutputMapping type for details.


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
target_property has a value which is a string
target_type_transform has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
target_property has a value which is a string
target_type_transform has a value which is a string


=end text

=back



=head2 ScriptInputMapping

=over 4



=item Description

input_parameter - parameter_id, if not specified then one of 'constant_value' or 
    'narrative_system_variable' should be set.
constant_value - constant value, could be even map/array, if not specified then 'input_parameter' or
    'narrative_system_variable' should be set.
narrative_system_variable - name of internal narrative framework property, currently only these names are
    supported: 'workspace', 'token', 'user_id'; if not specified then one of 'input_parameter' or
    'constant_value' should be set.
generated_value - automatically generated value; it could be used as independent mode or when another mode 
    finished with empty value (for example in case 'input_parameter' is defined but value of this
    parameter is left empty by user); so this mode has lower priority when used with another mode.
target_property - name of script parameter.
target_type_transform - none/string/int/float/ref, optional field, default is 'none' (it's in plans to
    support list<type>, mapping<type> and tuple<t1,t2,...> transformations).
@optional input_parameter constant_value narrative_system_variable generated_value 
@optional target_property target_type_transform


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
target_property has a value which is a string
target_type_transform has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
generated_value has a value which is a NarrativeMethodStore.AutoGeneratedValue
target_property has a value which is a string
target_type_transform has a value which is a string


=end text

=back



=head2 ScriptOutputMapping

=over 4



=item Description

input_parameter - parameter_id, if not specified then one of 'constant_value' or 
    'narrative_system_variable' should be set.
script_output_path - list of properties and array element positions defining JSON-path traversing
    through which we can find necessary value. 
constant_value - constant value, could be even map/array, if not specified then 'input_parameter' or
    'narrative_system_variable' should be set.
narrative_system_variable - name of internal narrative framework property, currently only these names are
    supported: 'workspace', 'token', 'user_id'; if not specified then one of 'input_parameter' or
    'constant_value' should be set.
target_property - name of field inside structure that will be send as arguement. Optional field,
    in case this field is not defined (or null) whole object will be sent as method argument instead of
    wrapping it by structure with inner property defined by 'target_property'.
target_type_transform - none/string/int/float/list<type>/mapping<type>/ref, optional field, default is 
    no transformation.
@optional input_parameter script_output_path constant_value narrative_system_variable 
@optional target_property target_type_transform


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
script_output_path has a value which is a reference to a list where each element is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
target_property has a value which is a string
target_type_transform has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
input_parameter has a value which is a string
script_output_path has a value which is a reference to a list where each element is a string
constant_value has a value which is an UnspecifiedObject, which can hold any non-null object
narrative_system_variable has a value which is a string
target_property has a value which is a string
target_type_transform has a value which is a string


=end text

=back



=head2 MethodBehavior

=over 4



=item Description

Determines how the method is handled when run.
kb_service_name - name of service which will be part of fully qualified method name, optional field (in
    case it's not defined developer should enter fully qualified name with dot into 'kb_service_method'.
kb_service_input_mapping - mapping from input parameters to input service method arguments.
kb_service_output_mapping - mapping from output of service method to final output of narrative method.
output_mapping - mapping from input to final output of narrative method to support steps without back-end operations.
kb_service_input_mapping - mapping from input parameters to input service method arguments.
kb_service_output_mapping - mapping from output of service method to final output of narrative method.
@optional python_function kb_service_name kb_service_method kb_service_input_mapping kb_service_output_mapping


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
python_class has a value which is a string
python_function has a value which is a string
kb_service_url has a value which is a string
kb_service_name has a value which is a string
kb_service_method has a value which is a string
script_module has a value which is a string
script_name has a value which is a string
script_has_files has a value which is a NarrativeMethodStore.boolean
kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
python_class has a value which is a string
python_function has a value which is a string
kb_service_url has a value which is a string
kb_service_name has a value which is a string
kb_service_method has a value which is a string
script_module has a value which is a string
script_name has a value which is a string
script_has_files has a value which is a NarrativeMethodStore.boolean
kb_service_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodInputMapping
kb_service_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ServiceMethodOutputMapping
output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.OutputMapping
script_input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptInputMapping
script_output_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.ScriptOutputMapping


=end text

=back



=head2 MethodSpec

=over 4



=item Description

The method specification which should provide enough information to render a default
input widget for the method.

replacement_text indicates the text that should replace the input boxes after the method
has run.  You can refer to parameters by putting them in double curly braces (on the front
end we will use the handlebars library).
   for example:  Ran flux balance analysis on model {{model_param}} with parameter 2 set to {{param2}}.


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
info has a value which is a NarrativeMethodStore.MethodBriefInfo
replacement_text has a value which is a string
widgets has a value which is a NarrativeMethodStore.WidgetSpec
parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
behavior has a value which is a NarrativeMethodStore.MethodBehavior
job_id_output_field has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
info has a value which is a NarrativeMethodStore.MethodBriefInfo
replacement_text has a value which is a string
widgets has a value which is a NarrativeMethodStore.WidgetSpec
parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
fixed_parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.FixedMethodParameter
behavior has a value which is a NarrativeMethodStore.MethodBehavior
job_id_output_field has a value which is a string


=end text

=back



=head2 AppBriefInfo

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
subtitle has a value which is a string
tooltip has a value which is a string
header has a value which is a string
icon has a value which is a NarrativeMethodStore.Icon
categories has a value which is a reference to a list where each element is a string
loading_error has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
subtitle has a value which is a string
tooltip has a value which is a string
header has a value which is a string
icon has a value which is a NarrativeMethodStore.Icon
categories has a value which is a reference to a list where each element is a string
loading_error has a value which is a string


=end text

=back



=head2 AppFullInfo

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
contact has a value which is a NarrativeMethodStore.email
subtitle has a value which is a string
tooltip has a value which is a string
header has a value which is a string
description has a value which is a string
technical_description has a value which is a string
suggestions has a value which is a NarrativeMethodStore.Suggestions
categories has a value which is a reference to a list where each element is a string
icon has a value which is a NarrativeMethodStore.Icon
screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
authors has a value which is a reference to a list where each element is a NarrativeMethodStore.username
contact has a value which is a NarrativeMethodStore.email
subtitle has a value which is a string
tooltip has a value which is a string
header has a value which is a string
description has a value which is a string
technical_description has a value which is a string
suggestions has a value which is a NarrativeMethodStore.Suggestions
categories has a value which is a reference to a list where each element is a string
icon has a value which is a NarrativeMethodStore.Icon
screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot


=end text

=back



=head2 AppStepInputMapping

=over 4



=item Description

Defines how any input to a particular step should be
populated based 
step_source - the id of the step to pull the parameter from
isFromInput - set to true (1) to indicate that the input should be pulled from the input
    parameters of the step_source.  This is the only supported option.  In the future, it
    may be possible to pull the input from the output of the previous step (which would
    require special handling of the app runner).
from - the id of the input parameter/output field in step_source to retrieve the value
to - the name of the parameter to automatically populate in this step
transformation - not supported yet, but may be used to indicate if a transformation of the
    value should occur when mapping the input to this step
//@optional transformation


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
step_source has a value which is a string
is_from_input has a value which is a NarrativeMethodStore.boolean
from has a value which is a string
to has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
step_source has a value which is a string
is_from_input has a value which is a NarrativeMethodStore.boolean
from has a value which is a string
to has a value which is a string


=end text

=back



=head2 AppSteps

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
step_id has a value which is a string
method_id has a value which is a string
input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
description has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
step_id has a value which is a string
method_id has a value which is a string
input_mapping has a value which is a reference to a list where each element is a NarrativeMethodStore.AppStepInputMapping
description has a value which is a string


=end text

=back



=head2 AppSpec

=over 4



=item Description

typedef structure {

} AppBehavior;


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
info has a value which is a NarrativeMethodStore.AppBriefInfo
steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
info has a value which is a NarrativeMethodStore.AppBriefInfo
steps has a value which is a reference to a list where each element is a NarrativeMethodStore.AppSteps


=end text

=back



=head2 TypeInfo

=over 4



=item Description

@optional icon landing_page_url_prefix loading_error


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
type_name has a value which is a string
name has a value which is a string
subtitle has a value which is a string
tooltip has a value which is a string
description has a value which is a string
icon has a value which is a NarrativeMethodStore.ScreenShot
view_method_ids has a value which is a reference to a list where each element is a string
import_method_ids has a value which is a reference to a list where each element is a string
landing_page_url_prefix has a value which is a string
loading_error has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
type_name has a value which is a string
name has a value which is a string
subtitle has a value which is a string
tooltip has a value which is a string
description has a value which is a string
icon has a value which is a NarrativeMethodStore.ScreenShot
view_method_ids has a value which is a reference to a list where each element is a string
import_method_ids has a value which is a reference to a list where each element is a string
landing_page_url_prefix has a value which is a string
loading_error has a value which is a string


=end text

=back



=head2 ListCategoriesParams

=over 4



=item Description

List all the categories.  Optionally, if load_methods or load_apps are set to 1,
information about all the methods and apps is provided.  This is important
load_methods - optional field (default value is 1)


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
load_methods has a value which is a NarrativeMethodStore.boolean
load_apps has a value which is a NarrativeMethodStore.boolean
load_types has a value which is a NarrativeMethodStore.boolean

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
load_methods has a value which is a NarrativeMethodStore.boolean
load_apps has a value which is a NarrativeMethodStore.boolean
load_types has a value which is a NarrativeMethodStore.boolean


=end text

=back



=head2 GetCategoryParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
ids has a value which is a reference to a list where each element is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
ids has a value which is a reference to a list where each element is a string


=end text

=back



=head2 ListParams

=over 4



=item Description

These parameters do nothing currently, but are a placeholder for future options
on listing methods or apps
limit - optional field (default value is 0)
offset - optional field (default value is 0)


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
limit has a value which is an int
offset has a value which is an int

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
limit has a value which is an int
offset has a value which is an int


=end text

=back



=head2 GetMethodParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
ids has a value which is a reference to a list where each element is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
ids has a value which is a reference to a list where each element is a string


=end text

=back



=head2 GetAppParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
ids has a value which is a reference to a list where each element is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
ids has a value which is a reference to a list where each element is a string


=end text

=back



=head2 GetTypeParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
type_names has a value which is a reference to a list where each element is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
type_names has a value which is a reference to a list where each element is a string


=end text

=back



=head2 ValidateMethodParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
spec_json has a value which is a string
display_yaml has a value which is a string
extra_files has a value which is a reference to a hash where the key is a string and the value is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
spec_json has a value which is a string
display_yaml has a value which is a string
extra_files has a value which is a reference to a hash where the key is a string and the value is a string


=end text

=back



=head2 ValidationResults

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
is_valid has a value which is a NarrativeMethodStore.boolean
errors has a value which is a reference to a list where each element is a string
warnings has a value which is a reference to a list where each element is a string
app_full_info has a value which is a NarrativeMethodStore.AppFullInfo
app_spec has a value which is a NarrativeMethodStore.AppSpec
method_full_info has a value which is a NarrativeMethodStore.MethodFullInfo
method_spec has a value which is a NarrativeMethodStore.MethodSpec
type_info has a value which is a NarrativeMethodStore.TypeInfo

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
is_valid has a value which is a NarrativeMethodStore.boolean
errors has a value which is a reference to a list where each element is a string
warnings has a value which is a reference to a list where each element is a string
app_full_info has a value which is a NarrativeMethodStore.AppFullInfo
app_spec has a value which is a NarrativeMethodStore.AppSpec
method_full_info has a value which is a NarrativeMethodStore.MethodFullInfo
method_spec has a value which is a NarrativeMethodStore.MethodSpec
type_info has a value which is a NarrativeMethodStore.TypeInfo


=end text

=back



=head2 ValidateAppParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
spec_json has a value which is a string
display_yaml has a value which is a string
extra_files has a value which is a reference to a hash where the key is a string and the value is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
spec_json has a value which is a string
display_yaml has a value which is a string
extra_files has a value which is a reference to a hash where the key is a string and the value is a string


=end text

=back



=head2 ValidateTypeParams

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
spec_json has a value which is a string
display_yaml has a value which is a string
extra_files has a value which is a reference to a hash where the key is a string and the value is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
spec_json has a value which is a string
display_yaml has a value which is a string
extra_files has a value which is a reference to a hash where the key is a string and the value is a string


=end text

=back



=cut

package Bio::KBase::NarrativeMethodStore::Client::RpcClient;
use base 'JSON::RPC::Client';
use POSIX;
use strict;

#
# Override JSON::RPC::Client::call because it doesn't handle error returns properly.
#

sub call {
    my ($self, $uri, $headers, $obj) = @_;
    my $result;


    {
	if ($uri =~ /\?/) {
	    $result = $self->_get($uri);
	}
	else {
	    Carp::croak "not hashref." unless (ref $obj eq 'HASH');
	    $result = $self->_post($uri, $headers, $obj);
	}

    }

    my $service = $obj->{method} =~ /^system\./ if ( $obj );

    $self->status_line($result->status_line);

    if ($result->is_success) {

        return unless($result->content); # notification?

        if ($service) {
            return JSON::RPC::ServiceObject->new($result, $self->json);
        }

        return JSON::RPC::ReturnObject->new($result, $self->json);
    }
    elsif ($result->content_type eq 'application/json')
    {
        return JSON::RPC::ReturnObject->new($result, $self->json);
    }
    else {
        return;
    }
}


sub _post {
    my ($self, $uri, $headers, $obj) = @_;
    my $json = $self->json;

    $obj->{version} ||= $self->{version} || '1.1';

    if ($obj->{version} eq '1.0') {
        delete $obj->{version};
        if (exists $obj->{id}) {
            $self->id($obj->{id}) if ($obj->{id}); # if undef, it is notification.
        }
        else {
            $obj->{id} = $self->id || ($self->id('JSON::RPC::Client'));
        }
    }
    else {
        # $obj->{id} = $self->id if (defined $self->id);
	# Assign a random number to the id if one hasn't been set
	$obj->{id} = (defined $self->id) ? $self->id : substr(rand(),2);
    }

    my $content = $json->encode($obj);

    $self->ua->post(
        $uri,
        Content_Type   => $self->{content_type},
        Content        => $content,
        Accept         => 'application/json',
	@$headers,
	($self->{token} ? (Authorization => $self->{token}) : ()),
    );
}



1;
