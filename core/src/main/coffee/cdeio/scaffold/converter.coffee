{Calendar, Date, ArrayList, HashSet, TimeZone} = java.util
{ClassUtils} = org.springframework.util
{DateUtils} = org.apache.commons.lang.time
{EntityMetaResolver} = com.zyeeda.cdeio.web.scaffold

{cdeio} = require 'cdeio/config'
{type} = require 'cdeio/util/type'
{mark} = require 'cdeio/mark'
objects = require 'cdeio/util/objects'

serviceHost = require 'cdeio/service'

parseDate = (pattern, desiredType, stringDate) ->
    return null if not stringDate
    DateUtils.parseDate stringDate, pattern

innerConverters =
    'java.util.Date': (value, fieldMeta) ->
        parseDate.apply null, [cdeio.freeDateFormat, Date, value]
    'java.util.Calendar': (value, fieldMeta) ->
        parseDate.apply null, [cdeio.freeDateFormat, Calendar, value]
    'java.lang.Integer': (value, fieldMeta) ->
        if value is null or value.length is 0 then null else new java.lang.Integer(value)
    'java.lang.Long': (value, fieldMeta) ->
        if value is null or value.length is 0 then null else new java.lang.Long(value)
    'java.lang.Float': (value, fieldMeta) ->
        if value is null or value.length is 0 then null else new java.lang.Float(value)
    'java.lang.Double': (value, fieldMeta) ->
        if value is null or value.length is 0 then null else new java.lang.Double(value)
    'java.lang.Boolean': (value, fieldMeta) ->
        if value is null or value.length is 0 then null else new java.lang.Boolean(value)


handleArray = (service, fieldType, targetType, value, converter) ->
    t = type(value)
    if t is 'object'
        vs = []
        vs[k] = v for k, v of value
        value = vs
    else if t is 'string'
        value = [value]

    list = if ClassUtils.isAssignable fieldType, ArrayList then new ArrayList() else new HashSet()

    manager = service.createManager targetType
    fn = mark('beans', EntityMetaResolver).on (resolver) ->
        meta = resolver.resolveEntity targetType
        for id in value
            if type(id) is 'object'
                o = targetType.newInstance()
                for k, v of id
                    continue if not meta.hasField k
                    o[k] = converter.convert v, meta.getField(k) if v != 'null'
                list.add o
            else
                entity = manager.find id
                list.add entity if entity

    fn()
    list

defaultConverter = (value, fieldMeta) ->

    if fieldMeta.isEntity()
        if value and value.id
            service = serviceHost.createService()
            manager = service.createManager fieldMeta.type
            manager.find value.id
        else
            c = fieldMeta.type.getConstructor()
            c.newInstance()
    else if fieldMeta.isManyToManyOwner()
        service = serviceHost.createService()
        handleArray service, fieldMeta.type, fieldMeta.manyToManyTargetType, value, @
    else if fieldMeta.isManyToManyTarget()
        service = serviceHost.createService()
        handleArray service, fieldMeta.type, fieldMeta.manyToManyOwnerType, value, @
    else if fieldMeta.isOneToMany()
        service = serviceHost.createService()
        handleArray service, fieldMeta.type, fieldMeta.manyType, value, @
    else
        return value if fieldMeta.type is java.lang.String
        return null if value is null or value.length is 0

        c = fieldMeta.type.getConstructor java.lang.String
        c.newInstance value

exports.createConverter = (converters = {}) ->

    _inner: objects.extend {}, innerConverters, converters
    convert: (value, fieldMeta) ->
        converter = if @_inner[fieldMeta.name] then @_inner[fieldMeta.name] else @_inner[fieldMeta.type.getName()]
        converter = converter or defaultConverter
        converter.call @, value, fieldMeta
