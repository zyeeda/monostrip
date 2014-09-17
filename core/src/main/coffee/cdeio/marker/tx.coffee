{tx} = require 'cdeio/tx'
_ = require 'underscore'

exports.handler = (context, attributes, fn, args) ->
    options = attributes[0] or {}
    needStatus = options.needStatus
    options.callback = (status) ->
        args.unshift status if needStatus is true
        fn.apply null, args
    tx _.extend({}, options)
