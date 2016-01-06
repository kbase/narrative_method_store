#BEGIN_HEADER
#END_HEADER


class GenomeFeatureComparator:
    '''
    Module Name:
    GenomeFeatureComparator

    Module Description:
    
    '''

    ######## WARNING FOR GEVENT USERS #######
    # Since asynchronous IO can lead to methods - even the same method -
    # interrupting each other, you must be *very* careful when using global
    # state. A method could easily clobber the state set by another while
    # the latter method is running.
    #########################################
    #BEGIN_CLASS_HEADER
    #END_CLASS_HEADER

    # config contains contents of config file in a hash or None if it couldn't
    # be found
    def __init__(self, config):
        #BEGIN_CONSTRUCTOR
        #END_CONSTRUCTOR
        pass

    def compare_genome_features(self, ctx, params):
        # ctx is the context object
        # return variables are: returnVal
        #BEGIN compare_genome_features
        returnVal = {'params': params, 'token': ctx['token']}
        #END compare_genome_features

        # At some point might do deeper type checking...
        if not isinstance(returnVal, object):
            raise ValueError('Method compare_genome_features return value ' +
                             'returnVal is not type object as required.')
        # return the results
        return [returnVal]
