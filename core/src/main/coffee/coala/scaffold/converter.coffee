
{env} = require 'coala/config'
{objects} = require 'coala/util'
{createService} = require 'coala/service'
{SimpleDateFormat} = java.text
{Calendar, Date} = java.util

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
    if fieldMeta.isEntity()
        service = createService()
        manager = service.createManager fieldMeta.type
        manager.find value
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
