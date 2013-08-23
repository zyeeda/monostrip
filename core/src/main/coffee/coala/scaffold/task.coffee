{createRouter, getJsonFilter} = require 'coala/router'
{mark} = require 'coala/mark'
{json} = require 'coala/response'
{generateForms} = require 'coala/scaffold/form-generator'
{requireScaffoldConfig} = require 'coala/scaffold/router'
objects = require 'coala/util/objects'
{coala} = require 'coala/config'

{TaskService} = com.zyeeda.coala.bpm
{EntityMetaResolver} = com.zyeeda.coala.web.scaffold
{ClassUtils} = org.springframework.util
{Authentication} = org.activiti.engine.impl.identity
{EventSubscriptionQueryImpl} = org.activiti.engine.impl

router = exports.router = createRouter()

taskOrderMap =
    id: 'TaskId'
    name: 'TaskName'
    description: 'TaskDescription'
    priority: 'TaskPriority'
    assignee: 'TaskAssignee'
    createTime: 'TaskCreateTime'
    dueDate: 'TaskDueDate'

taskToVo = (task) ->
    ps = [
        'id', 'name', 'description', 'priority', 'owner', 'assignee', 'processInstanceId', 'processDefinitionId', 'createTime', 'dueDate',
        'startTime', 'endTime', 'executionId'
    ]
    vo = {}
    vo[name] = task[name] for name in ps
    vo

processToVo = (process) ->
    ps = [
        'id', 'processInstanceId', 'processDefinitionId', 'startTime', 'endTime', 'durationInMillis', 'deleteReason',
        'endActivityId', 'businessKey', 'startUserId', 'startActivityId', 'superProcessInstanceId'
    ]
    vo = {}
    vo[name] = process[name] for name in ps
    vo

processDefinitionToVo = (process) ->
    ps = ['id', 'name', 'category', 'key', 'version']
    vo = {}
    vo[name] = process[name] for name in ps
    vo

getCurrentUser = ->
    currentUser = 'tom'
    Authentication.setAuthenticatedUserId currentUser
    currentUser

taskQuery = (createQuery, request, process, resolver, noExtra) ->
    configs = coala.extractPaginationInfo request.params
    orders = coala.extractOrderInfo request.params

    result = {}
    if configs?
        pageSize = configs.maxResults
        count = createQuery().count()
        result.recordCount = count
        result.pageCount = Math.ceil count/pageSize

    query = createQuery()
    if orders
        for order in orders
            query['orderBy' + taskOrderMap[key]]()[value]() for key, value of order

    if configs
        tasks = query.listPage configs.firstResult, configs.maxResults
    else
        task = query.list()

    results = (taskToVo(task) for task in tasks.toArray())
    filter = {}
    if noExtra isnt true
        for o in results
            o.entity = process.getTaskRelatedEntity(o.id)
            entityClass = o.entity.getClass()
            meta = resolver.resolveEntity entityClass
            opts = requireScaffoldConfig meta.path
            f = getJsonFilter opts, 'list'
            objects.extend filter, f
            o.process = process.repository.createProcessDefinitionQuery().processDefinitionId(o.processDefinitionId).singleResult()
            o.process = processDefinitionToVo o.process

            eventQuery = new EventSubscriptionQueryImpl(process.runtime.commandExecutor)
            events = eventQuery.executionId(o.executionId).list()
            o.isRejectable = true for event in events.toArray() when event.eventName.indexOf('reject-') is 0
    else
        for o in results
            o.isRevokable = false
            continue if o.endTime isnt null
            execution = process.runtime.createExecutionQuery().executionId(o.executionId).singleResult()
            parentId = execution.parentId
            continue if parentId is null

            eventQuery = new EventSubscriptionQueryImpl(process.runtime.commandExecutor)
            events = eventQuery.executionId(o.executionId).list()
            isRevokable = true for event in events.toArray() when event.eventName.indexOf('revoke-') is 0
            continue if not isRevokable

            ts = process.history.createHistoricTaskInstanceQuery().processInstanceId(o.processInstanceId).executionId(parentId).list()
            o.isRevokable = true for t in ts.toArray() when t.assignee is getCurrentUser()

    configs || (configs = {})
    o = coala.generateListResult results, configs.currentPage, configs.maxResults, result.recordCount, result.pageCount
    json o, filter

processQuery = (process, request, resolver) ->
    configs = coala.extractPaginationInfo(request.params) or {}
    orders = coala.extractOrderInfo request.params
    status = request.params.status
    params = pagination: configs

    if orders
        for order in orders
            for key, value of order
                params.orderField = key
                params.order = value

    result = process.findHistoricProcessByInvolvedUser 'tom', status, params
    items = (processToVo(p) for p in result.items.toArray())
    result.items = items
    filter = {}
    for o in result.items
        o.entity = process.getHistoricProcessRelatedEntity(o.processInstanceId)
        entityClass = o.entity.getClass()
        meta = resolver.resolveEntity entityClass
        opts = requireScaffoldConfig meta.path
        f = getJsonFilter opts, 'list'
        objects.extend filter, f

        p = process.repository.createProcessDefinitionQuery().processDefinitionId(o.processDefinitionId).singleResult()
        p = processDefinitionToVo p
        delete p.id
        objects.extend o, p

    o = coala.generateListResult result.items, configs.currentPage, configs.maxResults, result.recordCount, result.pageCount
    json o, filter


router.get '/', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request) ->
    currentUser = getCurrentUser()

    taskQuery ->
        process.task.createTaskQuery().taskInvolvedUser(currentUser)
    , request, process, resolver

router.post '/batch/audit', mark('process').on (process, request) ->
    currentUser = getCurrentUser()

    ids = request.params.ids
    process.completeTask id, currentUser for id in ids
    json ids

router.post '/batch/reject', mark('process').on (process, request) ->
    currentUser = getCurrentUser()

    ids = request.params.ids || []
    comment = request.params.comment
    if comment
        process.addComment taskId, null, comment for taskId in ids

    process.reject taskId for taskId in ids
    json ids

router.put '/reject/:taskId', mark('process').on (process, request, taskId) ->
    currentUser = getCurrentUser()

    comment = request.params.comment
    if comment
        process.addComment taskId, null, comment

    process.reject taskId
    json taskId

router.get '/revoke/:taskId', mark('process').on (process, request, taskId) ->
    currentUser = getCurrentUser()

    process.revoke taskId
    json taskId

router.put '/:taskId', mark('process').on (process, request, taskId) ->
    currentUser = getCurrentUser()

    entity = process.getTaskRelatedEntity taskId
    params = {}
    for param, value of request.params
        if param.indexOf('entity.') != -1
            name = param.substring 7
            params[name] = value
    router.resolveEntity entity, params

    process.completeTask taskId, currentUser, entity

    json taskId

router.get '/list/:processId', mark('process').on (process, request, processId) ->
    currentUser = getCurrentUser()

    taskQuery ->
        process.history.createHistoricTaskInstanceQuery().processInstanceId(processId).orderByTaskId().desc()
    , request, process , null, true

router.get '/:taskId', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request, taskId) ->
    currentUser = getCurrentUser()

    task = process.getTask taskId
    processDefinition = process.repository.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult()
    p = process.history.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult()
    entity = process.getTaskRelatedEntity taskId
    entityClass = entity.getClass()
    meta = resolver.resolveEntity entityClass
    options = requireScaffoldConfig meta.path

    pvo = processToVo p
    pdvo = processDefinitionToVo processDefinition
    delete pdvo.id
    objects.extend pvo, pdvo

    eventQuery = new EventSubscriptionQueryImpl(process.runtime.commandExecutor)
    events = eventQuery.executionId(task.executionId).list()

    task = taskToVo(task)
    task.isRejectable = true for event in events.toArray() when event.eventName.indexOf('reject-') is 0


    json {entity: entity, process: pvo, task: task}, getJsonFilter(options, 'get')

router.get '/completed', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request) ->
    currentUser = getCurrentUser()

    processQuery process, request, resolver

router.get '/completed/:id', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request, id) ->
    currentUser = getCurrentUser()

    p = process.history.createHistoricProcessInstanceQuery().processInstanceId(id).singleResult()
    processDefinition = process.repository.createProcessDefinitionQuery().processDefinitionId(p.getProcessDefinitionId()).singleResult()
    entity = process.getHistoricProcessRelatedEntity id
    meta = resolver.resolveEntity entity.getClass()
    options = requireScaffoldConfig meta.path

    pvo = processToVo p
    pdvo = processDefinitionToVo processDefinition
    delete pdvo.id
    objects.extend pvo, pdvo
    json {entity: entity, process: pvo}, getJsonFilter(options, 'get')

router.get '/configuration/forms/:taskId', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request, taskId) ->
    currentUser = getCurrentUser()

    if taskId.charAt(0) is 'p'
        isHistoric = true
        taskId = taskId.substring(1)

        p = process.history.createHistoricProcessInstanceQuery().processInstanceId(taskId).singleResult()
        pd = process.repository.createProcessDefinitionQuery().processDefinitionId(p.getProcessDefinitionId()).singleResult()
        formName = pd.key

        vs = process.history.createHistoricDetailQuery().processInstanceId(taskId).variableUpdates().list()
        entityClass = null
        for v in vs.toArray()
            entityClass = v.textValue if v.name is 'ENTITYCLASS'
    else
        isHistoric = false
        task = process.getTask taskId
        entityClass = process.runtime.getVariable(task.getExecutionId(), 'ENTITYCLASS')
        formName = task.taskDefinitionKey

    entityMeta = resolver.resolveEntity ClassUtils.forName(entityClass, null)

    path = entityMeta.path
    options = requireScaffoldConfig path
    form = generateForms(entityMeta, options.labels, options.forms, options.fieldGroups, formName, options)

    processTab =
        title: 'Process'
        groups: ['process-info']
    taskTab =
        title: 'Task'
        groups: ['task-info']

    if form.tabs
        form.tabs.push processTab
        form.tabs.push taskTab
    else
        groupNames = (name for name, value of form.fieldGroups)
        form.tabs = [{
            title: 'Form',
            groups: groupNames
        }, processTab, taskTab]

    for name, value of form.fieldGroups
        field.name = 'entity.' + field.name for field in value

    form.fieldGroups['process-info'] = []
    form.fieldGroups['task-info'] = []

    form.groups.push name: 'process-info', readOnly: true
    form.groups.push name: 'task-info', readOnly: true
    newField = (name, label) ->
        name: name
        label: label
        type: 'string'
        readOnly: true
        colspan: 1
        rowspan: 1

    newProcessField = (name, label) ->
        o = newField 'process.' + name, label
        form.fieldGroups['process-info'].push o
        o

    newTaskField = (name, label) ->
        o = newField 'task.' + name, label
        form.fieldGroups['task-info'].push o
        o

    newProcessField 'name', 'Process Name'
    newProcessField 'version', 'Version'
    newProcessField 'startUserId', 'Submitter'
    newProcessField 'startTime', 'Start Time'

    if isHistoric
        g.readOnly = true for g in form.groups

        newProcessField 'endTime', 'End Time'
        newProcessField 'durationInMillis', 'Duration'

        form.fieldGroups['task-info'].push type: 'feature', path: 'coala:task-grid', options:
            model: 'tasks/list/' + taskId
            columns: [
                name: 'id', header: 'ID'
            ,
                name: 'name', header: 'Task Name'
            ,
                name: 'startTime', header: 'Start Time'
            ,
                name: 'endTime', header: 'End Time'
            ,
                name: 'assignee', header: 'Assignee'
            ,
                name: 'isRevokable', header: 'Revokable'
            ]
    else
        newTaskField 'name', 'Task Name'
        newTaskField 'createTime', 'Create Time'
        newTaskField 'dueDate', 'Due Date'
        newTaskField 'owner', 'Owner'
        newTaskField 'description', 'Description'

    newTaskField 'task.dueDate', 'Due Date'

    json form
