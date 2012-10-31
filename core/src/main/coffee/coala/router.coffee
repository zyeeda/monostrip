{Application} = require 'stick'
_ = require 'underscore'

{getLogger} = require 'ringo/logging'
{objects, type, paths} = require 'coala/util'
{coala} = require 'coala/config'
{json, html, notFound, internalServerError} = require 'coala/response'

{createService} = require 'coala/scaffold/service'
{createConverter} = require 'coala/scaffold/converter'
{createValidator} = require 'coala/validation/validator'
{createValidationContext} = require 'coala/validation/validation-context'

{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet
{Create, Update} = com.zyeeda.framework.validation.group

log = getLogger module.id
validator = new createValidator()
entityMetaResovler = Context.getInstance(module).getBeanByClass(com.zyeeda.framework.web.scaffold.EntityMetaResolver)

###
processRoot = (router, repo, prefix) ->
    routersRepo = repo.getChildRepository coala.routerFoldername
    log.debug "routersRepo.exists = #{routersRepo.exists()}"
    return if not routersRepo.exists()
    routers = routersRepo.getResources false
    for r in routers
        try
            module = r.getModuleName()
            url = prefix + r.getBaseName()
            log.debug "mount #{module} to #{url}"
            router.mount url, module
        catch e
            log.error "Cannot mount #{r.getModuleName()}."
    true

processRepository = (router, repo, prefix) ->
    processRoot router, repo, prefix
    for r in repo.getRepositories()
        processRepository router, r, prefix + r.getName() + '/'
    true

exports.createApplication = (module, mountDefaultRouters = true) ->
    router = new Application()
    router.configure 'mount'

    if module
        root = module.getRepository('./')
        processRepository router, root, '/'

    if mountDefaultRouters
        router.mount '/helper', 'coala/frontend-development-helper-router' if coala.development
        router.mount '/scaffold', 'coala/scaffold/router'
        router.mount '/scaffold/tasks', 'coala/scaffold/task'

    router
###

exports.createRouter = ->
    router = new Application()
    router.configure 'params', 'route'
    extendRouter router
    router

extendRouter = (router) ->
    router.attachDomain = attachDomain.bind null, router
    router.resolveEntity = resolveEntity.bind null
    return

resolveEntity = (entity, params, converters) ->
    entityMeta = entityMetaResovler.resolveEntity entity.getClass()
    mergeEntityAndParameter converters: converters, params, entityMeta, 'resolve', entity

# By default, the relationship of action, domain operate and url map is:
# create: insert a domain, url is POST path
# list: search for domains, url is GET path/by/field-desc/page/1
# get: fetch a domain by id, url is GET path/domain_id
# update: update a domain, url is PUT path/domain_id
# remove: remove a domain by id, url is DELETE path/domain_id
# batchRemove: batch remove domains, url is POST path/delete

# can supply handlers to override default action handlers
# ex.
#   handlers: {
#       get: function(options, service, entityMeta, request, id){
#           ...
#       }
#   }

# These actions metioned above are auto generated.
# You can exclude some of them by add parameter 'deny' into options,
# ex. exclude: ['create', 'list']

# You must specify json filters in options, these filters are used to generate action results
# the key named 'defaults' in filters is the default filter for all actions.
# you can use actionName to special an action.
# ex.
# filters: {
#     defaults: {include: {'filter name': ['fields']}, exclude: {'filter name': [fields]}}
#     list: {include: ...}
#     get: {...}
# }

# there also is a way to specifiy some converters to convert request parameters to domain fields
# converters: { 'field name or class name': (value, fieldMeta) -> }

ID_SUFFIX = '/:id'

attachDomain = (router, path, clazz, options = {}) ->
    entityMeta = entityMetaResovler.resolveEntity clazz
    entityMeta.path = path if entityMeta.path is null
    path = entityMeta.path

    listUrl = createUrl = path
    removeUrl = updateUrl = getUrl = path + ID_SUFFIX
    batchRemoveUrl = path + '/delete'

    excludes = {}
    excludes[name] = true for name in options.exclude or []

    service = getService options, entityMeta
    handlers = objects.extend {}, defaultHandlers, options.handlers or {}

    router.get listUrl, handlers.list.bind handlers, options, service, entityMeta unless excludes.list
    router.get getUrl, handlers.get.bind handlers, options, service, entityMeta unless excludes.get
    router.post createUrl, handlers.create.bind handlers, options, service, entityMeta unless excludes.create
    router.put updateUrl, handlers.update.bind handlers, options, service, entityMeta unless excludes.update
    router.del removeUrl, handlers.remove.bind handlers, options, service, entityMeta unless excludes.remove
    router.post batchRemoveUrl, handlers.batchRemove.bind handlers, options, service, entityMeta unless excludes.batchRemove

    if type(options.doWithRouter) is 'function'
        r = createMockRouter()
        options.doWithRouter r
        mountMockRouter router, path, r

    router

createMockRouter = ->
    router =
        gets: {}
        posts: {}
        puts: {}
        dels: {}

    for name in ['get', 'post', 'put', 'del']
        do (name) ->
            router[name] = (url, fn) ->
                router[name+'s'][url] = fn
    router

mountMockRouter = (target, path, router) ->
    for name in ['get', 'post', 'put', 'del']
        do (name) ->
            target[name].call target, paths.join(path, url), fn for url, fn of router[name + 's']

createEntity = (clazz) ->
    c = clazz.getConstructor()
    c.newInstance()

getService = (options, entityMeta) ->
    options.service or createService entityMeta.entityClass, entityMeta

getJsonFilter = (options, type) ->
    return {} unless options.filters
    options.filters[type] or options.filters.defaults or {}

defaultHandlers =
    list: (options, service, entityMeta, request) ->
        entity = createEntity entityMeta.entityClass
        mergeEntityAndParameter options, request.params, entityMeta, 'list', entity

        result = {}
        config = {}

        paginationInfo = coala.extractPaginationInfo request.params
        if paginationInfo?
            paginationInfo.fetchCount = true

            pageSize = paginationInfo.maxResults
            count = service.list entity, paginationInfo

            result.recordCount = count
            result.pageCount = Math.ceil count/pageSize

            delete paginationInfo.fetchCount
            _.extend config, paginationInfo

        orderInfo = coala.extractOrderInfo request.params
        if orderInfo?.length isnt 0
            config.orderBy = orderInfo

        restrictInfo = coala.extractRestrictInfo request.params
        if restrictInfo?.length isnt 0
            config.restricts = restrictInfo
            if options.configs?
                options.configs.fields = setFieldsDefaultValue options.configs.fields
                config.configs = options.configs

        ###
        configs = coala.extractPaginationInfo request.params
        orders = coala.extractOrderInfo request.params
        if configs?
            configs.fetchCount = true
            pageSize = configs.maxResults
            count = service.list entity, configs
            result.recordCount = count
            result.pageCount = Math.ceil count/pageSize
            delete configs.fetchCount

        if orders?.length isnt 0
            configs = configs or {}
            configs.orderBy = orders

        ###
        result.results = service.list entity, config

        o = coala.generateListResult result.results, config.currentPage, config.maxResults, result.recordCount, result.pageCount
        json o, getJsonFilter(options, 'list')

    get: (options, service, entityMeta, request, id) ->
        entity = service.get id
        return notFound() if entity is null

        json entity, getJsonFilter(options, 'get')

    create: (options, service, entityMeta, request) ->
        entity = createEntity entityMeta.entityClass
        mergeEntityAndParameter options, request.params, entityMeta, 'create', entity

        violations = callValidators 'create', options, request, entity
        return json violations: violations if violations.length > 0

        result = callHook 'before', 'Create', options, entityMeta, request, entity
        return result if result isnt undefined

        entity = service.create(entity)

        result = callHook 'after', 'Create', options, entityMeta, request, entity
        return result if result isnt undefined

        json entity, objects.extend getJsonFilter(options, 'create'), { status: 201 }

    update: (options, service, entityMeta, request, id) ->
        entity = service.get id
        return notFound() if entity is null

        mergeEntityAndParameter options, request.params, entityMeta, 'update', entity

        violations = callValidators 'update', options, request, entity
        return json violations: violations if violations.length > 0

        result = callHook 'before', 'Update', options, entityMeta, request, entity
        return result if result isnt undefined

        service.update entity

        result = callHook 'after', 'Update', options, entityMeta, request, entity
        return result if result isnt undefined

        json entity, getJsonFilter(options, 'update')

    remove: (options, service, entityMeta, request, id) ->
        entity = service.get id
        return notFound() if entity is null

        violations = callValidators 'remove', options, request, entity
        return json violations: violations if violations.length > 0

        result = callHook 'before', 'Remove', options, entityMeta, request, entity
        return result if result isnt undefined

        service.remove entity

        result = callHook 'after', 'Remove', options, entityMeta, request, entity
        return result if result isnt undefined

        json id

    batchRemove: (options, service, entityMeta, request) ->
        ids = request.params.ids
        ids = if type(ids) is 'string' then [ids] else ids

        entities = (service.get id for id in ids)

        violations = callValidators 'batchRemove', options, request, entities
        return json violations: violations if violations.length > 0

        result = callHook 'before', 'BatchRemove', options, entityMeta, request, entities
        return result if result isnt undefined

        service.remove.apply service, entities

        result = callHook 'after', 'BatchRemove', options, entityMeta, request, entities
        return result if result isnt undefined

        r = (entity.id for entity in entities)
        json r

# the reason why put the entity in the end of argument list is that,
# when update, the arguments before entity are all bound
mergeEntityAndParameter = (options, params, entityMeta, type, entity) ->
    converter = createConverter options.converters
    for key, value of params
        continue if not entityMeta.hasField key
        entity[key] = converter.convert value, entityMeta.getField(key)
    options.afterMerge? entity, type
    entity

validationGroupMapping =
    create: Create
    update: Update

callValidators = (action, options, request, entity) ->
    context = createValidationContext()
    formName = request.params['_formName_'] or 'defaults'

    customValidator = options.validators?[action]?[formName]
    if customValidator? and type(customValidator) is 'function'
        customValidator.call null, context, entity, request

    log.debug "context.isBeanValidationSkipped = #{context.isBeanValidationSkipped}"

    if not context.isBeanValidationSkipped and (action is 'create' or action is 'update')
        validator.validate context, entity, validationGroupMapping[action]

    log.debug "context.hasViolations() = #{context.hasViolations()}"
    log.debug "context.violations.length = #{context.violations.length}"

    context.collectViolations()

callHook = (hookType, action, options, meta, request, entity) ->
    hookName = hookType + action
    formName = request.params['_formName_'] or 'defaults'

    hook = options.hooks?[hookName]?[formName]
    if hook? and type(hook) is 'function'
        try
            hook.call null, entity, request, meta
        catch e
            internalServerError e

setFieldsDefaultValue = (fields) ->
    isNullAlias = false
    isNullPosition = false
    if fields? and fields.length > 0
        isNullAlias = true if not fields[0].alias
        isNullPosition = true if not fields[0].position and fields[0].position != 0
    else
        return fields
    if isNullAlias and isNullPosition
        for f, i in fields
            f.alias = f.name
            f.position = i
        return fields
    else if !isNullAlias and isNullPosition
        for f, i in fields
            f.position = i
        return fields
    else if isNullAlias and !isNullPosition
        for f, i in fields
            f.alias = f.name
        return fields
    fields
