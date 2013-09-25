{SimpleDateFormat} = java.text
{Calendar, Date, ArrayList, HashSet} = java.util
{ClassUtils} = org.springframework.util

{coala} = require 'coala/config'
{type} = require 'coala/util/type'
objects = require 'coala/util/objects'

serviceHost = require 'coala/service'

parseDate = (pattern, desiredType, stringDate) ->
    return null if not stringDate
    format = new SimpleDateFormat pattern
    date = format.parse stringDate
    if desiredType is Calendar then format.getCalendar() else date

innerConverters =
    'java.util.Date': (value, fieldMeta) ->
        parseDate.apply null, [coala.dateFormat, Date, value]
    'java.util.Calendar': (value, fieldMeta) ->
        parseDate.apply null, [coala.dateFormat, Calendar, value]
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


handleArray = (service, fieldType, targetType, value) ->
    value = if type(value) is 'string' then [value] else value
    list = if ClassUtils.isAssignable fieldType, ArrayList then new ArrayList() else new HashSet()

    manager = service.createManager targetType
    for id in value
        entity = manager.find id
        list.add entity if entity

    list

defaultConverter = (value, fieldMeta) ->

    if fieldMeta.isEntity()
        service = serviceHost.createService()
        manager = service.createManager fieldMeta.type
        manager.find value
    else if fieldMeta.isManyToManyOwner()
        service = serviceHost.createService()
        handleArray service, fieldMeta.type, fieldMeta.manyToManyTargetType, value
    else if fieldMeta.isManyToManyTarget()
        service = serviceHost.createService()
        handleArray service, fieldMeta.type, fieldMeta.manyToManyOwnerType, value
    else if fieldMeta.isOneToMany()
        service = serviceHost.createService()
        handleArray service, fieldMeta.type, fieldMeta.manyType, value
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
        converter.call null, value, fieldMeta
