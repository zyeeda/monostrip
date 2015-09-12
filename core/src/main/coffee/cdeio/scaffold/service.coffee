{mark} = require 'cdeio/mark'
{createService} = require 'cdeio/service'
{type} = require 'cdeio/util/type'
upload = require 'cdeio/util/upload'
fs = require 'fs'
_ = require 'underscore'
{cdeio} = require 'cdeio/config'
createProcessService = require('cdeio/scaffold/process-service').createService

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

    cascadeSave = (mgr, entity, data, etClass, preAttachment) ->
        entityMeta = entityMetaResolver.resolveEntity etClass

        for fieldMeta in entityMeta.getFields()
            en = entity[fieldMeta.name]
            da = data[fieldMeta.name]

            continue unless en

            if en instanceof Attachment

                #如果编辑操作之前有附件，并且跟编辑之后的附件不一致，则删除原附件
                if preAttachment[fieldMeta.name] && en.id != preAttachment[fieldMeta.name]
                    upload.deleteAttachment preAttachment[fieldMeta.name]

                if en.id
                    upload.commitAttachment en.id
                else
                    entity[fieldMeta.name] = null
                continue

            #多对一关联，Many处关联One
            if fieldMeta.entity
                if da
                    if d and (d['__FORM_FLAG__'] == '' || d['__FORM_FLAG__'] == 'true')
                        fieldMgr = baseService.createManager fieldMeta.type
                        mapped = cascadeSave(fieldMgr, en, da, fieldMeta.type)
                        en[fieldMeta.mappedBy] = mapped if fieldMeta.mappedBy
                    # 如果传了对象过来，但不是从表单过来的，也没有id，无效对象
                    else if da.id == undefined
                        entity[fieldMeta.name] = null

            #一对多关联，One处关联Many
            if fieldMeta.isOneToMany()
                if en != undefined and en != null and en.isEmpty and not en.isEmpty()
                    fieldMgr = baseService.createManager fieldMeta.manyType
                    vs = en.toArray()
                    en.clear()
                    for e, i in vs
                        # 当前台没传入数据，原先的list属性保持
                        if !da
                            en.add e
                            continue
                        d = da[i]
                        # 此处已经删除全部关系再加入，若要真实删除，请修改以下逻辑
                        if d and d['__FORM_TYPE__'] == 'delete'
                            # fieldMgr.remove e
                            e[fieldMeta.mappedBy] = null
                            fieldMgr.merge e
                            continue
                        if d and (_.isUndefined(d['__FORM_FLAG__']) || d['__FORM_FLAG__'] == '' || d['__FORM_FLAG__'] == 'true')
                            if fieldMeta.mappedBy
                                e[fieldMeta.mappedBy] = entity

                            en.add cascadeSave(fieldMgr, e, d, fieldMeta.manyType)
                        else
                            if fieldMeta.mappedBy
                                e[fieldMeta.mappedBy] = entity

                            fieldMgr.merge e
                            en.add e

            #多对多双向关联，Many、One处都有关联
            else if fieldMeta.isManyToManyTarget()
                if en != undefined and en != null and en.isEmpty and not en.isEmpty()
                    type = fieldMeta.manyToManyTargetType
                    type = fieldMeta.manyToManyOwnerType if type == null
                    fieldMgr = baseService.createManager type
                    vs = en.toArray()
                    en.clear()
                    for e, i in vs
                        # 当前台没传入数据，原先的list属性保持
                        if !da
                            en.add e
                            continue
                        d = da[i]
                        if d and d['__FORM_TYPE__'] == 'delete'
                            e[fieldMeta.mappedBy] = null
                            fieldMgr.merge e
                            continue
                        if d and (d['__FORM_FLAG__'] == '' || d['__FORM_FLAG__'] == 'true')
                            en.add cascadeSave(fieldMgr, e, d, type)
                        else
                            en.add e

            #多对多单向关联，One处关联Many
            else if fieldMeta.isManyToManyOwner()
                if en != undefined and en != null and en.isEmpty and not en.isEmpty()
                    type = fieldMeta.manyToManyOwnerType
                    type = fieldMeta.manyToManyTargetType if type == null
                    fieldMgr = baseService.createManager type
                    vs = en.toArray()
                    en.clear()
                    for e, i in vs
                        # 当前台没传入数据，原先的list属性保持
                        if !da
                            en.add e
                            continue
                        d = da[i]
                        # 此处已经删除全部关系再加入，若要真实删除，请修改以下逻辑
                        if d and d['__FORM_TYPE__'] == 'delete'
                            # fieldMgr.remove e
                            continue
                        if d and (d['__FORM_FLAG__'] == '' || d['__FORM_FLAG__'] == 'true')
                            en.add cascadeSave(fieldMgr, e, d, type)
                        else
                            en.add e
                else
                    delete entity[fieldMeta.name]
        mgr.merge entity

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
