# support mark 'services'
# to inject specified services
# attributes should be a service's module id or a collection of it
# ex. :
# mark('services', ['coala/service', 'someModule/service']).on(function(baseService, moduleService){});

{type} = require 'coala/util'

exports.handler = (context, attributes, fn, args) ->
    if attributes
        attributes = [attributes] if type(attributes) is 'string'
        throw new Error('attributes must be a string or an string array') if type(attributes) isnt 'array'
        services = (require(m).createService() for m in attributes)
        args = services.concat args
    fn.apply null, args
