{mark} = require 'coala/mark'
{createService} = require 'coala/service'
{type} = require 'coala/util/type'
upload = require 'coala/util/upload'
fs = require 'fs'
_ = require 'underscore'
{coala} = require 'coala/config'
createProcessService = require('coala/scaffold/process-service').createService

{Context} = com.zyeeda.coala.web.SpringAwareJsgiServlet
{ProcessStatusAware} = com.zyeeda.coala.commons.annotation.scaffold
{Authentication} = org.activiti.engine.impl.identity
{ArrayList, HashSet} = java.util
{ClassUtils} = org.springframework.util
{Attachment} = com.zyeeda.coala.commons.resource.entity
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

        processDefinitionId = scaffold.boundProcessId
        if type(processDefinitionId) is 'function'
            processDefinitionId = processDefinitionId entity
        variables =
            ENTITY: entity.id
            ENTITYCLASS: entity.getClass()?.getName()
            SUBMITTER: currentUser
        for property, value of entity
            variables[property] = value if value isnt undefined and type(value) isnt 'function' and value isnt null

        processInstance = runtimeService.startProcessInstanceByKey processDefinitionId, variables

        # 写入流程实例id到实体中
        entity.processInstanceId = processInstance.processInstanceId
        if entity instanceof ProcessStatusAware
            entity.processDefinitionId = processDefinitionId
            entity.processInstanceId = processInstance.id
            entity.submitter = currentUser
            # entity.status = '开始'
            manager.merge entity
        else
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

        # 查询流程数据
        list4Process: (entity, options) ->
            manager = baseService.createManager service.entityClass
            if options.taskType is 'waiting'
                currentUser = getCurrentUser()
                accountClass = ClassUtils.forName 'com.zyeeda.coala.commons.organization.entity.Account'   
                accountManager = baseService.createManager accountClass
                account = accountManager.find currentUser

                groupIds = []
                departments = []
                roles = []
                departments = getParentDepatments account.department if account.department
                roles = account.roles.toArray() if account.roles
                departmentIds = _.map departments, (department) ->
                    "'" + department.id + "'"
                roleIds = _.map roles, (role) ->
                    "'" + role.id + "'"
                _.each departmentIds, (id) ->
                    groupIds.push id
                _.each roleIds, (id) ->
                    groupIds.push id
                options.groupIds = groupIds

            manager.findByEntity4Process options

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
                if entity[fm.name] and entity[fm.name] instanceof Attachment
                    upload.commitAttachment entity[fm.name].id

            entity = manager.save entity

            entity[key] = value for key, value of backup
            manySideUpdate entity

            # 启动流程
            if scaffold.boundProcessId
                processInstanceId = startProcess entity, manager
            entity

        update: mark('tx', { needStatus: true }).on (txStatus, id, fn) ->
            manager = baseService.createManager service.entityClass
            entity = manager.find id

            pre = {}
            attachments = {}
            for fieldMeta in entityMeta.getFields()
                if fieldMeta.isOneToMany() or fieldMeta.isManyToManyTarget() or fieldMeta.isManyToManyOwner()
                    pre[fieldMeta.name] = entity[fieldMeta.name].toArray()
                if entity[fieldMeta.name] and entity[fieldMeta.name] instanceof Attachment
                    attachments[fieldMeta.name] = entity[fieldMeta.name].id

            if fn(entity, service) is false
                txStatus.setRollbackOnly()
                null
            for key, value of attachments
                if value
                    if not entity[key]
                        upload.remove value
                    else if value isnt entity[key].id
                        upload.commitAttachment entity[key].id
                        upload.remove value
                    else
                        upload.commitAttachment entity[key].id
            manySideUpdate entity, pre
            entity

        remove: mark('tx').on (entities...) ->
            manager = baseService.createManager service.entityClass
            manager.remove.apply manager, entities

    service
getCurrentUser = ->
    currentUser = 'tom'
    Authentication.setAuthenticatedUserId currentUser
    currentUser

# 递归查询父部门
# TODO 可能存在性能问题
getParentDepatments = (department, parents = []) ->
    parents.push department
    if department.parent
        getParentDepatments department.parent, parents

    parents        
