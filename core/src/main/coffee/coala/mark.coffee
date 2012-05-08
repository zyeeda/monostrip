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

{objects,type} = require 'coala/util'

handlers =
    tx: txAnnotationHandler

    services: servicesAnnotationHandler

    managers: (context, attributes, fn, args) ->
        if attributes
            service = createService()
            attr = if type(attributes) == 'array' then attributes else [attributes]
            managers = []

            managers.push service.createManager clazz for clazz in managers
            args = managers.concat args
        fn.apply null, args

    inject: (context, attributes, fn, args) ->
        if attributes
            attr = if type(attributes) == 'array' then attributes else [attributes]
            beans = []
            beans.push((if type(name) == 'string' then context.getBean else context.getBeanByClass)(name)) for name in attr
            args = beans.concat args
        fn.apply null, args

obj =
    annos: []
    keys: []
    ###
    # parameter name is the annotation's name, it is used to find the annotation handler
    # parameter attributes will pass into the handler which is found by name
    ###
    mark: (name, attributes) ->
        throw new Error('one annotation once') if obj.keys.indexOf(name) isnt -1
        throw new Error("annotation #{name} is not supported") unless name of handlers
        obj.annos.push {attributes: attributes, name: name}
        obj.keys.push name
        obj

    ###
    # the end of the at chain, returns an function which wrapped the argument fn
    ###
    on: (fn, me) ->
        result = (anno for anno in obj.annos)
        obj.annos = []
        obj.keys = []

        context = Context.getInstance()
        result.reduce ((memo, anno) ->
            handler = handlers[anno.name]
            attributes = anno.attributes
            (args...) ->
                handler.apply(null,[context, attributes, memo, args])), fn.bind me

exports.mark = obj.mark

exports[name] = obj.mark.bind obj, name for name of handlers
