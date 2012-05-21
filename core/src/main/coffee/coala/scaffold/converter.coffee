{SimpleDateFormat} = java.text
{Calendar, Date, ArrayList, HashSet} = java.util
{ClassUtils} = org.springframework.util

{env} = require 'coala/config'
{objects, type} = require 'coala/util'
{createService} = require 'coala/service'

parseDate = (pattern, desiredType, stringDate) ->
    format = new SimpleDateFormat pattern
    date = format.parse stringDate
    if desiredType is Calendar then format.getCalendar() else date

innerConverters =
    'java.util.Date': (value, fieldMeta) ->
        parseData.apply null, [env.dateFormat, Date, value]
    'java.util.Calendar': (value, fieldMeta) ->
        parseData.apply null, [env.dateFormat, Calendar, value]

defaultConverter = (value, fieldMeta) ->
    service = createService()

    if fieldMeta.isEntity()
        manager = service.createManager fieldMeta.type
        manager.find value
    else if fieldMeta.isManyToManyOwner()
        value = if type(value) is 'string' then [value] else value
        list = if ClassUtils.isAssignable fieldMeta.type, ArrayList then new ArrayList() else new HashSet()

        manager = service.createManager fieldMeta.manyToManyTarget
        list.add manager.find id for id in value

        list
    else
        return value if fieldMeta.type is java.lang.String
        c = fieldMeta.type.getConstructor java.lang.String
        c.newInstance value

exports.createConverter = (converters = {}) ->

    _inner: objects.extend {}, innerConverters, converters
    convert: (value, fieldMeta) ->
        converter = if @_inner[fieldMeta.name] then @_inner[fieldMeta.name] else @_inner[fieldMeta.type.getName()]
        converter = converter or defaultConverter
        converter.call null, value, fieldMeta
