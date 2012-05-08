{tx} = require 'coala/tx'

exports.handler = (context, attributes = {}, fn, args) ->
    needStatus = attributes.needStatus
    attributes.callback = (status) ->
        args.unshift status if needStatus is true
        fn.apply null, args
    tx attributes
