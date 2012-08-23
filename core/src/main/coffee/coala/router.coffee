{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet
{Application} = require 'stick'
{objects, type, paths} = require 'coala/util'
{coala} = require 'coala/config'
{json,html} = require 'coala/response'
{createService} = require 'coala/scaffold/service'
{createConverter} = require 'coala/scaffold/converter'

log = require('ringo/logging').getLogger module.id
entityMetaResovler = Context.getInstance(module).getBeanByClass(com.zyeeda.framework.web.scaffold.EntityMetaResolver)

processRoot = (router, repo, prefix) ->
    routersRepo = repo.getChildRepository coala.routerFoldername
    print router, routersRepo, routersRepo.exists(), 'routers'
    return if not routersRepo.exists()
    routers = routersRepo.getResources false
    for r in routers
        try
            module = r.getModuleName()
            url = prefix + r.getBaseName()
            log.debug "mount #{module} to #{url}"
            router.mount url, module
        catch e
            log.warn "can't mount #{r.getModuleName()}, it is not export and router"
    true

processRepository = (router, repo, prefix) ->
    processRoot router, repo, prefix
    for r in repo.getRepositories()
        processRepository router, r, prefix + r.getName() + '/'
    true

exports.createMountPoint = (module)->
    router = new Application()
    router.configure 'mount'
    if module
        root = module.getRepository('./')
        processRepository router, root, '/'
    router

exports.createRouter = ->
    router = new Application()
    router.configure 'params', 'route'
    extendRouter router
    router

autoMount = (router)->
    repos = @getRepository('./').getRepositories()
    for repo in repos
        resource = repo.getResource 'module.js'
        try
            router.mount "/#{repo.getName()}", resource.getModuleName() if resource.exists()
        catch e
            log.warn "can't mount #{resource.getModuleName()}, it is not export an router"

extendRouter = (router) ->
    router.attachDomain = attachDomain.bind router, router
    router.resolveEntity = resolveEntity.bind router
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

    listUrl = path
    removeUrl = updateUrl = getUrl = path + ID_SUFFIX
    createUrl = path
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
            target[name].call target, paths.join(path, url), fn for url,fn of router[name + 's']

createEntity = (clazz) ->
    c = clazz.getConstructor()
    c.newInstance()

getService = (options, entityMeta) ->
    options.service or createService entityMeta.entityClass


getJsonFilter = (options, type) ->
    return {} unless options.filters
    options.filters[type] or options.filters.defaults or {}


defaultHandlers =
    list: (options, service, entityMeta, request) ->
        result = {}

        entity = createEntity entityMeta.entityClass
        mergeEntityAndParameter options, request.params, entityMeta, 'list', entity

        configs = coala.extractPaginationInfo request.params
        orders = coala.extractOrderInfo request.params
        restricts = coala.extractRestrictInfo request.params
        if configs?
            if restricts?
                configs.restricts = restricts
            configs.configs = options.configs

            configs.fetchCount = true
            pageSize = configs.maxResults
            count = service.list entity, configs
            result.recordCount = count
            result.pageCount = Math.ceil count/pageSize
            delete configs.fetchCount

        if orders?.length isnt 0
            configs = configs or {}
            configs.orderBy = orders

        result.results = service.list entity, configs

        o = coala.generateListResult result.results, configs.currentPage, configs.maxResults, result.recordCount, result.pageCount
        json o, getJsonFilter(options, 'list')

    get: (options, service, entityMeta, request, id) ->
        json service.get(id), getJsonFilter(options, 'get')

    create: (options, service, entityMeta, request) ->
        entity = createEntity entityMeta.entityClass
        mergeEntityAndParameter options, request.params, entityMeta, 'create', entity
        json service.create(entity), getJsonFilter(options, 'create')

    update: (options, service, entityMeta, request, id) ->
        entity = service.update id, mergeEntityAndParameter.bind(@, options, request.params, entityMeta, 'update')
        json entity, getJsonFilter(options, 'update')

    remove: (options, service, entityMeta, request, id) ->
        json service.remove(id), getJsonFilter(options, 'remove')

    batchRemove: (options, service, entityMeta, request) ->
        ids = request.params.ids
        ids = if type(ids) is 'string' then [ids] else ids
        result = service.remove.apply service, ids
        json result, getJsonFilter(options, 'batchRemove')


# the reason why put the entity in the end of argument list is that,
# when update, the arguments before entity are all bound
mergeEntityAndParameter = (options, params, entityMeta, type, entity) ->
    converter = createConverter options.converters
    for key, value of params
        continue if not entityMeta.hasField key
        entity[key] = converter.convert value,entityMeta.getField(key)
    options.afterMerge? entity, type
    entity
