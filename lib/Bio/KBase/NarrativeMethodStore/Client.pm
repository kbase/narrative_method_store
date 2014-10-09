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
	$url = 'https://kbase.us/services/narrative_method_store/';
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
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
categorization is a reference to a list where each element is a string

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
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
categorization is a reference to a list where each element is a string


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
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
categorization is a reference to a list where each element is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string

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
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
categorization is a reference to a list where each element is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string


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
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
categorization is a reference to a list where each element is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	long_hint has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
boolean is an int
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
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
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
RadioOptions is a reference to a hash where the following keys are defined:
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_function has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string

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
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
categorization is a reference to a list where each element is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	long_hint has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
boolean is an int
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
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
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
RadioOptions is a reference to a hash where the following keys are defined:
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_function has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string


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
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
categorization is a reference to a list where each element is a string

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
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
categorization is a reference to a list where each element is a string


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
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
categorization is a reference to a list where each element is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string

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
	contact has a value which is a NarrativeMethodStore.email
	subtitle has a value which is a string
	tooltip has a value which is a string
	description has a value which is a string
	technical_description has a value which is a string
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
	screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot
username is a string
email is a string
categorization is a reference to a list where each element is a string
ScreenShot is a reference to a hash where the following keys are defined:
	url has a value which is a NarrativeMethodStore.url
url is a string


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
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
categorization is a reference to a list where each element is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	long_hint has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
boolean is an int
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
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
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
RadioOptions is a reference to a hash where the following keys are defined:
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_function has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string

</pre>

=end html

=begin text

$params is a NarrativeMethodStore.GetMethodParams
$return is a reference to a list where each element is a NarrativeMethodStore.MethodSpec
GetMethodParams is a reference to a hash where the following keys are defined:
	ids has a value which is a reference to a list where each element is a string
MethodSpec is a reference to a hash where the following keys are defined:
	info has a value which is a NarrativeMethodStore.MethodBriefInfo
	widgets has a value which is a NarrativeMethodStore.WidgetSpec
	parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
	behavior has a value which is a NarrativeMethodStore.MethodBehavior
MethodBriefInfo is a reference to a hash where the following keys are defined:
	id has a value which is a string
	name has a value which is a string
	ver has a value which is a string
	subtitle has a value which is a string
	tooltip has a value which is a string
	categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
categorization is a reference to a list where each element is a string
WidgetSpec is a reference to a hash where the following keys are defined:
	input has a value which is a string
	output has a value which is a string
MethodParameter is a reference to a hash where the following keys are defined:
	id has a value which is a string
	ui_name has a value which is a string
	short_hint has a value which is a string
	long_hint has a value which is a string
	field_type has a value which is a string
	allow_multiple has a value which is a NarrativeMethodStore.boolean
	optional has a value which is a NarrativeMethodStore.boolean
	advanced has a value which is a NarrativeMethodStore.boolean
	default_values has a value which is a reference to a list where each element is a string
	text_options has a value which is a NarrativeMethodStore.TextOptions
	textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
	intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
	floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
	checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
	dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
	radio_options has a value which is a NarrativeMethodStore.RadioOptions
boolean is an int
TextOptions is a reference to a hash where the following keys are defined:
	valid_ws_types has a value which is a reference to a list where each element is a string
	validate_as has a value which is a string
TextAreaOptions is a reference to a hash where the following keys are defined:
	n_rows has a value which is an int
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
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
RadioOptions is a reference to a hash where the following keys are defined:
	ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
	ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string
MethodBehavior is a reference to a hash where the following keys are defined:
	python_function has a value which is a string
	kb_service_name has a value which is a string
	kb_service_method has a value which is a string


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
                method_name => 'get_method_spec',
            );
        } else {
            return wantarray ? @{$result->result} : $result->result->[0];
        }
    } else {
        Bio::KBase::Exceptions::HTTP->throw(
            error => "Error invoking method get_method_spec",
            status_line => $self->{client}->status_line,
            method_name => 'get_method_spec',
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



=head2 categorization

=over 4



=item Description

Organization of where in a menu the method should appear


=item Definition

=begin html

<pre>
a reference to a list where each element is a string
</pre>

=end html

=begin text

a reference to a list where each element is a string

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
categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
name has a value which is a string
ver has a value which is a string
subtitle has a value which is a string
tooltip has a value which is a string
categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization


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
contact has a value which is a NarrativeMethodStore.email
subtitle has a value which is a string
tooltip has a value which is a string
description has a value which is a string
technical_description has a value which is a string
categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
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
description has a value which is a string
technical_description has a value which is a string
categorizations has a value which is a reference to a list where each element is a NarrativeMethodStore.categorization
screenshots has a value which is a reference to a list where each element is a NarrativeMethodStore.ScreenShot


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



=head2 TextOptions

=over 4



=item Description

valid_ws_types  - list of valid ws types that can be used for input
validate_as     - int | float | nonnumeric | none


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
valid_ws_types has a value which is a reference to a list where each element is a string
validate_as has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
valid_ws_types has a value which is a reference to a list where each element is a string
validate_as has a value which is a string


=end text

=back



=head2 TextAreaOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
n_rows has a value which is an int

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
n_rows has a value which is an int


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



=head2 DropdownOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string


=end text

=back



=head2 RadioOptions

=over 4



=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
ids_to_options has a value which is a reference to a hash where the key is a string and the value is a string
ids_to_tooltip has a value which is a reference to a hash where the key is a string and the value is a string


=end text

=back



=head2 MethodParameter

=over 4



=item Description

Description of a method parameter.
@optional text_options textarea_options intslider_options checkbox_options
@optional dropdown_options radio_options


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
id has a value which is a string
ui_name has a value which is a string
short_hint has a value which is a string
long_hint has a value which is a string
field_type has a value which is a string
allow_multiple has a value which is a NarrativeMethodStore.boolean
optional has a value which is a NarrativeMethodStore.boolean
advanced has a value which is a NarrativeMethodStore.boolean
default_values has a value which is a reference to a list where each element is a string
text_options has a value which is a NarrativeMethodStore.TextOptions
textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
radio_options has a value which is a NarrativeMethodStore.RadioOptions

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
id has a value which is a string
ui_name has a value which is a string
short_hint has a value which is a string
long_hint has a value which is a string
field_type has a value which is a string
allow_multiple has a value which is a NarrativeMethodStore.boolean
optional has a value which is a NarrativeMethodStore.boolean
advanced has a value which is a NarrativeMethodStore.boolean
default_values has a value which is a reference to a list where each element is a string
text_options has a value which is a NarrativeMethodStore.TextOptions
textarea_options has a value which is a NarrativeMethodStore.TextAreaOptions
intslider_options has a value which is a NarrativeMethodStore.IntSliderOptions
floatslider_options has a value which is a NarrativeMethodStore.FloatSliderOptions
checkbox_options has a value which is a NarrativeMethodStore.CheckboxOptions
dropdown_options has a value which is a NarrativeMethodStore.DropdownOptions
radio_options has a value which is a NarrativeMethodStore.RadioOptions


=end text

=back



=head2 MethodBehavior

=over 4



=item Description

Determines how the method is handled when run.
@optional python_function kb_service_name kb_service_method


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
python_function has a value which is a string
kb_service_name has a value which is a string
kb_service_method has a value which is a string

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
python_function has a value which is a string
kb_service_name has a value which is a string
kb_service_method has a value which is a string


=end text

=back



=head2 MethodSpec

=over 4



=item Description

The method specification which should provide enough information to render a default
input widget for the method.


=item Definition

=begin html

<pre>
a reference to a hash where the following keys are defined:
info has a value which is a NarrativeMethodStore.MethodBriefInfo
widgets has a value which is a NarrativeMethodStore.WidgetSpec
parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
behavior has a value which is a NarrativeMethodStore.MethodBehavior

</pre>

=end html

=begin text

a reference to a hash where the following keys are defined:
info has a value which is a NarrativeMethodStore.MethodBriefInfo
widgets has a value which is a NarrativeMethodStore.WidgetSpec
parameters has a value which is a reference to a list where each element is a NarrativeMethodStore.MethodParameter
behavior has a value which is a NarrativeMethodStore.MethodBehavior


=end text

=back



=head2 ListParams

=over 4



=item Description

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
