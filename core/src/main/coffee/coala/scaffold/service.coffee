{mark} = require 'coala/mark'
{createService} = require 'coala/service'
{type} = require 'coala/util/type'
fs = require 'fs'
{coala} = require 'coala/config'
createProcessService = require('coala/scaffold/process-service').createService

{Context} = com.zyeeda.coala.web.SpringAwareJsgiServlet
{ProcessStatusAware} = com.zyeeda.coala.entities.base
{Authentication} = org.activiti.engine.impl.identity
{ArrayList, HashSet} = java.util
{ClassUtils} = org.springframework.util
entityMetaResolver = Context.getInstance(module).getBeanByClass(com.zyeeda.coala.web.scaffold.EntityMetaResolver)

exports.createService = (entityClass, entityMeta, scaffold) ->
    baseService = createService()

    manySideUpdate = (entity, previousValues = {}) ->
        for fieldMeta in entityMeta.getFields()
            if fieldMeta.isOneToMany()
                fieldManager = baseService.createManager fieldMeta.manyType

                for e in (previousValues[fieldMeta.name] or [])
                    e[fieldMeta.mappedBy] = null
                    fieldManager.merge e

                values = entity[fieldMeta.name]
                if values != null and values.isEmpty and not values.isEmpty()
                    for value in values.toArray()
                        value = fieldManager.merge value
                        value[fieldMeta.mappedBy] = entity
            else if fieldMeta.isManyToManyTarget()
                fieldManager = baseService.createManager fieldMeta.manyToManyOwnerType

                for e in (previousValues[fieldMeta.name] or [])
                    e[fieldMeta.mappedBy].remove entity
                    fieldManager.merge e

                values = entity[fieldMeta.name]
                if values != null and values.isEmpty and not values.isEmpty()
                    for value in values.toArray()
                        value = fieldManager.merge value
                        list = value[fieldMeta.mappedBy]
                        if not list
                            value[fieldMeta.mappedBy] = list = if ClassUtils.isAssignable fieldMeta.type, ArrayList then new ArrayList() else new HashSet()
                        list.add entity
            else if fieldMeta.isManyToManyOwner()
                fieldManager = baseService.createManager fieldMeta.manyToManyOwnerType
                values = entity[fieldMeta.name]
                if values != null and values.isEmpty and not values.isEmpty()
                    vs = (fieldManager.merge value for value in values.toArray())
                    values.clear()
                    values.add v for v in vs
        true

    startProcess = mark('beans', 'runtimeService').on (runtimeService, entity, manager) ->
        currentUser = 'tom'
        Authentication.setAuthenticatedUserId currentUser

        processId = scaffold.boundProcessId
        if type(processId) is 'function'
            processId = processId entity
        variables =
            ENTITY: entity.id
            ENTITYCLASS: entityMeta.entityClass.getName()
            SUBMITTER: currentUser
        for property, value of entity
            variables[property] = value if value isnt undefined and type(value) isnt 'function' and value isnt null

        processInstance = runtimeService.startProcessInstanceByKey processId, variables
        if entity instanceof ProcessStatusAware
            entity.processId = processId
            entity.processInstanceId = processInstance.id
            entity.status = processId
            manager.merge entity

        processInstance

    service =
        entityClass: entityClass

        list: (entity, options) ->
            manager = baseService.createManager service.entityClass
            if options.filters
                manager.findByEntity options
            else
                manager.findByExample entity, options

        get: (id) ->
            manager = baseService.createManager service.entityClass
            manager.find id

        create: mark('tx').on (entity) ->
            manager = baseService.createManager service.entityClass
            backup = {}
            for fm in entityMeta.getFields()
                if fm.isManyToManyTarget() or fm.isOneToMany() or fm.isManyToManyOwner()
                    backup[fm.name] = entity[fm.name]
                    entity[fm.name] = null

            entity = manager.save entity

            entity[key] = value for key, value of backup
            manySideUpdate entity

            if scaffold.boundProcessId
                startProcess entity, manager
            entity

        update: mark('tx').on (id, fn) ->
            manager = baseService.createManager service.entityClass
            entity = manager.find id

            pre = {}
            for fieldMeta in entityMeta.getFields()
                if fieldMeta.isOneToMany() or fieldMeta.isManyToManyTarget() or fieldMeta.isManyToManyOwner()
                    pre[fieldMeta.name] = entity[fieldMeta.name].toArray()

            return null if fn(entity, service) is false
            manySideUpdate entity, pre
            entity

        ###
        remove: mark('tx').on (id...) ->
            manager = baseService.createManager service.entityClass
            manager.removeById.apply manager, id
        ###

        remove: mark('tx').on (entities...) ->
            manager = baseService.createManager service.entityClass
            manager.remove.apply manager, entities

    service
