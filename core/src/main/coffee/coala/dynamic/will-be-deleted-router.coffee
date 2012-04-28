{DynamicModuleHelper} = com.zyeeda.framework.web.jsgi
{Application} = require 'stick'
{createContext} = require 'coala/context'
{env} = require 'config'
{json,html} = require 'coala/response'
{createDynamicBiz} = require 'coala/dynamic/biz'
{createConvertor} = require 'coala/dynamic/convertor'
{objects} = require 'coala/util'

###
extension point in extension module

extension.createBiz, type: function arguments: context, dynamicBiz
extension.convertors, type: object key: field name or class name
    value: function with arguments:value, fieldname, fieldclass, isEntityClass, context
extension.jsonFilter, type: object see: response.json
extension.listJsonFilter, type: object see:response.json
extension.extraActions, type: object see:bottom of this file
extension.afterMerge, type: function arguments: entity, mergeFor('list', 'create', 'update')
###

app = exports.app = Application()
app.configure 'params', 'route'

getEntities =  ->
    descriptors = DynamicModuleHelper.getConfigedEntities(env.entityPackages)
    result = {}
    result[descriptor.path] = descriptor for descriptor in descriptors

    return result

configedEntities = getEntities()

orderByAndPageParamsRegExpSuffix = '(?:\/by((?:\/(?!page)\\w+(?:\-desc|\-asc)?)*))?(?:\/page\/(\\d+))?'
orderByRegExp = /\/(\w+)(?:\-(desc|asc))?/g
idSuffix = '/:id'

getOrderBy = (orders) ->
    result = []
    while (m = orderByRegExp.exec orders ) isnt null
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

createBiz = (context, extension, entityClass) ->
    dynamicBiz = createDynamicBiz context, entityClass
    if extension.createBiz then extension.createBiz context, dynamicBiz else dynamicBiz

mergeEntityAndParameter = (context, extension, params, descriptor, mergeFor, entity) ->
    convertor = createConvertor extension.convertors
    for key, value of params
        continue if not descriptor.containsField key
        entity[key] = convertor.convert value, key, descriptor.getFieldClass(key), descriptor.isEntity(key), context
    extension.afterMerge? entity, mergeFor
    entity

handler =
    list: (extension, descriptor, jsonFilter, request, orders, page) ->
        context = createContext request
        biz = createBiz context, extension, descriptor.entityClass
        result = {}

        entity = DynamicModuleHelper.newInstance descriptor.entityClass
        mergeEntityAndParameter context, extension, request.params, descriptor, 'list', entity

        options = getPageInfo request, page
        if options?
            options.fetchCount = true
            pageSize = options.maxResults
            count = biz.list entity, options
            result.recordCount = count
            result.pageCount = Math.ceil count/pageSize
            delete options.fetchCount

        orderBy = getOrderBy orders
        pageable = options?
        if orderBy?.length isnt 0
            options = options or {}
            options.orderBy = orderBy


        result.results = biz.list entity, options

        json result, jsonFilter

    get: (extension, descriptor, jsonFilter, request, id) ->
        context = createContext request
        biz = createBiz context, extension, descriptor.entityClass
        json biz.get(id), jsonFilter

    create: (extension, descriptor, jsonFilter, request) ->
        context = createContext request
        biz = createBiz context, extension, descriptor.entityClass
        entity = DynamicModuleHelper.newInstance descriptor.entityClass
        mergeEntityAndParameter context, extension, request.params, descriptor, 'create', entity
        json biz.create(entity), jsonFilter

    update: (extension, descriptor, jsonFilter, request, id) ->
        context = createContext request
        biz = createBiz context, extension, descriptor.entityClass
        entity = biz.update id, mergeEntityAndParameter.bind(@, context, extension, request.params, descriptor, 'update')
        json entity, jsonFilter

    del: (extension, descriptor, jsonFilter, request, id) ->
        context = createContext request
        biz = createBiz context, extension, descriptor.entityClass
        json biz.del(id), jsonFilter

    batchDelete: (extension, descriptor, jsonFilter, request) ->
        context = createContext request
        biz = createBiz context, extension, descriptor.entityClass
        result = biz.del.apply biz, request.params.ids
        json result, jsonFilter

resolveEnitties = (url, descriptor) ->
    extension =
        try
            require env.dynamicExtentionPath + url
        catch e
            print "error: #{e}"
            {}

    jsonFilter = extension.jsonFilter
    listJsonFilter = extension.listJsonFilter or jsonFilter
    print "#{url} filter:#{JSON.stringify jsonFilter}"

    listUrl = new RegExp("#{url.replace '/', '\\/'}#{orderByAndPageParamsRegExpSuffix}")
    deleteUrl = updateUrl = getUrl = url + idSuffix
    createUrl = url
    batchDeleteUrl = url + '/delete'

    app.get listUrl, handler.list.bind handler, extension, descriptor, listJsonFilter
    app.get getUrl, handler.get.bind handler, extension, descriptor, jsonFilter
    app.post createUrl, handler.create.bind handler, extension, descriptor, jsonFilter
    app.put updateUrl, handler.update.bind handler, extension, descriptor, jsonFilter
    app.del deleteUrl, handler.del.bind handler, extension, descriptor, jsonFilter
    app.post batchDeleteUrl, handler.batchDelete.bind handler, extension, descriptor, jsonFilter

    ###
    # extraActions should be like
    # exports.extraActions = [{
    #   method: 'get'
    #   url: 'url'
    #   fn: ->
    # },{
    #   method: 'post'
    #   url: 'url'
    #   fn: ->
    # }]
    ###
    if extension.extraActions?
        print "extra: #{url}, #{JSON.stringify extension.extraActions}"
        app[action.method] url + action.url, action.fn for action in extension.extraActions
    return

resolveEnitties key, value for key, value of configedEntities
