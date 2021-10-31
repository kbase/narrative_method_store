/*

*/
module NarrativeMethodStore {

    /* Returns the current running version of the NarrativeMethodStore. */
    funcdef ver() returns (string);

    typedef structure {
    	string git_spec_url;
    	string git_spec_branch;
    	string git_spec_commit;
    	string update_interval;
    } Status;

    /* Simply check the status of this service to see what Spec repository it is
    using, and what commit it is on */
    funcdef status() returns (Status);


    /* @range [0,1] */
    typedef int boolean;

    typedef string url;
    typedef string username;
    typedef string email;

    typedef structure {
        string id;
        string name;
        string ver;
        string tooltip;
        string description;
        list<string> parent_ids;
        string loading_error;
    } Category;

    typedef structure {
        url url;
    } Icon;

    /* Minimal information about a method suitable for displaying the method in a menu or navigator.
         input_types and output_types - sets of valid_ws_types occured in input/output parameters.
         git_commit_hash - optional repo version defined for dynamically registered methods.
         app_type - is one of: "app", "viewer", "editor".
    */
    typedef structure {
        string id;
        string module_name;
        string git_commit_hash;
        string name;
        string ver;
        string subtitle;
        string tooltip;
        Icon icon;
        list<string> categories;
        string loading_error;
        list <username> authors;
        list <string> input_types;
        list <string> output_types;
        string app_type;
    } MethodBriefInfo;

    typedef structure {
        url url;
    } ScreenShot;

    /* Publication info can get complicated.  To keep things simple, we only allow a few things now:
         pmid - pubmed id, if present, we can use this id to pull all publication info we want
         display_text - what is shown to the user if there is no pubmed id, or if the pubmed id is not valid
         link - a link to the paper, also not needed if pmid is valid, but could be used if pubmed is down
    */
    typedef structure {
        string pmid;
        string display_text;
        url link;
    } Publication;


    typedef structure {
        list<string> related_methods;
        list<string> next_methods;
        list<string> related_apps;
        list<string> next_apps;
    } Suggestions;

    /* Full information about a method suitable for displaying a method landing page.
         git_commit_hash - optional repo version defined for dynamically registered methods.
         app_type - is one of: "app", "viewer", "editor".
    */
    typedef structure {
        string id;
        string module_name;
        string git_commit_hash;
        string name;
        string ver;
        list <username> authors;
        list <username> kb_contributors;
        email contact;

        string subtitle;
        string tooltip;
        string description;
        string technical_description;
        string app_type;

        Suggestions suggestions;

        Icon icon;

        list<string> categories;

        list<ScreenShot> screenshots;

        list<Publication> publications;

    } MethodFullInfo;

    /* specify the input / ouput widgets used for rendering */
    typedef structure {
        string input;
        string output;
    } WidgetSpec;


    /*
        regex - regular expression in javascript syntax
        error_text - message displayed if the input does not statisfy this constraint
        match - set to 1 to check if the input matches this regex, set to 0 to check
                if input does not match this regex.  default is 1
    */
    typedef structure {
        string regex;
        string error_text;
        boolean match;
    } RegexMatcher;

    /*
        valid_ws_types  - list of valid ws types that can be used for input
        validate_as     - int | float | nonnumeric | none
        is_output_name  - true if the user is specifying an output name, false otherwise, default is false
    */
    typedef structure {
        list <string> valid_ws_types;
        string validate_as;
        boolean is_output_name;
        string placeholder;
        int min_int;
        int max_int;
        float min_float;
        float max_float;
        list <RegexMatcher> regex_constraint;
    } TextOptions;

    typedef structure {
        int n_rows;
        string placeholder;
    } TextAreaOptions;

    typedef structure {
        int min;
        int max;
        int step;
    } IntSliderOptions;

    typedef structure {
        float min;
        float max;
    } FloatSliderOptions;

    typedef structure {
        int checked_value;
        int unchecked_value;
    } CheckboxOptions;

    /*
       value is what is passed from the form, display is how the selection is
       shown to the user
    */
    typedef structure {
        string value;
        string display;
    } DropdownOption;


    /*
     Defines a parameter field that allows users to select from a list of options. It will
     appear as a dropdown (a 'select' HTML element).

     Parameters:

        options   - a list of maps with keys 'value' and 'display'; 'display' is the text
                  presented to the user, and 'value' is what is passed from the element
                  when it is submitted. See the DropDownOption type for the spec.

        multiselection - If true, multiple selections are allowed from a single field, and
                  the parameter will return a list, rather than a single value.
                  This parameter is optional.
                  Default = false
     */

    typedef structure {
        list<DropdownOption> options;
        boolean multiselection;
    } DropdownOptions;

    typedef structure {
        list<string> id_order;
        mapping<string,string> ids_to_options;
        mapping<string,string> ids_to_tooltip;
    } RadioOptions;

    typedef structure {
        list<string> tab_id_order;
        mapping<string,string> tab_id_to_tab_name;
        mapping<string,list<string>> tab_id_to_param_ids;
    } TabOptions;

    /*
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
            service_function - optional name of SDK method including prefix with SDK
                           module started up as dynamic service (it's fully qualified
                           method name where module and method are separated by '.')
            service_version - optional version of module used in service_function
                           (default value is 'release').
    */
    typedef structure {
        list <string> constant_ref;
        string parameter_id;
        list <string> subdata_included;
        list <string> path_to_subdata;
        string selection_id;
        list <string> selection_description;
        string description_template;
        string service_function;
        string service_version;
    } SubdataSelection;

    /*
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

    */
    typedef structure {
        string placeholder;
        boolean multiselection;
        boolean show_src_obj;
        boolean allow_custom;
        SubdataSelection subdata_selection;
    } TextSubdataOptions;

    /*
        Defines a parameter field that allows autocomplete based on a call to a dynamic service.
        For instance, selection of files from the staging_service or from kbase_search. It will
        appear as a text field with dropdown similar to selection of other WS data objects.

            data_source - one of ftp_staging | search | custom. Provides sensible defaults to
                           for the following parameters for a common type of dropdown which can be
                           overwritten

            service_function - name of SDK method including prefix with SDK module started up as
                           dynamic service (it's fully qualified method name where module and
                           method are separated by '.').

            service_version - optional version of module used in service_function
                           (default value is 'release').

            service_params - The parameters that will be supplied to the dynamic service call as
                           JSON. The special text "{{dynamic_dropdown_input}}" will be replaced by
                           the value of user input at call time.

            selection_id - The value of this key will be extracted from the item selected by the
                           user. The item is expected to be represented as a map.

            exact_match_on - if exactly matching the user's input to the results from the dynamic
                           service is required, this field contains the name of the key in
                           the results document that contains the value to which the user's
                           input should be matched. May or may not be the same key as
                           'selection_id'.

            description_template - Defines how the description of items is rendered using
                           Handlebar templates (use the keys in the items as variable names)

            multiselection - If true, then multiple selections are allowed in a single input field.
                           This will override the allow_multiple option (which allows user addition)
                           of additional fields.  If true, then this parameter will return a list.
                           Default= false

            query_on_empty_input - true, the default, to send a request to the dynamic service even
                           if there is no input.

            result_array_index - The index of the result array returned from the dynamic service
                           from where the selection items will be extracted. Default 0.

            path_to_selection_items - The path into the result data object to the list of
                           selection items. If missing, the data at the specified result array
                           index is used (defaulting to the first returned value in the list).

            The selection items data structure must be a list of mappings or structures.

            As an example of correctly specifying where the selection items are within the
            data structure returned from the dynamic service, if the data structure is:

            [
                "foo",                # return array position 0
                {                     # return array position 1
                 "interesting_data":
                     [
                         "baz",
                         "boo",
                         [
                             {"id": 1,
                              "name": "foo"
                              },
                              ...
                             {"id": 42,
                              "name": "wowbagger"
                              }
                         ],
                         "bat"
                     ]
                 },
                 "bar"                # return array position 2
             ]

            Note that KBase dynamic services all return an array of values, even for single-value
            returns, as the KIDL spec allows specifying multiple return values per function.

            In this case:
                result_array_index would be 1
                path_to_selection_items would be ["interesting_data", "2"]
                selection_id would be "name"

            The selection items would be the 42 items represented by
            {"id": 1,
             "name": "foo"
             },
             ...
            {"id": 42,
             "name": "wowbagger"
             }

            Selection items must always be a list of maps.

            The final value returned when the user selects a value would be the "name" field -
            "foo" if the first item is selected, and "wowbagger" if the last item is selected.

    */

    typedef structure {
        string data_source;
        string service_function;
        string service_version;
        UnspecifiedObject service_params;
        string selection_id;
        string exact_match_on;
        string description_template;
        boolean multiselection;
        boolean query_on_empty_input;
        int result_array_index;
        list<string> path_to_selection_items;
    } DynamicDropdownOptions;


    /*
        Description of a method parameter.

        id - id of the parameter, must be unique within the method
        ui_name - short name that is displayed to the user
        short_hint - short phrase or sentence describing the parameter
        description - longer and more technical description of the parameter
        field_type - one of: text | textarea | textsubdata | intslider | floatslider | checkbox |
                     dropdown | radio | tab | file | dynamic_dropdown
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
                   
        valid_file_types - a list of staging area file types that are valid for the method
            parameter. This might apply to a text box, dropdown, dynamic dropdown, etc. depending
            on the context. The file type is available in the mappings key of the json response
            from staging service importer mappings endpoint. Each mapping has a file_type key
            containing the type.

        @optional text_options textarea_options intslider_options floatslider_options
        @optional checkbox_options dropdown_options radio_options tab_options dynamic_dropdown_options
    */
    typedef structure {
        string id;
        string ui_name;
        string short_hint;
        string description;
        string field_type;
        boolean allow_multiple;
        boolean optional;
        boolean advanced;
        boolean disabled;

        string ui_class;

        list<string> default_values;
        list<string> valid_file_types;

        TextOptions text_options;
        TextAreaOptions textarea_options;
        IntSliderOptions intslider_options;
        FloatSliderOptions floatslider_options;
        CheckboxOptions checkbox_options;
        DropdownOptions dropdown_options;
        DynamicDropdownOptions dynamic_dropdown_options;
        RadioOptions radio_options;
        TabOptions tab_options;
        TextSubdataOptions textsubdata_options;
    } MethodParameter;

    /* a fixed parameter that does not appear in the method input forms, but is informational for users in describing
    a backend parameter that cannot be changed (e.g. if a service picks a fixed parameter for say Blast) */
    typedef structure {
        string ui_name;
        string description;
    } FixedMethodParameter;

    /*
    	prefix - optional string concatenated before generated part
    	symbols - number of generated characters, optional, default is 8
    	suffix - optional string concatenated after generated part
    	@optional prefix symbols suffix
    */
    typedef structure {
        string prefix;
        int symbols;
        string suffix;
    } AutoGeneratedValue;

    /*
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
        target_property - name of field inside structure that will be send as argument. Optional field,
            in case this field is not defined (or null) whole object will be sent as method argument instead of
            wrapping it by structure with inner property defined by 'target_property'.
        target_type_transform - none/string/int/float/ref, optional field, default is 'none' (it's in plans to
            support list<type>, mapping<type> and tuple<t1,t2,...> transformations).
        @optional input_parameter constant_value narrative_system_variable generated_value
        @optional target_argument_position target_property target_type_transform
    */
    typedef structure {
        string input_parameter;
        UnspecifiedObject constant_value;
        string narrative_system_variable;
        AutoGeneratedValue generated_value;
        int target_argument_position;
        string target_property;
        string target_type_transform;
    } ServiceMethodInputMapping;

    /*
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
    */
    typedef structure {
        string input_parameter;
        list<string> service_method_output_path;
        UnspecifiedObject constant_value;
        string narrative_system_variable;
        string target_property;
        string target_type_transform;
    } ServiceMethodOutputMapping;

    /* This structure should be used in case narrative method doesn't run any back-end code.
    	See docs for ServiceMethodOutputMapping type for details.
    */
    typedef structure {
        string input_parameter;
        UnspecifiedObject constant_value;
        string narrative_system_variable;
        string target_property;
        string target_type_transform;
    } OutputMapping;

    /*
        Determines how the method is handled when run.
        kb_service_name - name of service which will be part of fully qualified method name, optional field (in
            case it's not defined developer should enter fully qualified name with dot into 'kb_service_method'.
        kb_service_version - optional git commit hash defining version of repo registered dynamically.
        kb_service_input_mapping - mapping from input parameters to input service method arguments.
        kb_service_output_mapping - mapping from output of service method to final output of narrative method.
        resource_estimator_module - optional module for the resource estimator method.
        resource_estimator_method - optional name of method for estimating resource requirements.
        output_mapping - mapping from input to final output of narrative method to support steps without back-end operations.
        @optional kb_service_name kb_service_method kb_service_input_mapping kb_service_output_mapping resource_estimator_module resource_estimator_method
    */
    typedef structure {
        string kb_service_url;
        string kb_service_name;
        string kb_service_version;
        string kb_service_method;
        string resource_estimator_module;
        string resource_estimator_method;
        list<ServiceMethodInputMapping> kb_service_input_mapping;
        list<ServiceMethodOutputMapping> kb_service_output_mapping;
        list<OutputMapping> output_mapping;
    } MethodBehavior;

    /*
        Description of a method parameter.

        id - id of the parameter group, must be unique within the method among all parameters
                        and groups,
        parameter_ids - IDs of parameters included in this group,
        ui_name - short name that is displayed to the user,
        short_hint - short phrase or sentence describing the parameter group,
        description - longer and more technical description of the parameter group (long-hint),
        allow_mutiple - allows entry of a list instead of a single structure, default is 0
                        if set, the number of starting boxes will be either 1 or the
                        number of elements in the default_values list,
        optional - set to true to make the group optional, default is 0,
        advanced - set to true to make this an advanced option, default is 0
                        if an option is advanced, it should also be optional or have
                        a default value,
        id_mapping - optional mapping for parameter IDs used to pack group into resulting
                        value structure (not used for non-multiple groups),
        with_border - flag for one-copy groups saying to show these group with border.

        @optional id_mapping
    */
    typedef structure {
        string id;
        list<string> parameter_ids;
        string ui_name;
        string short_hint;
        string description;
        boolean allow_multiple;
        boolean optional;
        boolean advanced;
        mapping<string, string> id_mapping;
        boolean with_border;
    } MethodParameterGroup;

    /*
        The method specification which should provide enough information to render a default
        input widget for the method.

        replacement_text indicates the text that should replace the input boxes after the method
        has run.  You can refer to parameters by putting them in double curly braces (on the front
        end we will use the handlebars library).
           for example:  Ran flux balance analysis on model {{model_param}} with parameter 2 set to {{param2}}.

    */
    typedef structure {
        MethodBriefInfo info;

        string replacement_text;

        WidgetSpec widgets;
        list<MethodParameter> parameters;

        list<FixedMethodParameter> fixed_parameters;

        list<MethodParameterGroup> parameter_groups;

        MethodBehavior behavior;

        string job_id_output_field;
    } MethodSpec;



    typedef structure {
        string id;
        string name;
        string ver;
        string subtitle;
        string tooltip;
        string header;
        Icon icon;
        list<string> categories;
        string loading_error;
    } AppBriefInfo;

    typedef structure {
        string id;
        string name;
        string ver;
        list <username> authors;
        email contact;

        string subtitle;
        string tooltip;

        string header;

        string description;
        string technical_description;

        Suggestions suggestions;

        list<string> categories;

        Icon icon;
        list<ScreenShot> screenshots;
    } AppFullInfo;

    /*
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
    */
    typedef structure {
        string step_source;
        boolean is_from_input;
        string from;
        string to;
    } AppStepInputMapping;

    typedef structure {
        string step_id;
        string method_id;
        list<AppStepInputMapping> input_mapping;
        string description;
    } AppSteps;

    /* typedef structure {

    } AppBehavior; */

    typedef structure {
        AppBriefInfo info;

        list<AppSteps> steps;

    } AppSpec;

	/*
	    export_functions - optional mapping from UI label to exporter SDK local function.
	    @optional icon landing_page_url_prefix loading_error
	*/
    typedef structure {
        string type_name;
        string name;
        string subtitle;
        string tooltip;
        string description;
        ScreenShot icon;
        list<string> view_method_ids;
        list<string> import_method_ids;
        mapping<string, string> export_functions;
        string landing_page_url_prefix;
        string loading_error;
    } TypeInfo;


    /*
        List all the categories.  Optionally, if load_methods or load_apps are set to 1,
        information about all the methods and apps is provided.  This is important
        load_methods - optional field (default value is 1).
        tag - optional access level for dynamic repos (one of 'dev', 'beta' or 'release').
    */
    typedef structure {
        boolean load_methods;
        boolean load_apps;
        boolean load_types;
        string tag;
    } ListCategoriesParams;

    funcdef list_categories(ListCategoriesParams params)
                returns ( mapping<string, Category> categories,
                          mapping<string, MethodBriefInfo> methods,
                          mapping<string, AppBriefInfo> apps,
                          mapping<string, TypeInfo> types);

    typedef structure {
        list <string> ids;
    } GetCategoryParams;

    funcdef get_category(GetCategoryParams params) returns (list<Category>);

    /*
        These parameters do nothing currently, but are a placeholder for future options
        on listing methods or apps
        limit - optional field (default value is 0)
        offset - optional field (default value is 0)
        tag - optional access level for dynamic repos (one of 'dev', 'beta' or 'release').
    */
    typedef structure {
        int limit;
        int offset;
        string tag;
    } ListParams;

    funcdef list_methods(ListParams params) returns (list<MethodBriefInfo>);

    funcdef list_methods_full_info(ListParams params) returns (list<MethodFullInfo>);

    funcdef list_methods_spec(ListParams params) returns (list<MethodSpec>);

    /*
        tag - optional access level for dynamic repos (one of 'dev', 'beta' or 'release').
    */
    typedef structure {
        string tag;
    } ListMethodIdsAndNamesParams;

    funcdef list_method_ids_and_names(ListMethodIdsAndNamesParams params) returns (mapping<string,string>);


    funcdef list_apps(ListParams params) returns (list<AppBriefInfo>);

    funcdef list_apps_full_info(ListParams params) returns (list<AppFullInfo>);

    funcdef list_apps_spec(ListParams params) returns (list<AppSpec>);

    funcdef list_app_ids_and_names() returns (mapping<string,string>);

    funcdef list_types(ListParams params) returns (list<TypeInfo>);

    /*
        tag - optional access level for dynamic repos (one of 'dev', 'beta' or 'release').
    */
    typedef structure {
        list <string> ids;
        string tag;
    } GetMethodParams;

    funcdef get_method_brief_info(GetMethodParams params) returns (list<MethodBriefInfo>);

    funcdef get_method_full_info(GetMethodParams params) returns (list<MethodFullInfo>);

    funcdef get_method_spec(GetMethodParams params) returns (list<MethodSpec>);



    typedef structure {
        list <string> ids;
    } GetAppParams;

    funcdef get_app_brief_info(GetAppParams params) returns (list<AppBriefInfo>);

    funcdef get_app_full_info(GetAppParams params) returns (list<AppFullInfo>);

    funcdef get_app_spec(GetAppParams params) returns (list<AppSpec>);


    typedef structure {
        list <string> type_names;
    } GetTypeParams;

    funcdef get_type_info(GetTypeParams params) returns (list<TypeInfo>);



    /*
        verbose - flag for adding more details into error messages (like stack traces).
    */
    typedef structure {
    	string id;
    	string spec_json;
    	string display_yaml;
    	mapping <string,string> extra_files;
    	boolean verbose;
    } ValidateMethodParams;

    typedef structure {
    	boolean is_valid;
        list<string> errors;
        list<string> warnings;
        AppFullInfo app_full_info;
        AppSpec app_spec;
        MethodFullInfo method_full_info;
        MethodSpec method_spec;
        TypeInfo type_info;
    } ValidationResults;

    funcdef validate_method(ValidateMethodParams params) returns (ValidationResults);

    typedef structure {
    	string id;
    	string spec_json;
    	string display_yaml;
    	mapping <string,string> extra_files;
    } ValidateAppParams;

    funcdef validate_app(ValidateAppParams params) returns (ValidationResults);


    typedef structure {
    	string id;
    	string spec_json;
    	string display_yaml;
    	mapping <string,string> extra_files;
    } ValidateTypeParams;

    funcdef validate_type(ValidateTypeParams params) returns (ValidationResults);

    /* need to add category validation as well */

    /*
        Describes how to find repository widget JavaScript.
        module_name - name of module defined in kbase.yaml;
        version - optional parameter limiting search by certain version timestamp;
        widget_id - name of java-script file stored in repo's 'ui/widgets' folder.
        tag - optional access level for dynamic repos (one of 'dev', 'beta', 'release').
    */
    typedef structure {
        string module_name;
        int version;
        string widget_id;
        string tag;
    } LoadWidgetParams;

    funcdef load_widget_java_script(LoadWidgetParams params) returns (string
        java_script);

    /****************************** Dynamic Repos API *******************************/

    typedef structure {
        string git_url;
        string git_commit_hash;
    } RegisterRepoParams;

    funcdef register_repo(RegisterRepoParams params) returns () authentication
        required;

    typedef structure {
        string module_name;
    } DisableRepoParams;

    funcdef disable_repo(DisableRepoParams params) returns () authentication
        required;

    typedef structure {
        string module_name;
    } EnableRepoParams;

    funcdef enable_repo(EnableRepoParams params) returns () authentication
        required;

    /*
        tag - one of two values: 'beta' or 'release'.
    */
    typedef structure {
        string module_name;
        string tag;
    } PushRepoToTagParams;

    funcdef push_repo_to_tag(PushRepoToTagParams params) returns ()
        authentication required;

};
