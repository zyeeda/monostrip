###
# this module aims to support java-annotation-like features
# just like this:
# app.get(mark('services',['someservice','anotherservice']).on(function(){}));
#
# if annotation handler want pass arguments to function must use prepend
# the execution of annotations if first-mark-last-run
# the sequence of arguments which passed by handlers is first-mark-first-get
# mark('a').mark('b').mark('c').on(function(a, b, c){})
# execute sequence is c -> b ->a
# arguments sequence is a, b, c
###
{Context} = com.zyeeda.cdeio.web.SpringAwareJsgiServlet

{type} = require 'cdeio/util/type'
_ = require 'underscore'
log = require('ringo/logging').getLogger module.id

handlers = {}
###
tx: require('cdeio/marker/tx').handler
beans: require('cdeio/marker/beans').handler
services: require('cdeio/marker/services').handler
managers: require('cdeio/marker/managers').handler
process: require('cdeio/marker/process').handler
knowledge: require('cdeio/marker/knowledge').handler
###

obj =
    ###
    # parameter name is the annotation's name, it is used to find the annotation handler
    # parameter attributes will pass into the handler which is found by name
    ###
    mark: (name, attributes...) ->
        throw new Error("one annotation once, keys: #{@keys}, name: #{name}") if @keys.indexOf(name) isnt -1
        attr = _.flatten attributes
        @annos.push {attributes: attr, name: name}
        @keys.push name
        @

    ###
    # the end of the at chain, returns an function which wrapped the argument fn
    ###
    on: (fn, me) ->
        result = (anno for anno in @annos)
        @annos = []
        @keys = []

        context = Context.getInstance(module)
        result.reduce ((memo, anno) ->
            ((ctx, anno, me, args...) ->
                handler = handlers[anno.name]
                att = anno.attributes
                log.debug "invoke handler: #{anno.name}: #{handler}"
                throw new Error("no handler is named #{anno.name}") unless handler
                throw new Error("handler #{anno.name} is not a function") unless _.isFunction handler
                handler.apply(null,[ctx, att, me, args])
            ).bind null, context, anno, memo
        ), fn.bind me

exports.mark = (args...) ->
    o = _.extend annos: [], keys: [], obj
    o.mark args...
exports.registerHandler = (name, fn) ->
    log.debug "register handler #{name}"
    handlers[name] = fn

exports[name] = exports.mark.bind exports, name for name of handlers
