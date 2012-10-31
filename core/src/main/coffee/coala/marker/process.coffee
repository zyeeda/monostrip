{type} = require 'coala/util'
{createService} = require 'coala/process-service'

exports.handler = (context, attributes, fn, args) ->
    args.unshift createService()
    fn.apply null, args
