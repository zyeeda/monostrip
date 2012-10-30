
{mark} = require 'coala/mark'
{createService} = require 'coala/service'
{type} = require 'coala/util'

{ProcessStatusAware} = com.zyeeda.framework.entities.base

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
                        value[fieldMeta.mappedBy] = entity
                        fieldManager.merge value
            else if fieldMeta.isManyToManyTarget()
                fieldManager = baseService.createManager fieldMeta.manyToManyOwnerType

                for e in (previousValues[fieldMeta.name] or [])
                    e[fieldMeta.mappedBy].remove entity
                    fieldManager.merge e

                values = entity[fieldMeta.name]
                if values != null and values.isEmpty and not values.isEmpty()
                    for value in values.toArray()
                        value[fieldMeta.mappedBy].add entity
                        fieldManager.merge value

    startProcess = mark('beans', 'runtimeService').on (runtimeService, entity, manager) ->
        processId = scaffold.boundProcessId
        variables =
            ENTITY: entity.id
            ENTITYCLASS: entityMeta.entityClass.getName()
            SUBMITTER: 'current user id'
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
            manager.findByExample entity, options

        get: (id) ->
            manager = baseService.createManager service.entityClass
            manager.find id

        create: mark('tx').on (entity) ->
            manager = baseService.createManager service.entityClass
            entity = manager.save entity
            manySideUpdate entity

            if scaffold.boundProcessId
                startProcess entity, manager
            entity

        update: mark('tx').on (entity) ->
            manager = baseService.createManager service.entityClass

            pre = {}
            for fieldMeta in entityMeta.getFields()
                if fieldMeta.isOneToMany() or fieldMeta.isManyToManyTarget()
                    pre[fieldMeta.name] = entity[fieldMeta.name].toArray()

            manager.flush()
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
