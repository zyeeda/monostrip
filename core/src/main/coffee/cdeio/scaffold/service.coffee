{mark} = require 'cdeio/mark'
{createService} = require 'cdeio/service'
{type} = require 'cdeio/util/type'
upload = require 'cdeio/util/upload'
fs = require 'fs'
_ = require 'underscore'
{cdeio} = require 'cdeio/config'
createProcessService = require('cdeio/scaffold/process-service').createService
logger = require('ringo/logging').getLogger module.id

{Context} = com.zyeeda.cdeio.web.SpringAwareJsgiServlet
{ProcessStatusAware} = com.zyeeda.cdeio.commons.annotation.scaffold
{Authentication} = org.activiti.engine.impl.identity
{SecurityUtils} = org.apache.shiro
{ArrayList, HashSet} = java.util
{ClassUtils} = org.springframework.util
{Attachment} = com.zyeeda.cdeio.commons.resource.entity
entityMetaResolver = Context.getInstance(module).getBeanByClass(com.zyeeda.cdeio.web.scaffold.EntityMetaResolver)

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

    cascadeSave = (mgr, databaseEntity, requestEntity, etClass, preAttachment) ->
        entityMeta = entityMetaResolver.resolveEntity etClass

        for fieldMeta in entityMeta.getFields()
            fieldName = fieldMeta.name

            dbEntityFieldValue = databaseEntity[fieldName]
            reqEntityFieldValue = requestEntity[fieldName]

            logger.debug "before continue with databaseEntity = #{databaseEntity} fieldName = #{fieldName} dbEntityFieldValue = #{dbEntityFieldValue} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)}"

            logger.debug "before continue if condition result #{not reqEntityFieldValue}  #{not dbEntityFieldValue}"

            continue if not reqEntityFieldValue or not dbEntityFieldValue

            logger.debug "after continue with databaseEntity = #{databaseEntity} fieldName = #{fieldName}"

            if dbEntityFieldValue instanceof Attachment
                # 如果编辑操作之前有附件，并且跟编辑之后的附件不一致，则删除原附件
                if preAttachment[fieldName] && dbEntityFieldValue.id != preAttachment[fieldName]
                    upload.deleteAttachment preAttachment[fieldName]

                if dbEntityFieldValue.id
                    upload.commitAttachment dbEntityFieldValue.id
                else
                    databaseEntity[fieldName] = null
                continue

            # fieldMeta 与 databaseEntity 多对一关联，Many处关联One
            if fieldMeta.entity
                fieldType = fieldMeta.type
                fieldMgr = baseService.createManager fieldType

                logger.debug "into cascadeSave fieldMeta.entity with databaseEntity = #{databaseEntity} fieldName = #{fieldName} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)} fieldType = #{fieldType} reqEntityFieldValueId = #{reqEntityFieldValue.id}"

                # 由页面操作的数据在出现关联时继续深入级联保存
                if reqEntityFieldValue['__FORM_FLAG__'] is 'true'
                    logger.debug "into fieldMeta.entity if"

                    mapped = cascadeSave(fieldMgr, dbEntityFieldValue, reqEntityFieldValue, fieldType)

                    # 级联保存之后更新被关联方数据
                    dbEntityFieldValue[fieldMeta.mappedBy] = mapped if fieldMeta.mappedBy
                # 如果传了对象过来，但不是从表单过来的，也没有id，无效对象
                else if _.isUndefined reqEntityFieldValue.id

                    logger.debug "into fieldMeta.entity else"

                    databaseEntity[fieldName] = null

            if fieldMeta.isOneToMany() or fieldMeta.isManyToManyTarget() or fieldMeta.isManyToManyOwner()
                if fieldMeta.isOneToMany() # fieldMeta 与 databaseEntity 一对多关联，One处关联Many
                    continue if dbEntityFieldValue.isEmpty?()

                    fieldType = fieldMeta.manyType

                    logger.debug "into cascadeSave fieldMeta.isOneToMany with databaseEntity = #{databaseEntity} fieldName = #{fieldName} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)} fieldType = #{fieldType}"
                else if fieldMeta.isManyToManyTarget() # fieldMeta 与 databaseEntity 多对多双向关联，Many、One处都有关联
                    continue if dbEntityFieldValue.isEmpty?()

                    fieldType = fieldMeta.manyToManyOwnerType

                    logger.debug "into cascadeSave fieldMeta.isManyToManyTarget with databaseEntity = #{databaseEntity} fieldName = #{fieldName} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)} fieldType = #{fieldType}"
                else if fieldMeta.isManyToManyOwner() # fieldMeta 与 databaseEntity 多对多单向关联，One处关联Many
                    if dbEntityFieldValue.isEmpty?()
                        delete databaseEntity[fieldName]
                        continue

                    fieldType = fieldMeta.manyToManyTargetType

                    logger.debug "into cascadeSave fieldMeta.isManyToManyOwner with databaseEntity = #{databaseEntity} fieldName = #{fieldName} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)} fieldType = #{fieldType}"

                fieldMgr = baseService.createManager fieldType

                dbEntityFieldValueList = dbEntityFieldValue.toArray()
                dbEntityFieldValue.clear()

                for dbEntityFieldValueItem, i in dbEntityFieldValueList
                    if !reqEntityFieldValue
                        dbEntityFieldValue.add dbEntityFieldValueItem
                        continue

                    reqEntityFieldValueItem = reqEntityFieldValue[i]
                    continue unless reqEntityFieldValueItem

                    # 如果是删除操作则解除关联关系即可
                    if reqEntityFieldValueItem and reqEntityFieldValueItem['__FORM_TYPE__'] is 'delete'
                        if fieldMeta.mappedBy
                            dbEntityFieldValueItem[fieldMeta.mappedBy] = null

                        if fieldMeta.isOneToMany() or fieldMeta.isManyToManyTarget()
                            dbEntityFieldValueItem = fieldMgr.merge dbEntityFieldValueItem
                        continue


                    if fieldMeta.isOneToMany()
                        if fieldMeta.mappedBy # 如果不是删除操作，则说明关联关系还存在，需要保存此关系
                            dbEntityFieldValueItem[fieldMeta.mappedBy] = databaseEntity

                    # 由页面操作的数据在出现关联时继续深入级联保存
                    if reqEntityFieldValueItem and reqEntityFieldValueItem['__FORM_FLAG__'] is 'true'
                        dbEntityFieldValue.add cascadeSave(fieldMgr, dbEntityFieldValueItem, reqEntityFieldValueItem, fieldType)
                    # 不是由表单操作而来，说明数据原本就有关联关系
                    else
                        if fieldMeta.isOneToMany()
                            dbEntityFieldValueItem = fieldMgr.merge dbEntityFieldValueItem
                        dbEntityFieldValue.add dbEntityFieldValueItem
        mgr.merge databaseEntity

    startProcess = mark('beans', 'runtimeService').on (runtimeService, entity, manager) ->
        currentUser = getCurrentUser()

        processDefinitionKey = scaffold.processDefinitionKey
        if type(processDefinitionKey) is 'function'
            processDefinitionKey = processDefinitionKey entity
        variables =
            ENTITY: entity.id
            ENTITYCLASS: entity.getClass()?.getName()
            SUBMITTER: currentUser
        for property, value of entity
            variables[property] = value if value isnt undefined and type(value) isnt 'function' and value isnt null

        processInstance = runtimeService.startProcessInstanceByKey processDefinitionKey, variables

        # 写入流程实例id到实体中
        entity.processInstanceId = processInstance.processInstanceId
        if entity instanceof ProcessStatusAware
            entity.processDefinitionId = processInstance.processDefinitionId
            entity.processInstanceId = processInstance.id
            entity.submitter = currentUser
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
                accountClass = ClassUtils.forName 'com.zyeeda.cdeio.commons.organization.entity.Account'
                accountManager = baseService.createManager accountClass
                account = accountManager.find currentUser

                groupIds = []
                departments = []
                roles = []
                # 处理任务参与者，将部门和角色映射为 group
                departments = getParentDepatments account.department if account?.department
                roles = account.roles.toArray() if account?.roles
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

        create: mark('tx').on (entity, data) ->
            manager = baseService.createManager service.entityClass

            entity = manager.save entity
            entity = cascadeSave manager, entity, data, service.entityClass

            # 启动流程
            if scaffold.processDefinitionKey
                processInstanceId = startProcess entity, manager

            entity

        update: mark('tx', { needStatus: true }).on (txStatus, id, fn, data) ->
            manager = baseService.createManager service.entityClass
            entity = manager.find id

            preAttachment = {}
            for fieldMeta in entityMeta.getFields()
                if entity[fieldMeta.name] and entity[fieldMeta.name] instanceof Attachment
                    preAttachment[fieldMeta.name] = entity[fieldMeta.name].id

            if fn(entity, service) is false
                txStatus.setRollbackOnly()
                null

            entity = cascadeSave manager, entity, data, service.entityClass, preAttachment

            entity

        remove: mark('tx').on (entities...) ->
            manager = baseService.createManager service.entityClass
            manager.remove.apply manager, entities

    service
getCurrentUser = ->
    p = SecurityUtils.getSubject().getPrincipal()
    if Authentication.getAuthenticatedUserId()
        if p and Authentication.getAuthenticatedUserId() isnt p.getAccountName()
            currentUser = p.getAccountName()
            Authentication.setAuthenticatedUserId currentUser
        else
            currentUser = Authentication.getAuthenticatedUserId()
    else
        currentUser = p?.getAccountName() or 'tom'
        Authentication.setAuthenticatedUserId currentUser

    currentUser

# 递归查询父部门
# TODO 可能存在性能问题
getParentDepatments = (department, parents = []) ->
    parents.push department
    if department.parent
        getParentDepatments department.parent, parents

    parents
