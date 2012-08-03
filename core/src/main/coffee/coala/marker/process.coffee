# support mark 'process'
# to inject specified process
# attributes should be a process's module id or a collection of it
# ex. :
# mark('process', ['coala/process', 'someModule/process']).on(function(baseProcess, moduleProcess){});

{type} = require 'coala/util'
{createService} = require 'coala/process-service'

exports.handler = (context, attributes, fn, args) ->
    # if attributes
    #     attributes = [attributes] if type(attributes) is 'string'
    #     throw new Error('attributes must be a string or an string array') if type(attributes) isnt 'array'
    #     processes = (require(m).createService() for m in attributes)
    args.push createService()
    fn.apply null, args
