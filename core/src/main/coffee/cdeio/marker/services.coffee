# support mark 'services'
# to inject specified services
# attributes should be a service's module id or a collection of it
# ex. :
# mark('services', ['cdeio/service', 'someModule/service']).on(function(baseService, moduleService){});

{cdeio} = require 'cdeio/config'
paths = require 'cdeio/util/paths'

exports.handler = (context, attributes, fn, args) ->
    services = []
    for m in attributes
        names = m.split cdeio.servicePathSeperator
        name = names[0].replace(/(^\/)|(\/$)/g, '') + '.feature/service'
        name += "-#{names[1]}" if names.length > 1
        services.push require(name).createService()
    args = services.concat args
    fn.apply null, args
