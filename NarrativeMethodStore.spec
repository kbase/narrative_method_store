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

    /* Organization of where in a menu the method should appear */
    typedef list<string> categorization;
    
    /* Minimal information about a method suitable for displaying the method in a menu or navigator. */
    typedef structure {
        string id;
        string name;
        string ver;
        string subtitle;
        string tooltip;
        list<categorization> categorizations;
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
        
        list<categorization> categorizations;
        
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
        Determines how the method is handled when run.
        @optional python_function kb_service_name kb_service_method
    */
    typedef structure {
        string python_function;
        string kb_service_name;
        string kb_service_method;
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
    } MethodSpec;




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