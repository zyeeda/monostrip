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

    cascadeSave = (mgr, mergedEntity, requestEntity, etClass, preAttachment) ->
        entityMeta = entityMetaResolver.resolveEntity etClass

        for fieldMeta in entityMeta.getFields()
            fieldName = fieldMeta.name

            mergedEntityFieldValue = mergedEntity[fieldName]
            reqEntityFieldValue = requestEntity[fieldName]

            logger.debug "before continue with mergedEntity = #{mergedEntity} fieldName = #{fieldName} mergedEntityFieldValue = #{mergedEntityFieldValue} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)}"

            logger.debug "before continue if condition result #{not reqEntityFieldValue}  #{not mergedEntityFieldValue}"

            continue if not reqEntityFieldValue or not mergedEntityFieldValue

            logger.debug "after continue with mergedEntity = #{mergedEntity} fieldName = #{fieldName}"

            if mergedEntityFieldValue instanceof Attachment
                # 如果编辑操作之前有附件，并且跟编辑之后的附件不一致，则删除原附件
                if preAttachment[fieldName] && mergedEntityFieldValue.id != preAttachment[fieldName]
                    upload.deleteAttachment preAttachment[fieldName]

                if mergedEntityFieldValue.id
                    upload.commitAttachment mergedEntityFieldValue.id
                else
                    mergedEntity[fieldName] = null
                continue

            # fieldMeta 与 mergedEntity 多对一关联，Many处关联One
            if fieldMeta.entity
                fieldType = fieldMeta.type
                fieldMgr = baseService.createManager fieldType

                logger.debug "into cascadeSave fieldMeta.entity with mergedEntity = #{mergedEntity} fieldName = #{fieldName} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)} fieldType = #{fieldType} reqEntityFieldValueId = #{reqEntityFieldValue.id}"

                # 由页面操作的数据在出现关联时继续深入级联保存
                if reqEntityFieldValue['__FORM_FLAG__'] is 'true'
                    logger.debug "into fieldMeta.entity if"

                    mapped = cascadeSave(fieldMgr, mergedEntityFieldValue, reqEntityFieldValue, fieldType)

                    # 级联保存之后更新被关联方数据
                    mergedEntityFieldValue[fieldMeta.mappedBy] = mapped if fieldMeta.mappedBy
                # 如果传了对象过来，但不是从表单过来的，也没有id，无效对象
                else if _.isUndefined reqEntityFieldValue.id
                    logger.debug "into fieldMeta.entity else"

                    mergedEntity[fieldName] = null

            if fieldMeta.isOneToMany() or fieldMeta.isManyToManyTarget() or fieldMeta.isManyToManyOwner()
                if fieldMeta.isOneToMany() # fieldMeta 与 mergedEntity 一对多关联，One处关联Many
                    continue if mergedEntityFieldValue.isEmpty?()

                    fieldType = fieldMeta.manyType

                    logger.debug "into cascadeSave fieldMeta.isOneToMany with mergedEntity = #{mergedEntity} fieldName = #{fieldName} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)} fieldType = #{fieldType}"
                else if fieldMeta.isManyToManyTarget() # fieldMeta 与 mergedEntity 多对多双向关联，Many、One处都有关联
                    continue if mergedEntityFieldValue.isEmpty?()

                    fieldType = fieldMeta.manyToManyOwnerType

                    logger.debug "into cascadeSave fieldMeta.isManyToManyTarget with mergedEntity = #{mergedEntity} fieldName = #{fieldName} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)} fieldType = #{fieldType}"
                else if fieldMeta.isManyToManyOwner() # fieldMeta 与 mergedEntity 多对多单向关联，One处关联Many
                    if mergedEntityFieldValue.isEmpty?()
                        delete mergedEntity[fieldName]
                        continue

                    fieldType = fieldMeta.manyToManyTargetType

                    logger.debug "into cascadeSave fieldMeta.isManyToManyOwner with mergedEntity = #{mergedEntity} fieldName = #{fieldName} reqEntityFieldValue = #{JSON.stringify(reqEntityFieldValue)} fieldType = #{fieldType}"

                fieldMgr = baseService.createManager fieldType

                mergedEntityFieldValueList = mergedEntityFieldValue.toArray()
                mergedEntityFieldValue.clear()

                mergedEntityFieldValueMap = {}
                noIdmergedEntityFieldValueList = []
                for mergedEntityFieldValueItem in mergedEntityFieldValueList
                    if !mergedEntityFieldValueItem.id
                        noIdmergedEntityFieldValueList.push mergedEntityFieldValueItem
                    else
                        mergedEntityFieldValueMap[mergedEntityFieldValueItem.id] = mergedEntityFieldValueItem

                for key, reqEntityFieldValueItem of reqEntityFieldValue
                    if mergedEntityFieldValueList.length > 0 and mergedEntityFieldValueList[0] instanceof Attachment
                        mergedEntityFieldValueItem = mergedEntityFieldValueMap[reqEntityFieldValueItem]
                    else
                        if reqEntityFieldValueItem.id
                            mergedEntityFieldValueItem = mergedEntityFieldValueMap[reqEntityFieldValueItem.id]
                        else
                            mergedEntityFieldValueItem = noIdmergedEntityFieldValueList.shift()

                    logger.debug "into cascadeSave with reqEntityFieldValueItem = #{JSON.stringify(reqEntityFieldValueItem)} mergedEntityFieldValueItemId = #{mergedEntityFieldValueItem.id}"

                    # 如果是删除操作则解除关联关系即可
                    if reqEntityFieldValueItem and reqEntityFieldValueItem['__FORM_TYPE__'] is 'delete'
                        if fieldMeta.mappedBy
                            mergedEntityFieldValueItem[fieldMeta.mappedBy] = null

                        mergedEntityFieldValueItem = fieldMgr.merge mergedEntityFieldValueItem
                        continue

                    if fieldMeta.isOneToMany()
                        if fieldMeta.mappedBy # 如果不是删除操作，则说明关联关系还存在，需要保存此关系
                            mergedEntityFieldValueItem[fieldMeta.mappedBy] = mergedEntity

                    # 由页面操作的数据在出现关联时继续深入级联保存
                    if reqEntityFieldValueItem and reqEntityFieldValueItem['__FORM_FLAG__'] is 'true'
                        mergedEntityFieldValue.add cascadeSave(fieldMgr, mergedEntityFieldValueItem, reqEntityFieldValueItem, fieldType)
                    # 不是由表单操作而来，说明数据原本就有关联关系
                    else
                        if fieldMeta.isOneToMany()
                            mergedEntityFieldValueItem = fieldMgr.merge mergedEntityFieldValueItem
                        mergedEntityFieldValue.add mergedEntityFieldValueItem
        mgr.merge mergedEntity

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

            # 此处调用了 cdeio/router(defaultHandlers --> update 中的 updateIt函数, 做了将 request 中带的从表单中获取的数据 merge 到 entity 中和验证表单数据的操作)
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
