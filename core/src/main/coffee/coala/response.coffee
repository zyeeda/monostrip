
fs          = require 'fs'
handlebars  = require 'handlebars'

res = require 'ringo/jsgi/response'
log = require('ringo/logging').getLogger module.id

{type} = require 'coala/util/type'
objects = require 'coala/util/objects'
{coala}         = require 'coala/config'
{objectMapper}  = require 'coala/util/jackson'

{SimpleFilterProvider}      = com.fasterxml.jackson.databind.ser.impl
{SimpleBeanPropertyFilter}  = com.zyeeda.coala.jackson

charset = 'utf-8'

exports.charset = (c) ->
    charset = c

contentType = (type) ->
    'Content-Type': if charset then "#{type}; charset=#{charset}" else type

exports.redirect = res.redirect

exports.html = (args...) ->
    result =
        status: 200
        headers: contentType 'text/html'
        body: args

    if args.length is 1 and type(args[0]) is 'object'
        config = args[0]
        (result[name] = config[name] if config[name]) for name of result

    result.body = [result.body] if type(result.body) isnt 'array'

    result

exports.xml = (args...) ->
    result =
        status: 200
        headers: contentType 'application/xml'
        body: args

    if args.length is 1
        if typeof args[0] is 'xml'
            result.body = args[0].toXmlString()
        else if type(args[0]) is 'object'
            (result[name] = args[0][name] if args[0][name]) for name of result

    result.body = [result.body] if type(result.body) isnt 'array'

    result

exports.template = (path, params) ->
    repo = this.getRepository('./').getRootRepository()
    res = repo.getResource(path)
    if res.exists()
        template = handlebars.compile res.getContent('UTF-8')
        exports.html template params
    else
        exports.notFound 'template:', path, ' is not exists'

###
generate json response, support three ways:

the first, filters configurated by a config object
response.json(object,{
    include: {
        filter1: ['field1','field2'],
        filter2: 'field3'
    },
    exclude: {
        filter3: ['field4','field5'],
        filter4:'field6'
    }
});

the second, chained include/exclude method.
response.json(object)
    .include('filter1',['field1','field2']).include('filter2','field3')
    .exclude('filter3',['field4','field5']).exclude('filter4','field6');

the third, chained add method, use the third argument to identify include(default) or exclude( value:'exclude','e')
response.json(object)
    .add('filter1',['field1','field2']).add('filter2','field3')
    .add('filter3',['field4','field5'],'exclude').add('filter4','field6','e');
###
exports.json = (object, config) ->

    result =
        _object: object,
        _included: {},
        _excluded: {},
        _jsonResult: null,
        _isStream: false,

        status: 200,
        headers: contentType 'application/json'

        add: (filter, fields, type) ->
            target = if type is 'exclude' then @_excluded else @_included
            fields = if Array.isArray fields then fields else [fields]

            filters = target[filter]
            target[filter] = if filters then filters.concat filelds else fields
            @

        include: (filter, fields) ->
            @add filter, fields, 'include'

        exclude: (filter, fields) ->
            @add filter, fields, 'exclude'

        forEach: (fn) ->
            return if @_isStream is true

            if @_jsonResult is null
                log.debug 'Cached json result is null, generate it.'
                writer = getObjectWriter @_included, @_excluded
                # mapper = buildObjectMapper @_included, @_excluded, result
                @_jsonResult = writer.writeValueAsString @_object
                log.debug "Generation finished. json result = #{@_jsonResult}"
            else
                log.debug "Using cached json result. json result = #{@_jsonResult}"

            ###
            unless @_jsonResult?
                mapper = buildObjectMapper @_included, @_excluded, result
                @_jsonResult = mapper.writeValueAsString @_object
                log.debug "generate json, json result:#{@_jsonResult}"
            log.debug "forEach called, json result:#{@_jsonResult}"
            ###
            fn @_jsonResult

        asStream: (request) ->
            @_isStream = true
            log.debug "headers = #{JSON.stringify @headers}"
            exports.stream request,
                headers: @headers
                status: @status
                body: (stream) =>
                    writer = getObjectWriter @_included, @_excluded
                    # mapper = buildObjectMapper @_included, @_excluded, result
                    writer.writeValue stream, @_object
                    return

    if config?
        result.status = config.status if config.status?
        result.headers = objects.extend result.headers, config.headers if config.headers?
        delete config.status
        delete config.headers

        if config.include or config.exclude
            result.include k, v for k, v of config.include
            result.exclude k, v for k, v of config.exclude
        else
            (if k.charAt(0) is '!' then result.exclude k.substring(1), v else result.include k, v) for k, v of config
        # result.status = config.status or result.status
        # result.headers = objects.extend result.headers, config.headers or {}
        # result.dateFormat = config.dateFormat

    result.body = result
    result

getObjectWriter = (included, excluded, result) ->
    filter = new SimpleFilterProvider()

    for k, v of included
        throw new Error("Filter '#{key}' exists in both included and excluded configuration.") if k in excluded
        filter.addFilter k, SimpleBeanPropertyFilter.filterOutAllExcept v

    filter.addFilter k, SimpleBeanPropertyFilter.serializeAllExcept v for k, v of excluded

    objectMapper.writer filter

exports.stream = (request, callback) ->
    unless request? and request.env? and (type(callback) is 'function' or type(callback) is 'object')
        throw new Error('invalid arguments')

    status = 200
    headers =
        'Content-Type': 'binary/octet-stream'
    log.debug "callback: #{JSON.stringify callback}"

    if type(callback) is 'object'
        status = callback.status or status
        headers = objects.extend headers, callback.headers or {}
        callback = if type(callback.body) is 'function' then callback.body else ->

    servletResponse = request.env.servletResponse
    servletResponse.setStatus status

    for key, value of headers
        if type(value) is 'string'
            value = value.split '\n'
        return unless Array.isArray value
        servletResponse.addHeader key, n for n in value

    try
        callback servletResponse.getOutputStream()
    catch error
        return exports.internalServerError error

    status: status
    headers:
        'X-JSGI-Skip-Response': true
    body: []

###
errors =
    items: []

    append: (item) ->
        @items.push item

    size : ->
        @items.length

    collect: (asJson) ->
        return true if @items.length is 0

        items = @items
        @items = []
        result = { errors : items }
        result = exports.json result if asJson is true
        result

exports.error = (args...) ->
    errors.append arg for arg in args

    errors
###

exports.notFound = (args...) ->
    status: 404
    headers: contentType 'text/html'
    body: args

exports.internalServerError = (args...) ->
    status: 500
    headers: contentType 'text/html'
    body: args
