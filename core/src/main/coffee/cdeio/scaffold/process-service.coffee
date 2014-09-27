{type} = require 'cdeio/util/type'
objects = require 'cdeio/util/objects'
{createManager} = require 'cdeio/manager'

{Context} = com.zyeeda.cdeio.web.SpringAwareJsgiServlet
{HashMap} = java.util
{TaskService} = com.zyeeda.cdeio.bpm
{HistoryService} = com.zyeeda.cdeio.bpm
{ProcessEngine} = org.activiti.engine
{ProcessStatusAware} = com.zyeeda.cdeio.entities.base
{ClassUtils} = org.springframework.util
{EntityMetaResolver} = com.zyeeda.cdeio.web.scaffold
{HistoricProcess, HistoricTask} = com.zyeeda.cdeio.bpm.mapping

context = Context.getInstance(module)

entityToVariables = (entity) ->
    return {} if not entity
    resolver = context.getBeanByClass EntityMetaResolver
    clazz = entity.getClass()
    return {} if not clazz
    meta = resolver.resolveEntity clazz
    result = {}
    for key, value of entity
        result['_e_' + key] = value if meta.hasField key
    result

exports.createService = ->
    taskService = context.getBeanByClass TaskService
    historyService = context.getBeanByClass HistoryService
    factory = context.getBeanByClass ProcessEngine

    service =
        task: taskService
        repository: factory.getRepositoryService()
        runtime: factory.getRuntimeService()
        form: factory.getFormService()
        # history: factory.getHistoryService()
        history: historyService
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

        getHistoricProcessRelatedEntity: (processInstanceId) ->
            vs = service.history.createHistoricDetailQuery().processInstanceId(processInstanceId).variableUpdates().list()
            entityClass = null
            enittyId = null
            for v in vs.toArray()
                entityClass = v.textValue if v.name is 'ENTITYCLASS'
                entityId = v.textValue if v.name is 'ENTITY'
            entityClass = ClassUtils.forName entityClass
            manager = createManager entityClass
            manager.find entityId

        findHistoricProcessByInvolvedUser: (userId, status, options) ->
            manager = createManager()
            manager.mixin
                findHistoricProcessByInvolvedUser: (em, userId, status, params) ->
                    builder = em.getCriteriaBuilder()
                    sql = if params.count then builder.createQuery(java.lang.Long) else builder.createQuery(HistoricProcess)

                    hp = sql.from HistoricProcess
                    ht = sql.from HistoricTask
                    if params.count
                        sql.select builder.countDistinct hp
                    else
                        sql.distinct true
                        sql.select hp

                    ps = []
                    ps[0] = builder.equal(hp.get('processInstanceId'), ht.get('processInstanceId'))
                    ps[1] = ht.get('endTime').isNotNull()
                    user = builder.parameter java.lang.String, 'user'
                    ps[2] = builder.equal(ht.get('assignee'), user)
                    if status is 'finished'
                        ps[3] = hp.get('endTime').isNotNull()
                    else if status is 'unfinished'
                        ps[3] = hp.get('endTime').isNull()

                    cause = builder.and.apply(builder, ps)
                    sql.where(cause)

                    if not params.count and params.order
                        sql.orderBy builder[params.order] hp.get params.orderField

                    query = em.createQuery sql
                    query.setParameter 'user', userId
                    if params.count
                        query.getSingleResult()
                    else
                        if params.pagination?.maxResults
                            query.setMaxResults(params.pagination.maxResults)
                            query.setFirstResult(params.pagination.firstResult)
                        query.getResultList()

            result = {}
            if options.pagination?.maxResults
                pageSize = options.pagination.maxResults
                options.count = true
                count = manager.findHistoricProcessByInvolvedUser userId, status, options
                result.recordCount = count
                result.pageCount = Math.ceil count/pageSize
                delete options.count

            result.items = manager.findHistoricProcessByInvolvedUser userId, status, options
            result

        completeTask: (id, userId, entity, args = {}) ->
            variables = objects.extend {}, entityToVariables(entity), args
            task = service.getTask id
            service.runtime.setVariables task.getExecutionId(), variables
            service.task.claim id, userId
            service.task.complete id

        entityToVariables: entityToVariables
        __noSuchMethod__: (name, args) ->
            throw new Error("no such method: #{name}") if not taskService[name]
            taskService[name].apply taskService, args
