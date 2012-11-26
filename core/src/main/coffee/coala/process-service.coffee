{type, objects} = require 'coala/util'
{createManager} = require 'coala/manager'

{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet
{HashMap} = java.util
{TaskService} = com.zyeeda.framework.bpm
{ProcessEngine} = org.activiti.engine
{ProcessStatusAware} = com.zyeeda.framework.entities.base
{ClassUtils} = org.springframework.util
{EntityMetaResolver} = com.zyeeda.framework.web.scaffold

context = Context.getInstance(module)

entityToVariables = (entity) ->
    resolver = context.getBeanByClass EntityMetaResolver
    clazz = entity.getClass()
    return {} if not clazz
    meta = resolver.resolveEntity clazz
    result = {}
    for key, value of entity
        result[key] = value if meta.hasField key
    result

exports.createService = ->
    taskService = context.getBeanByClass TaskService
    factory = context.getBeanByClass ProcessEngine

    service =
        task: taskService
        repository: factory.getRepositoryService()
        runtime: factory.getRuntimeService()
        form: factory.getFormService()
        history: factory.getHistoryService()
        identity: factory.getIdentityService()
        management: factory.getManagementService()

        startProcess: (user, processKey, entity = {}, args = {}) ->
            variables = objects.extend {},
                SUBMITTER: user
                ENTITY: entity.id
                ENTITYCLASS: entity.getClass()?.getName()
            , entityToVariables(entity), args

            processInstance = service.runtime.startProcessInstanceByKey processKey, variables
            if entity instanceof ProcessStatusAware
                entity.processId = processId
                entity.processInstanceId = processInstance.id
                entity.status = processId
                entity.submitter = user

            processInstance

        getTask: (id) ->
            service.task.createTaskQuery().taskId(id).singleResult()

        getTaskRelatedEntity: (id) ->
            task = service.getTask id
            entityId = service.runtime.getVariable(task.getExecutionId(), 'ENTITY')
            entityClass = ClassUtils.forName service.runtime.getVariable(task.getExecutionId(), 'ENTITYCLASS')
            manager = createManager entityClass
            manager.find entityId

        getHistoricTaskRelatedEntity: (id) ->
            if type(id) is 'string'
                task = service.history.createHistoricTaskInstanceQuery().taskId(id).singleResult()
            else
                task = id

            vs = service.history.createHistoricDetailQuery().processInstanceId(task.processInstanceId).variableUpdates().list()
            entityClass = null
            enittyId = null
            for v in vs.toArray()
                entityClass = v.textValue if v.name is 'ENTITYCLASS'
                entityId = v.textValue if v.name is 'ENTITY'
            entityClass = ClassUtils.forName entityClass
            manager = createManager entityClass
            manager.find entityId

        findHistoricProcessByInvolvedUser: (userId) ->
            manager = createManager()
            manager.findHistoricProcessByInvolvedUser user: userId

        completeTask: (id, userId, entity = {}, args = {}) ->
            variables = objects.extend {}, entityToVariables(entity), args
            task = service.getTask id
            service.runtime.setVariables task.getExecutionId(), variables
            service.task.claim id, userId
            service.task.complete id

        __noSuchMethod__: (name, args) ->
            throw new Error("no such method: #{name}") if not taskService[name]
            taskService[name].apply taskService, args
