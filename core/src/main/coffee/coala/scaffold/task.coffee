{createRouter, getJsonFilter} = require 'coala/router'
{mark} = require 'coala/mark'
{json} = require 'coala/response'
{generateForms} = require 'coala/scaffold/form-generator'
{requireScaffoldConfig} = require 'coala/scaffold/router'
{objects} = require 'coala/util'
{coala} = require 'coala/config'

{TaskService} = com.zyeeda.framework.bpm
{EntityMetaResolver} = com.zyeeda.framework.web.scaffold
{ClassUtils} = org.springframework.util

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
        'startTime', 'endTime'
    ]
    vo = {}
    vo[name] = task[name] for name in ps
    vo

processDefinitionToVo = (process) ->
    ps = ['id', 'name', 'category', 'key', 'version']
    vo = {}
    vo[name] = process[name] for name in ps
    vo

taskQuery = (createQuery, request) ->
    configs = coala.extractPaginationInfo request.params
    orders = coala.extractOrderInfo request.params
    result = {}
    if configs?
        configs.fetchCount = true
        pageSize = configs.maxResults
        count = createQuery().count()
        result.recordCount = count
        result.pageCount = Math.ceil count/pageSize
        delete configs.fetchCount

    query = createQuery()
    if orders
        for order in orders
            query['orderBy' + taskOrderMap[key]]()[value]() for key, value of order

    if configs
        tasks = query.listPage configs.firstResult, configs.maxResults
    else
        task = query.list()

    results = (taskToVo(task) for task in tasks.toArray())
    configs || (configs = {})
    o = coala.generateListResult results, configs.currentPage, configs.maxResults, result.recordCount, result.pageCount
    json o

router.get '/', mark('process').on (process, request) ->
    currentUser = 'tom'
    taskQuery ->
        process.task.createTaskQuery().taskInvolvedUser(currentUser)
    , request

router.get '/reject/:taskId', mark('process').on (process, request, taskId) ->
    process.reject taskId
    json taskId

router.get '/revoke/:taskId', mark('process').on (process, request, taskId) ->
    process.revoke taskId
    json taskId

router.put '/:taskId', mark('process').on (process, request, taskId) ->
    entity = process.getTaskRelatedEntity taskId
    params = {}
    for param, value of request.params
        if param.indexOf('entity.') != -1
            name = param.substring 7
            params[name] = value
    router.resolveEntity entity, params

    currentUser = 'tom'
    process.completeTask taskId, currentUser, entity

    json taskId

router.get '/:taskId', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request, taskId) ->
    task = process.getTask taskId
    processDefinition = process.repository.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult()
    entity = process.getTaskRelatedEntity taskId
    entityClass = entity.getClass()
    meta = resolver.resolveEntity entityClass
    options = requireScaffoldConfig meta.path
    json {entity: entity, process: processDefinitionToVo(processDefinition), task: taskToVo(task)}, getJsonFilter(options, 'get')

router.get '/completed', mark('process').on (process, request) ->
    currentUser = 'tom'
    taskQuery ->
        process.history.createHistoricTaskInstanceQuery().taskAssignee(currentUser).finished()
    , request

router.get '/completed/:taskId', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request, taskId) ->
    task = process.history.createHistoricTaskInstanceQuery().taskId(taskId).singleResult()
    processDefinition = process.repository.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult()
    entity = process.getHistoricTaskRelatedEntity task
    meta = resolver.resolveEntity entity.getClass()
    options = requireScaffoldConfig meta.path

    json {entity: entity, process:processDefinitionToVo(processDefinition), task: taskToVo(task)}, getJsonFilter(options, 'get')

router.get '/completed/configuration/forms/:taskId', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request, taskId) ->


router.get '/comments/:taskId',  mark('process').on (process, request, taskId) ->
    json process.getComments(taskId)

router.get '/configuration/feature', ->
    json views: [{name: 'views:operators', region: 'operators'}, {name: 'views:grid', region: 'grid'}]

router.get '/configuration/operators', ->
    json audit: {label: 'Audit', icon: 'icon-plus'}

router.get '/configuration/grid', ->
    json
        colModel: [
            {name: 'id', label: 'ID'},
            {name: 'name', label: 'Name'}
        ]

router.get '/configuration/forms/:taskId', mark('process').mark('beans', EntityMetaResolver).on (process, resolver, request, taskId) ->
    task = process.getTask taskId
    isHistoric = true
    if task
        isHistoric = false
        entityClass = process.runtime.getVariable(task.getExecutionId(), 'ENTITYCLASS')
    else
        task = process.history.createHistoricTaskInstanceQuery().taskId(taskId).singleResult()
        vs = process.history.createHistoricDetailQuery().processInstanceId(task.processInstanceId).variableUpdates().list()
        entityClass = null
        for v in vs.toArray()
            entityClass = v.textValue if v.name is 'ENTITYCLASS'

    entityMeta = resolver.resolveEntity ClassUtils.forName(entityClass, null)

    path = entityMeta.path
    options = requireScaffoldConfig path
    form = generateForms(entityMeta, options.labels, options.forms, options.fieldGroups, task.taskDefinitionKey, options)

    processTab =
        title: 'Process',
        groups: ['process-info']

    if form.tabs
        form.tabs.push processTab
    else
        groupNames = (name for name, value of form.groups)
        form.tabs = [{
            title: 'Form',
            groups: groupNames
        }, processTab]

    form.groups['process-info'] =
        label: null
        columns: 1

    field.name = 'entity.' + field.name for field in form.fields
    newField = (name, label) ->
        name: name
        group: 'process-info'
        label: label
        type: 'string'
        readOnly: true
        colspan: 1
        rowspan: 1

    fields = [
        newField 'process.name', 'Process Name'
        newField 'task.name', 'Task Name'
    ]
    if isHistoric
        fields.push newField 'task.startTime', 'Start Time'
        fields.push newField 'task.endTime', 'End Time'
    else
        fields.push newField 'task.createTime', 'Create Time'

    fields.push newField 'task.dueDate', 'Due Date'


    form.fields = form.fields.concat fields

    json form
