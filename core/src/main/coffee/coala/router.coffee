{DynamicModuleHelper} = com.zyeeda.framework.web.jsgi
{Application} = require 'stick'
{objects} = require 'coala/util'
{env} = require 'config'
{json,html} = require 'coala/response'
{createService} = require 'coala/dynamic/service'
{createConvertor} = require 'coala/dynamic/convertor'

exports.createRouter = ->
    app = new Application()
    app.configure 'params', 'route'
    extendApp app

    app

extendApp = (app) ->
    app.attachDomain = attachDomain.bind app, app

# By default, the relationship of action, domain operate and url map is:
# create: insert a domain, url is POST path
# list: search for domains, url is GET path/by/field-desc/page/1
# get: fetch a domain by id, url is GET path/domain_id
# update: update a domain, url is PUT path/domain_id
# remove: remove a domain by id, url is DELETE path/domain_id
# batchRemove: batch remove domains, url is POST path/delete

# These actions metioned above are auto generated.
# You can exclude some of them by add parameter 'deny' into options,
# ex. deny: ['create', 'list']

# You must specify json filters in options, these filters are used to generate action results
# the key named 'jsonFilter' is the default filter for all actions.
# you can use actionName+JsonFilter to special an action.
# ex.
# jsonFilter: {include: {'filter name': ['fields']}, exclude: {'filter name': [fields]}}
# listJsonFilter: {include: ...

# there also is a way to specifiy some converters to convert request parameters to domain fields
# converters: { 'field name or class name': (value, fieldName, fieldClass, isEntityClass, context) -> }

ORDER_BY_AND_PAGE_PARAMS_REGEXP_SUFFIX = '(?:\/by((?:\/(?!page)\\w+(?:\-desc|\-asc)?)*))?(?:\/page\/(\\d+))?'
ORDER_BY_REGEXP = /\/(\w+)(?:\-(desc|asc))?/g
ID_SUFFIX = '/:id'

attachDomain = (app, path, clazz, options = {}) ->
    descriptor = DynamicModuleHelper.resolveEntity path, clazz

    listUrl = new RegExp("#{url.replace '/', '\\/'}#{ORDER_BY_AND_PAGE_PARAMS_REGEXP_SUFFIX}")
    removeUrl = updateUrl = getUrl = url + ID_SUFFIX
    createUrl = url
    batchRemoveUrl = url + '/delete'

    app.get listUrl, handlers.list.bind handlers, options, descriptor
    app.get getUrl, handlers.get.bind handlers, options, descriptor
    app.post createUrl, handlers.create.bind handlers, options, descriptor
    app.put updateUrl, handlers.update.bind handlers, options, descriptor
    app.del removeUrl, handlers.remove.bind handlers, options, descriptor
    app.post batchRemoveUrl, handlers.batchRemove.bind handlers, options, descriptor

    app


getService = (options, descriptor) ->
    options.service or createService descriptor.entityClass


getJsonFilter = (options, type) ->
    options[type + 'JsonFilter'] or options.jsonFilter or {}


handlers =
    list: (options, descriptor, request, orders, page) ->
        service = getService options, descriptor
        result = {}

        entity = DynamicModuleHelper.newInstance descriptor.entityClass
        mergeEntityAndParameter options, request.params, descriptor, 'list', entity

        configs = getPageInfo request, page
        if configs?
            configs.fetchCount = true
            pageSize = configs.maxResults
            count = service.list entity, configs
            result.recordCount = count
            result.pageCount = Math.ceil count/pageSize
            delete configs.fetchCount

        orderBy = getOrderBy orders
        if orderBy?.length isnt 0
            configs = configs or {}
            configs.orderBy = orderBy

        result.results = service.list entity, configs

        json result, getJsonFilter(options, 'list')

    get: (options, descriptor, request, id) ->
        service = getService options, descriptor
        json service.get(id), getJsonFilter(options, 'get')

    create: (options, descriptor, request) ->
        service = getService options, descriptor
        entity = DynamicModuleHelper.newInstance descriptor.entityClass
        mergeEntityAndParameter options, request.params, descriptor, 'create', entity
        json service.create(entity), getJsonFilter(options, 'create')

    update: (options, descriptor, request, id) ->
        service = getService options, descriptor
        entity = service.update id, mergeEntityAndParameter.bind(@, options, request.params, descriptor, 'update')
        json entity, getJsonFilter(options, 'update')

    remove: (options, descriptor, request, id) ->
        service = getService options, descriptor
        json service.del(id), getJsonFilter(options, 'remove')

    batchRemove: (options, descriptor, request) ->
        service = getService options, descriptor
        result = service.del.apply service, request.params.ids
        json result, getJsonFilter(options, 'batchRemove')


# the reason why put the entity in the end of argument list is that,
# when update, the arguments before entity are all bound
mergeEntityAndParameter = (options, params, descriptor, type, entity) ->
    convertor = createConvertor options.convertors
    for key, value of params
        continue if not descriptor.containsField key
        entity[key] = convertor.convert value, key, descriptor.getFieldClass(key), descriptor.isEntity(key), context
    extension.afterMerge? entity, type
    entity


getOrderBy = (orders) ->
    result = []
    while (m = ORDER_BY_REGEXP.exec orders ) isnt null
        order = {}
        order[m[1]] = m[2] or env.defaultOrder
        result.push order
    result


getPageInfo = (request, page) ->
    return null unless page?
    key = env.pageSizeKey
    pageSize = request.params[key] or env.defaultPageSize
    delete request.params[key]

    firstResult: (page - 1) * pageSize
    maxResults: pageSize
