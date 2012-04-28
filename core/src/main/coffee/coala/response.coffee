
res = require 'ringo/jsgi/response'
log = require('ringo/logging').getLogger module.id
{objects,type} = require 'coala/util'

{ObjectMapper} = org.codehaus.jackson.map
{SimpleFilterProvider, SimpleBeanPropertyFilter} = org.codehaus.jackson.map.ser.impl

exports.charset = res.charset

for name in ['html', 'xml', 'static', 'redirect', 'notFound', 'error']
    do (name) ->
        exports[name] = (args...) ->
            if args.length is 1 and type(args[0]) is 'object'
                config = args[0]
                body = if Array.isArray config.body then config.body else [config.body]
                throw new Error('illegal body property.') unless Array.isArray body

                result = res[name].apply res, body
                result.status = config.status or result.status
                result.headers = objects.extend result.headers, config.headers or {}
                result
            else
                res[name].apply res, args

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
    contentType = 'application/json'
    contentType = "#{contentType}; charset=#{res.charset()}" if res.charset()

    result =
        _object: object,
        _included: {},
        _excluded: {},
        _jsonResult: null,
        _isStream: false,
        status: 200,
        headers:
            'Content-Type': contentType

        add: (filter, fields, t) ->
            target = if t is 'e' or t is 'exclude' then @_excluded else @_included
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

            unless @_jsonResult?
                mapper = buildObjectMapper @_included, @_excluded
                @_jsonResult = mapper.writeValueAsString @_object
                log.debug "generate json, json result:#{@_jsonResult}"
            log.debug "forEach called, json result:#{@_jsonResult}"
            fn @_jsonResult

        asStream: (request) ->
            @_isStream = true
            log.debug JSON.stringify @headers
            exports.stream request,
                headers: @headers
                status: @status
                body: (stream) =>
                    mapper = buildObjectMapper @_included, @_excluded
                    mapper.writeValue stream, @_object
                    return

    if config?
        result.include k, v for k, v of config.include
        result.exclude k, v for k, v of config.exclude
        result.status = config.status or result.status
        result.headers = objects.extend result.headers, config.headers or {}

    result.body = result
    result

buildObjectMapper = (included, excluded) ->
    filter = new SimpleFilterProvider()

    for k, v of included
        throw new Error("filter:#{key} have both include and exclude property") if k in excluded
        filter.addFilter k, SimpleBeanPropertyFilter.filterOutAllExcept v

    filter.addFilter k, SimpleBeanPropertyFilter.serializeAllExcept v for k, v of excluded

    mapper = new ObjectMapper()
    mapper.writer filter

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
        return exports.error error

    status: status
    headers:
        'X-JSGI-Skip-Response': true
    body: []