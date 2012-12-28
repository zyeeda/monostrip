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
{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet

{type} = require 'coala/util/type'
_ = require 'underscore'
log = require('ringo/logging').getLogger module.id

handlers =
    tx: require('coala/marker/tx').handler
    beans: require('coala/marker/beans').handler
    services: require('coala/marker/services').handler
    managers: require('coala/marker/managers').handler
    process: require('coala/marker/process').handler
    knowledge: require('coala/marker/knowledge').handler

loadExtraHandler = (moduleId) ->
    try
        m = require moduleId
        throw new Error("marker extension module:#{moduleId} has no exports named handler") unless m.handler

        unless type(m.handler) is 'function'
            throw new Error("marker extension module:#{moduleId} has export a handler which is not a function")

        return m.handler
    catch e
        throw new Error("marker extension module:#{moduleId} is not found")

obj =
    ###
    # parameter name is the annotation's name, it is used to find the annotation handler
    # parameter attributes will pass into the handler which is found by name
    ###
    mark: (name, attributes...) ->
        log.debug "Using #{name} marker, already used #{@keys}. #{attributes}"
        log.debug "obj.keys.indexOf('#{name}') = #{@keys.indexOf(name)}"
        throw new Error("one annotation once, keys: #{@keys}, name: #{name}") if @keys.indexOf(name) isnt -1
        throw new Error("annotation #{name} is not supported") unless name of handlers
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
            handler = handlers[anno.name] or loadExtraHandler anno.name
            attributes = anno.attributes
            ((ctx, att, me, args...) ->
                handler.apply(null,[ctx, att, me, args])
            ).bind null, context, attributes, memo
        ), fn.bind me

exports.mark = (args...) ->
    o = _.extend annos: [], keys: [], obj
    o.mark args...

exports[name] = exports.mark.bind exports, name for name of handlers
