/*

*/
module NarrativeMethodStore {

    /* Returns the current running version of the NarrativeMethodStore. */
    funcdef ver() returns (string);

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

    /* Minimal information about a method suitable for displaying the method in a menu or navigator. */
    typedef structure {
        string id;
        string name;
        string ver;
        string subtitle;
        string tooltip;
        list<string> categories;
		string loading_error;
    } MethodBriefInfo;
    
    typedef structure {
        url url;
    } ScreenShot;
    
    /* Full information about a method suitable for displaying a method landing page. */
    typedef structure {
        string id;
        string name;
        string ver;
        list <username> authors;
        email contact;
        
        string subtitle;
        string tooltip;
        string description;
        string technical_description;
        
        list<string> categories;
        
        list<ScreenShot> screenshots;
        
    } MethodFullInfo;

    /* specify the input / ouput widgets used for rendering */
    typedef structure {
        string input;
        string output;
    } WidgetSpec;




    /*
        valid_ws_types  - list of valid ws types that can be used for input
        validate_as     - int | float | nonnumeric | none
    */
    typedef structure {
        list <string> valid_ws_types;
        string validate_as;
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
    
    typedef structure {
        mapping<string,string> ids_to_options;
    } DropdownOptions;
    
    typedef structure {
        mapping<string,string> ids_to_options;
        mapping<string,string> ids_to_tooltip;
    } RadioOptions;

    /*
        Description of a method parameter.
        @optional text_options textarea_options intslider_options checkbox_options
        @optional dropdown_options radio_options
    */
    typedef structure {
        string id;
        string ui_name;
        string short_hint;
        string long_hint;
        string field_type;
        boolean allow_multiple;
        boolean optional;
        boolean advanced;
        
        list<string> default_values;
        
        TextOptions text_options;
        TextAreaOptions textarea_options;
        IntSliderOptions intslider_options;
        FloatSliderOptions floatslider_options;
        CheckboxOptions checkbox_options;
        DropdownOptions dropdown_options;
        RadioOptions radio_options;
        
    } MethodParameter;
    
    /*
        target_argument_position - position of argument in RPC-method call, optional field, default value is 0.
        target_property - name of field inside structure that will be send as arguement. Optional field,
            in case this field is not defined (or null) whole object will be sent as method argument instead of
            wrapping it by structure with inner property defined by 'target_property'.
        target_type_transform - none/string/int/float/list<type>/mapping<type>/ref, optional field, default is 
            no transformation.
        @optional target_argument_position target_property target_type_transform
    */
    typedef structure {
        int target_argument_position;
        string target_property;
        string target_type_transform;
    } MethodParameterMapping;

    /*
        Determines how the method is handled when run.
        kb_service_name - name of service which will be part of fully qualified method name, optional field (in
            case it's not defined developer should enter fully qualified name with dot into 'kb_service_method'.
        kb_service_parameters_mapping - mapping from parameter_id to service method arguments (in case
            mapping is not described for some parameter it will be mapped into structure with target_property
            equal to parameter id.
        @optional python_function kb_service_name kb_service_method kb_service_parameters_mapping kb_service_workspace_name_mapping
    */
    typedef structure {
        string python_class;
        string python_function;
        string kb_service_url;
        string kb_service_name;
        string kb_service_method;
        mapping<string, MethodParameterMapping> kb_service_parameters_mapping;
        MethodParameterMapping kb_service_workspace_name_mapping;
    } MethodBehavior;

    /*
        The method specification which should provide enough information to render a default
        input widget for the method.
    */
    typedef structure {
        MethodBriefInfo info;
        
        WidgetSpec widgets;
        list<MethodParameter> parameters;
        
        MethodBehavior behavior;

        string job_id_output_field;
    } MethodSpec;


	/*
		load_methods - optional field (default value is 1)
	*/
	typedef structure {
		boolean load_methods;
	} ListCategoriesParams;

	funcdef list_categories(ListCategoriesParams params) 
		returns (mapping<string, Category> categories, mapping<string, MethodBriefInfo> methods);

    typedef structure {
        list <string> ids;
    } GetCategoryParams;

    funcdef get_category(GetCategoryParams params) returns (list<Category>);

	/*
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
    
    
    typedef structure {
        list <string> ids;
    } GetMethodParams;

    funcdef get_method_brief_info(GetMethodParams params) returns (list<MethodBriefInfo>);
    
    funcdef get_method_full_info(GetMethodParams params) returns (list<MethodFullInfo>);
    
    funcdef get_method_spec(GetMethodParams params) returns (list<MethodSpec>);

};