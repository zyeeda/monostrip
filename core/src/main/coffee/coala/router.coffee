{Application} = require 'stick'
_ = require 'underscore'

{getLogger} = require 'ringo/logging'
{type} = require 'coala/util/type'
objects = require 'coala/util/objects'
paths = require 'coala/util/paths'
{coala} = require 'coala/config'
{mark} = require 'coala/mark'
{json, html, notFound, internalServerError} = require 'coala/response'
{Authentication} = org.activiti.engine.impl.identity
{EventSubscriptionQueryImpl} = org.activiti.engine.impl
{ClassUtils} = org.springframework.util

{createService} = require 'coala/scaffold/service'
{createConverter} = require 'coala/scaffold/converter'
{createValidator} = require 'coala/validation/validator'
{createValidationContext} = require 'coala/validation/validation-context'

{createManager} = require 'coala/manager'

# processService = require 'coala/scaffold/process-service'

{Context} = com.zyeeda.coala.web.SpringAwareJsgiServlet
{Create, Update} = com.zyeeda.coala.validation.group

{SecurityUtils} = org.apache.shiro

timeFormat = new java.text.SimpleDateFormat 'yyyy-MM-dd HH:mm:ss'

log = getLogger module.id
entityMetaResovler = Context.getInstance(module).getBeanByClass(com.zyeeda.coala.web.scaffold.EntityMetaResolver)

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

attachDomain = (router, path, entityMeta, options = {}) ->
    listUrl = createUrl = path
    removeUrl = updateUrl = getUrl = path + ID_SUFFIX
    batchRemoveUrl = path + '/delete'

    excludes = {}
    excludes[name] = true for name in options.exclude or []

    service = getService options, entityMeta
    handlers = objects.extend {}, defaultHandlers(path, options), options.handlers or {}
    processHandlers = ->


    defaultPickerRouter = ->
        pickerOptions = objects.extend {}, options, {listType: 'picker'}
        router.get paths.join(path, '/picker'), handlers.list.bind handlers, pickerOptions, service, entityMeta unless excludes.list

    if type(options.doWithRouter) is 'function'
        r = createMockRouter()
        options.doWithRouter r
        defaultPickerRouter() unless mountMockRouter router, path, r
    else
        defaultPickerRouter()

    if options.style is 'process'
        url_subfix = '/process/'

        listUrl = path + url_subfix + 'waiting'
        getUrl = updateUrl = path + url_subfix + 'waiting' + ID_SUFFIX
        options.taskType = 'waiting'
        waitingListOptions = objects.extend {}, options, {listType: 'list', taskType: 'waiting'}
        router.get getUrl, handlers.get4Process.bind handlers, options, service, entityMeta unless excludes.get
        router.get listUrl, handlers.list.bind handlers, waitingListOptions, service, entityMeta

        listUrl = path + url_subfix + 'doing'
        getUrl = path + url_subfix + 'doing' + ID_SUFFIX
        options.taskType = 'doing'
        doingListOptions = objects.extend {}, options, {listType: 'list', taskType: 'doing'}
        router.get getUrl, handlers.get4Process.bind handlers, options, service, entityMeta unless excludes.get
        router.get listUrl, handlers.list.bind handlers, doingListOptions, service, entityMeta

        listUrl = path + url_subfix + 'done'
        getUrl = path + url_subfix + 'done' + ID_SUFFIX
        options.taskType = 'done'
        doneListOptions = objects.extend {}, options, {listType: 'list', taskType: 'done'}
        router.get getUrl, handlers.get4ProcessHistory.bind handlers, options, service, entityMeta unless excludes.get
        router.get listUrl, handlers.list.bind handlers, doneListOptions, service, entityMeta

        #
        listUrl = createUrl = path + url_subfix + 'none'
        getUrl = updateUrl = path + url_subfix + 'none' + ID_SUFFIX
        options.taskType = 'none'
        noneListOptions = objects.extend {}, options, {listType: 'list'}
        router.get getUrl, handlers.get4Process.bind handlers, options, service, entityMeta unless excludes.get
        router.get listUrl, handlers.list.bind handlers, noneListOptions, service, entityMeta
        router.post createUrl, handlers.create4Process.bind handlers, options, service, entityMeta unless excludes.create
        router.put updateUrl, handlers.update.bind handlers, options, service, entityMeta unless excludes.update

        url_subfix = '/task/'
        # for task
        task_url = path + url_subfix + 'claim' + ID_SUFFIX
        router.put task_url, handlers.claim.bind handlers, options, service, entityMeta

        task_url = path + url_subfix + 'complete' + ID_SUFFIX
        router.put task_url, handlers.complete.bind handlers, options, service, entityMeta

        task_url = path + url_subfix + 'reject' + ID_SUFFIX
        router.put task_url, handlers.reject.bind handlers, options, service, entityMeta

        task_url = path + url_subfix + 'recall' + ID_SUFFIX
        router.put task_url, handlers.recall.bind handlers, options, service, entityMeta

        # 历史信息
        history_url = path + '/history' + ID_SUFFIX
        historyListOptions = objects.extend {}, options, {listType: 'list'}
        router.get history_url, handlers.listHistoryTask.bind handlers, historyListOptions, service, entityMeta

    else
        if not excludes.list
            listOptions = objects.extend {}, options, {listType: 'list'}
            router.get listUrl, handlers.list.bind handlers, listOptions, service, entityMeta

        router.get getUrl, handlers.get.bind handlers, options, service, entityMeta unless excludes.get
        router.post createUrl, handlers.create.bind handlers, options, service, entityMeta unless excludes.create
        router.put updateUrl, handlers.update.bind handlers, options, service, entityMeta unless excludes.update
        router.del removeUrl, handlers.remove.bind handlers, options, service, entityMeta unless excludes.remove
        router.post batchRemoveUrl, handlers.batchRemove.bind handlers, options, service, entityMeta unless excludes.batchRemove

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
            target[name].call target, '/' + paths.join(path, url, true), fn for url, fn of router[name + 's']

    !!router.gets['/picker']

createEntity = (clazz) ->
    c = clazz.getConstructor()
    c.newInstance()

getService = (options, entityMeta) ->
    return options.service if options.service and not _.isFunction(options.service)
    service = createService entityMeta.entityClass, entityMeta, options
    if options.service then options.service(service) else service
getAccountService = ->
    entityClass = ClassUtils.forName 'com.zyeeda.coala.commons.organization.entity.Account'
    entityMeta = entityMetaResovler.resolveEntity entityClass
    createService entityMeta.entityClass, entityMeta

getAccountById = (id) ->
    getAccountService().get id

# 根据流程定义id 查询流程定义
getProcessDefinition = mark('process').on (process, processDefinitionId) ->
    process.repository.createProcessDefinitionQuery()
        .processDefinitionId(processDefinitionId)
        .singleResult()

getJsonFilter = exports.getJsonFilter = (options, type) ->
    return {} unless options.filters
    options.filters[type] or options.filters.defaults or {}

defaultHandlers = (path, options) ->
    o =
        list: (options, service, entityMeta, request, taskType) ->
            entity = createEntity entityMeta.entityClass
            mergeEntityAndParameter options, request.params, entityMeta, 'list', entity

            result = {}
            config = {}

            objects.extend config, {listType: options.listType, pickerFeatureName: request.params.pickerFeatureName, pickerFeatureType: request.params.pickerFeatureType, pickerFiled: request.params.pickerFiled}

            appPath = request.env.servletRequest.getRealPath '/WEB-INF/app'
            config.appPath = appPath

            filters = coala.extractFilterInfo request.params
            config.filters = filters if filters

            style = options.style
            if style? and options[style]? and options[style].colModel?
                config.fields= options[style].colModel

            paginationInfo = coala.extractPaginationInfo request.params
            if paginationInfo?
                paginationInfo.listType = config.listType
                paginationInfo.pickerFeatureName = config.pickerFeatureName
                paginationInfo.pickerFeatureType = config.pickerFeatureType
                paginationInfo.pickerFiled = config.pickerFiled
                paginationInfo.fetchCount = true
                pageSize = paginationInfo.maxResults
                paginationInfo.appPath = config.appPath
                paginationInfo.restricts = config.restricts
                paginationInfo.fields = config.fields
                paginationInfo.filters = filters if filters
                paginationInfo.taskType = options.taskType if options.style is 'process'
                count = if options.style is 'process' then service.list4Process entity, paginationInfo else service.list entity, paginationInfo

                result.recordCount = count
                result.pageCount = Math.ceil count/pageSize

                delete paginationInfo.fetchCount
                objects.extend config, paginationInfo

            orderInfo = coala.extractOrderInfo request.params
            if orderInfo?.length isnt 0
                config.orderBy = orderInfo

            if options.style is 'process'
                config.taskType = options.taskType
                result.results = service.list4Process entity, config
            else
                result.results = service.list entity, config

            o = coala.generateListResult result.results, config.currentPage, config.maxResults, result.recordCount, result.pageCount
            json o, getJsonFilter(options, 'list')

        # 查询任务历史信息
        listHistoryTask: mark('process').on (process, options, service, entityMeta, request, id) ->
            entity = service.get id
            # mergeEntityAndParameter options, request.params, entityMeta, 'list', entity

            result = {}
            config = {}

            objects.extend config, {listType: options.listType, pickerFeatureName: request.params.pickerFeatureName, pickerFeatureType: request.params.pickerFeatureType, pickerFiled: request.params.pickerFiled}

            appPath = request.env.servletRequest.getRealPath '/WEB-INF/app'
            config.appPath = appPath

            filters = coala.extractFilterInfo request.params
            config.filters = filters if filters

            style = options.style
            if style? and options[style]? and options[style].colModel?
                config.fields= options[style].colModel

            htQuery = process.history.createHistoricTaskInstanceQuery()
                .processInstanceId(entity.processInstanceId)
            paginationInfo = coala.extractPaginationInfo request.params
            if paginationInfo?
                paginationInfo.listType = config.listType
                paginationInfo.pickerFeatureName = config.pickerFeatureName
                paginationInfo.pickerFeatureType = config.pickerFeatureType
                paginationInfo.pickerFiled = config.pickerFiled
                paginationInfo.fetchCount = true
                pageSize = paginationInfo.maxResults
                paginationInfo.appPath = config.appPath
                paginationInfo.restricts = config.restricts
                paginationInfo.fields = config.fields
                paginationInfo.filters = filters if filters
                count = service.list entity, paginationInfo

                result.recordCount = 1
                result.pageCount = Math.ceil count/pageSize

                delete paginationInfo.fetchCount
                objects.extend config, paginationInfo

            # hts = htQuery.list()
            # _.each hts.toArray(), (task) ->
            #     task.startTime = timeFormat.format task.startTime

            # result.results = htQuery.list()

            result.results = process.history.getHistoricTasksByProcessInstanceId entity.processInstanceId

            o = coala.generateListResult result.results, config.currentPage, config.maxResults, result.recordCount, result.pageCount
            json o,
                include:
                    # '!historicTaskInstanceEntityFilter': ''
                    historicTaskFilter: ['id', 'name', 'assignee', 'assigneeName', 'startTime', 'claimTime', 'endTime', 'comment']

        get: (options, service, entityMeta, request, id) ->
            entity = service.get id
            return notFound() if entity is null

            json entity, getJsonFilter(options, 'get')
        # 获取实体及任务信息
        get4Process: mark('process').on (process, options, service, entityMeta, request, id) ->
            entity = service.get id
            currentUser = getCurrentUser()
            # 查询待办任务
            task = process.task.createTaskQuery()
            .taskCandidateUser(currentUser)
            .processDefinitionKey(options.processDefinitionKey)
            .processInstanceId(entity.processInstanceId)
            .processVariableValueEquals('ENTITY', id)
            .singleResult()

            # 如果待办任务为空，查询在办任务
            if task is null
                task = process.task.createTaskQuery()
                .taskAssignee(currentUser)
                .processDefinitionKey(options.processDefinitionKey)
                .processVariableValueEquals('ENTITY', id)
                .singleResult()

            historicProcessInstance = process.history.createHistoricProcessInstanceQuery()
                .processInstanceId(entity.processInstanceId)
                .singleResult()
            processDefinition = getProcessDefinition(historicProcessInstance.processDefinitionId)
            e = {}

            for key, value of entity
                e[key] = value if type(value) isnt 'function'

            e = objects.extend e,
                _t_pass: '1'
                _t_taskId: task?.id
                _t_taskName: task?.name
                _t_createTime: task?.createTime
                _t_assignee: task?.assignee
                _t_assigneeName: getAccountById(task.assignee)?.accountName or task.assignee if task.assignee
                _t_rejectable: false
                _p_name: processDefinition.name
                _p_description: processDefinition.description
                _p_startTime: historicProcessInstance.startTime
                _p_endTime: historicProcessInstance.endTime
                _p_submitter: getAccountById(entity.submitter)?.accountName or entity.submitter if entity.submitter

            if task
                # 判断是否可以回退
                if task.assignee
                    eventQuery = new EventSubscriptionQueryImpl(process.runtime.commandExecutor)
                    events = eventQuery.executionId(task.executionId).list()
                    e._t_rejectable = true for event in events.toArray() when event.eventName is 'reject-from-' + task.taskDefinitionKey
                else
                    e._t_rejectable = false #任务未被认领不能进行回退
            else
                e._t_rejectable = false

            return notFound() if entity is null

            json e, getJsonFilter(options, 'get')

        # 获取实体及任务历史信息
        get4ProcessHistory: mark('process').on (process, options, service, entityMeta, request, id) ->
            entity = service.get id
            currentUser = getCurrentUser()

            historicProcessInstance = process.history.createHistoricProcessInstanceQuery()
                .processInstanceId(entity.processInstanceId)
                .singleResult()
            historicTasks = process.history.createHistoricTaskInstanceQuery()
                .processInstanceId(entity.processInstanceId)
                .taskAssignee(currentUser)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list()
                .toArray()

            processDefinition = getProcessDefinition(historicProcessInstance.processDefinitionId)
            historicTask = historicTasks[0]
            e = {}

            for key, value of entity
                e[key] = value if type(value) isnt 'function'


            e = objects.extend e,
                _t_pass: '1'
                _t_taskId: historicTask.id
                _t_taskName: historicTask.name
                _t_createTime: historicTask.startTime
                _t_endTime: historicTask.endTime
                _t_assignee: historicTask.assignee
                _t_assigneeName: getAccountById(historicTask.assignee)?.accountName or historicTask.assignee if historicTask.assignee
                _t_rejectable: false
                _t_recallable: false
                _p_name: processDefinition.name
                _p_description: processDefinition.description
                _p_startTime: historicProcessInstance.startTime
                _p_endTime: historicProcessInstance.endTime
                _p_submitter: getAccountById(entity.submitter)?.accountName or entity.submitter if entity.submitter

            # 判断是否可以回退,含有signal事件的activity 的scope属性会被设置为true
            # 在scope节点被创建时流程会暂停当前的execution，创建子execution并执行
            # 所以根据历史任务的executionId无法查询到相应的召回事件，此处需要根据processInstanceId进行查询
            # TODO 处理任务被认领的情况
            eventQuery = new EventSubscriptionQueryImpl(process.runtime.commandExecutor)
            events = eventQuery.processInstanceId(historicTask.processInstanceId).list()
            e._t_recallable = true for event in events.toArray() when event.eventName is 'recall-to-' + historicTask.taskDefinitionKey

            return notFound() if entity is null

            json e, getJsonFilter(options, 'get')

        create: (options, service, entityMeta, request) ->
            entity = createEntity entityMeta.entityClass
            mergeEntityAndParameter options, request.params, entityMeta, 'create', entity

            result = callValidation 'create', options, request, entity
            return result if result isnt true

            result = callHook 'before', 'Create', options, entityMeta, request, entity
            return result if result isnt undefined

            entity = service.create(entity)

            result = callHook 'after', 'Create', options, entityMeta, request, entity
            return result if result isnt undefined

            json entity, objects.extend getJsonFilter(options, 'create'), { status: 201 }

        create4Process: mark('process').on (process, options, service, entityMeta, request) ->
            entity = createEntity entityMeta.entityClass
            mergeEntityAndParameter options, request.params, entityMeta, 'create', entity

            result = callValidation 'create', options, request, entity
            return result if result isnt true

            result = callHook 'before', 'Create', options, entityMeta, request, entity
            return result if result isnt undefined

            # entity.processDefinitionId = options.processDefinitionKey
            entity = service.create(entity)

            result = callHook 'after', 'Create', options, entityMeta, request, entity
            # 启动流程
            # process.startProcess getCurrentUser(), options.processDefinitionKey, entity
            return result if result isnt undefined

            json entity, objects.extend getJsonFilter(options, 'create'), { status: 201 }

        update: (options, service, entityMeta, request, id) ->
            result = true
            updateIt = (entity) ->
                mergeEntityAndParameter options, request.params, entityMeta, 'update', entity

                result = callValidation 'update', options, request, entity
                return false if result isnt true

                result = callHook 'before', 'Update', options, entityMeta, request, entity
                return false if result isnt undefined
                result = true

            entity = service.update id, updateIt
            return result if result isnt true

            result = callHook 'after', 'Update', options, entityMeta, request, entity
            return result if result isnt undefined

            json entity, getJsonFilter(options, 'update')

        remove: (options, service, entityMeta, request, id) ->
            entity = service.get id
            return notFound() if entity is null

            result = callValidation 'remove', options, request, entity
            return result if result isnt true

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

            result = callValidation 'batchRemove', options, request, entities
            return result if result isnt true

            result = callHook 'before', 'BatchRemove', options, entityMeta, request, entities
            return result if result isnt undefined

            service.remove.apply service, entities

            result = callHook 'after', 'BatchRemove', options, entityMeta, request, entities
            return result if result isnt undefined

            r = (entity.id for entity in entities)
            json r

        # for task
        claim: mark('process').on (process, options, service, entityMeta, request, id) ->
            entityId = id.split('|')[0]
            taskId = id.split('|')[1]
            result = true
            updateIt = (entity) ->
                mergeEntityAndParameter options, request.params, entityMeta, 'update', entity

                result = callValidation 'update', options, request, entity
                return false if result isnt true

                result = callHook 'before', 'Update', options, entityMeta, request, entity
                return false if result isnt undefined
                result = true

            entity = service.update entityId, updateIt

            process.task.claim taskId, getCurrentUser()
            return result if result isnt true

            result = callHook 'after', 'Update', options, entityMeta, request, entity
            return result if result isnt undefined

            json entity, getJsonFilter(options, 'update')

        complete: mark('process').on (process, options, service, entityMeta, request, id) ->
            entityId = id.split('|')[0]
            taskId = id.split('|')[1]
            result = true
            updateIt = (entity) ->
                mergeEntityAndParameter options, request.params, entityMeta, 'update', entity

                result = callValidation 'update', options, request, entity
                return false if result isnt true

                result = callHook 'before', 'Update', options, entityMeta, request, entity
                return false if result isnt undefined
                result = true

            entity = service.update entityId, updateIt

            variables = objects.extend {}, process.entityToVariables(entity)
            pass = request.params._t_pass or '1'
            comment = request.params._t_comment or ''
            if pass is '1' then pass = true else pass = false
            objects.extend variables,
                '_t_pass': pass
                '_t_comment': comment

            task = process.getTask taskId
            # 保存审核意见
            process.task.addComment taskId, task.getProcessInstanceId(), comment if not _.isEmpty comment

            process.runtime.setVariables task.getExecutionId(), variables
            process.task.claim taskId, getCurrentUser() if task.assignee is null
            process.task.complete taskId

            return result if result isnt true

            result = callHook 'after', 'Update', options, entityMeta, request, entity
            return result if result isnt undefined

            json entityId

        reject: mark('process').on (process, options, service, entityMeta, request, id) ->
            taskId = id.split('|')[1]

            # 保存回退原因
            rejectReason = request.params._t_reject_reason
            if not _.isEmpty rejectReason
                task = process.getTask taskId
                process.task.addComment taskId, task.getProcessInstanceId(), rejectReason

            process.task.reject taskId
            json taskId

        recall: mark('process').on (process, options, service, entityMeta, request, id) ->
            entityId = id.split('|')[0]
            taskId = id.split('|')[1]

            entity = service.get entityId
            process.task.recall taskId

            # 保存召回原因
            recallReason = request.params._t_recall_reason
            if not _.isEmpty recallReason
                tasks = process.task.createTaskQuery()
                    .processInstanceId(entity.processInstanceId)
                    .orderByTaskCreateTime()
                    .desc()
                    .list()
                    .toArray()
                task = tasks[0];
                process.task.addComment task.id, task.processInstanceId, recallReason

            json taskId

    return o if options.disableAuthz is true or coala.disableAuthz is true

    map = list: 'show', get: 'show', create: 'add', update: 'edit', remove: 'del', batchRemove: 'del'
    p = path.replace(/^\//, '').replace(/\/$/, '')
    for key, value of map
        o[key] = mark('perms', "#{p}:#{value}").on o[key]
    o

# the reason why put the entity in the end of argument list is that,
# when update, the arguments before entity are all bound
mergeEntityAndParameter = (options, params, entityMeta, type, entity) ->
    converter = createConverter options.converters
    for key, value of params
        continue if not entityMeta.hasField key
        entity[key] = converter.convert value, entityMeta.getField(key)
    options.afterMerge? entity, type
    entity
getCurrentUser = ->
    currentUser = 'tom'
    Authentication.setAuthenticatedUserId currentUser
    currentUser
taskToVo = (task) ->
    ps = [
        'id', 'name', 'description', 'priority', 'assignee', 'processInstanceId',
        'processDefinitionId', 'createTime', 'dueDate', 'startTime', 'endTime', 'executionId'
    ]
    vo = {}
    vo[name] = task[name] for name in ps
    vo
validationGroupMapping =
    create: Create
    update: Update

callValidation = (action, options, request, entity) ->
    context = createValidationContext()
    formName = request.params['__formName__'] or 'defaults'

    customValidator = options.validators?[action]?[formName]
    if customValidator? and type(customValidator) is 'function'
        customValidator.call null, context, entity, request

    log.debug "context.isBeanValidationSkipped = #{context.isBeanValidationSkipped}"

    if not context.isBeanValidationSkipped and (action is 'create' or action is 'update')
        validator = new createValidator()
        validator.validate context, entity, validationGroupMapping[action]

    log.debug "context.hasViolations() = #{context.hasViolations()}"
    log.debug "context.violations.length = #{context.violations.length}"

    violations = context.collectViolations()
    return json violations: violations, {status: 422} if violations.length > 0
    true

callHook = (hookType, action, options, meta, request, entity) ->
    hookName = hookType + action
    formName = request.params['__formName__'] or 'defaults'

    hook = options.hooks?[hookName]?[formName]
    if hook? and type(hook) is 'function'
        try
            hook.call null, entity, request, meta
        catch e
            internalServerError e
