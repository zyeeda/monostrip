{createService} = require 'coala/scaffold/process-service'

exports.handler = (context, attributes, fn, args) ->
    args.unshift createService()
    fn.apply null, args
