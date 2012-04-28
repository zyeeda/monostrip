
{env} = require 'config'
{objects} = require 'coala/util'
{createService} = require 'coala/service'
{SimpleDateFormat} = java.text
{Calendar, Date} = java.util
{DynamicModuleHelper} = com.zyeeda.framework.web.jsgi

parseDate = (desiredType, stringDate, pattern) ->
    format = new SimpleDateFormat pattern
    date = format.parse stringDate
    if desiredType is Calendar then format.getCalendar() else date


inner =
    'java.util.Date': (value, fieldName, clazz, isEntity, context, pattern = env.dateFormat) ->
        parseDate Date, value, pattern
    'java.util.Calendar': (value, fieldName, clazz, isEntity, context, pattern = env.dateFormat) ->
        parseDate Calendar, value, pattern

defaultConvertor = (value, fieldName, clazz, isEntity, context) ->
    if isEntity
        service = createService()
        dao = service.createDao clazz
        dao.find value
    else
        DynamicModuleHelper.constructByString clazz, value

exports.createConvertor = (convertors = {}) ->

    _inner: objects.extend {}, inner, convertors
    convert: (value, fieldName, clazz, isEntity, context) ->
        print "convert value:#{value} fieldName:#{fieldName} clazz:#{clazz.getName()} isEntity:#{isEntity}"
        convertor = if @_inner[fieldName] then @_inner[fieldName] else @_inner[clazz.getName()]
        convertor = convertor or defaultConvertor
        convertor.call null, value, fieldName, clazz, isEntity, context
