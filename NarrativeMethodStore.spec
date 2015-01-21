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
    
    /* Minimal information about a method suitable for displaying the method in a menu or navigator. */
    typedef structure {
        string id;
        string name;
        string ver;
        string subtitle;
        string tooltip;
        Icon icon;
        list<string> categories;
        string loading_error;
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
    
    /* Full information about a method suitable for displaying a method landing page. */
    typedef structure {
        string id;
        string name;
        string ver;
        list <username> authors;
        list <username> kb_contributors;
        email contact;
        
        string subtitle;
        string tooltip;
        string description;
        string technical_description;
        
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
    
    typedef structure {
        list<DropdownOption> options;
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
        Description of a method parameter.
        
        id - id of the parameter, must be unique within the method
        ui_name - short name that is displayed to the user
        short_hint - short phrase or sentence describing the parameter
        description - longer and more technical description of the parameter
        field_type - one of: text | textarea | intslider | floatslider | checkbox | 
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
        
        TextOptions text_options;
        TextAreaOptions textarea_options;
        IntSliderOptions intslider_options;
        FloatSliderOptions floatslider_options;
        CheckboxOptions checkbox_options;
        DropdownOptions dropdown_options;
        RadioOptions radio_options;
        TabOptions tab_options;
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
        target_property - name of field inside structure that will be send as arguement. Optional field,
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
    */
    typedef structure {
        string input_parameter;
        UnspecifiedObject constant_value;
        string narrative_system_variable;
        AutoGeneratedValue generated_value;
        string target_property;
        string target_type_transform;
    } ScriptInputMapping;

    /*
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
    */
    typedef structure {
        string input_parameter;
        list<string> script_output_path;
        UnspecifiedObject constant_value;
        string narrative_system_variable;
        string target_property;
        string target_type_transform;
    } ScriptOutputMapping;

    /*
        Determines how the method is handled when run.
        kb_service_name - name of service which will be part of fully qualified method name, optional field (in
            case it's not defined developer should enter fully qualified name with dot into 'kb_service_method'.
        kb_service_input_mapping - mapping from input parameters to input service method arguments.
        kb_service_output_mapping - mapping from output of service method to final output of narrative method.
        output_mapping - mapping from input to final output of narrative method to support steps without back-end operations.
        kb_service_input_mapping - mapping from input parameters to input service method arguments.
        kb_service_output_mapping - mapping from output of service method to final output of narrative method.
        @optional python_function kb_service_name kb_service_method kb_service_input_mapping kb_service_output_mapping
    */
    typedef structure {
        string python_class;
        string python_function;
        string kb_service_url;
        string kb_service_name;
        string kb_service_method;
        string script_module;
        string script_name;
        boolean script_has_files;
        list<ServiceMethodInputMapping> kb_service_input_mapping;
        list<ServiceMethodOutputMapping> kb_service_output_mapping;
        list<OutputMapping> output_mapping;
        list<ScriptInputMapping> script_input_mapping;
        list<ScriptOutputMapping> script_output_mapping;
    } MethodBehavior;

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
        string landing_page_url_prefix;
        string loading_error;
    } TypeInfo;


    /*
        List all the categories.  Optionally, if load_methods or load_apps are set to 1,
        information about all the methods and apps is provided.  This is important
        load_methods - optional field (default value is 1)
    */
    typedef structure {
        boolean load_methods;
        boolean load_apps;
        boolean load_types;
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
    */
    typedef structure {
        int limit;
        int offset;
    } ListParams;
    
    funcdef list_methods(ListParams params) returns (list<MethodBriefInfo>);
    
    funcdef list_methods_full_info(ListParams params) returns (list<MethodFullInfo>);
    
    funcdef list_methods_spec(ListParams params) returns (list<MethodSpec>);

    funcdef list_method_ids_and_names() returns (mapping<string,string>);
    
    
    funcdef list_apps(ListParams params) returns (list<AppBriefInfo>);
    
    funcdef list_apps_full_info(ListParams params) returns (list<AppFullInfo>);
    
    funcdef list_apps_spec(ListParams params) returns (list<AppSpec>);
    
    funcdef list_app_ids_and_names() returns (mapping<string,string>);
    
    funcdef list_types(ListParams params) returns (list<TypeInfo>);
    
    
    typedef structure {
        list <string> ids;
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

};
