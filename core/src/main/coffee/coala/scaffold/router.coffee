###
系统的总路由，会对系统中所有的 packages 进行解析并配置相应的访问路径
###
_                       = require 'underscore'
{type}                  = require 'coala/util/type'
{createRouter}          = require 'coala/router'
{coala}                 = require 'coala/config'
{json}                  = require 'coala/response'
{generateForms}         = require 'coala/scaffold/form-generator'
{createService}         = require 'coala/scaffold/service'
objects                 = require 'coala/util/objects'
{mark}                  = require 'coala/mark'
{getOptionInProperties} = require 'coala/config'
{join}                  = require 'coala/util/paths'
response                = require 'ringo/jsgi/response'

log = require('ringo/logging').getLogger module.id

{Context}          = com.zyeeda.coala.web.SpringAwareJsgiServlet

URLDecoder         = java.net.URLDecoder

{Authentication}   = org.activiti.engine.impl.identity

{ClassUtils}       = org.springframework.util

entityMetaResolver = Context.getInstance(module).getBeanByClass(com.zyeeda.coala.web.scaffold.EntityMetaResolver)

router = exports.router = createRouter()
# 所有的 app/config 中配置的 packages
metas = entityMetaResolver.resolveScaffoldEntities coala.entityPackages

exports.wrapGrid = wrapGrid = (grid, options) ->
    if not grid and options.labels
        columns = []
        columns.push {name: name, header: value} for name, value of options.labels
        grid = columns: columns
    else if grid and options.labels
        grid.columns = setLabelToColModel grid.columns, options.labels
    grid

mountExtraRoutes = (r, meta, options) ->
    r.get('configuration/forms/:formName', (request, formName) ->
        json generateForms(meta, options.labels, options.forms, options.fieldGroups, formName, options)
    )

    r.get('configuration/operators', (request) ->
        # 用户可以自定义操作， 在 operators 对象中定义即可
        ops = options.operators
        operators = objects.extend {}, coala.defaultOperators, ops

        for k, v of operators
            if operators[k] is false
                delete operators[k]
            else
                operators[k] = objects.extend {}, coala.defaultOperators[k], operators[k] if k of coala.defaultOperators
        json operators
    )

    r.get('configuration/grid', (request) ->
        grid = options['grid']
        if not grid and options.labels
            columns = []
            columns.push {name: name, header: value} for name, value of options.labels
            grid = columns: columns
        else if grid and options.labels
            grid.columns = setLabelToColModel grid.columns, options.labels
        json grid
    )


    r.get('configuration/picker', (request) ->
        picker = options['picker']
        if not picker and options.labels
            colModel = []
            colModel.push {name: name, header: value} for name, value of options.labels
            picker = grid:
                columns: colModel
        json picker
    )

    r.get('configuration/:name', (request, name) ->
        json options[name]
    )

    r.get('configuration/feature', (request) ->
        feature = options.feature or {}
        # 默认 style 为 grid ，coala 所支持的类型在 coala/scaffold/scaffold-feature-loader 中进行定义
        feature.style = options.style or 'grid'
        # 默认不支持前端扩展
        feature.enableFrontendExtension = !!options.enableFrontendExtension
        # 活动的标签，默认为 '全部' 标签，用于流程 feature 使用
        feature.activeTab = options.activeTab or 'none'
        # 默认 haveFilter = false
        feature.haveFilter = !!options.haveFilter

        json feature
    )

    r.get('configuration/fields', (request) ->
        if options.configs and options.configs.fields
            json options.configs.fields
        else
            json {}
    )

    # 流程相关配置
    # taskType ，待办:waiting ，在办:doing ，已办: done ,全部: none
    r.get('configuration/process/operators/:taskType', (request, taskType) ->

        operators =
            show:
                label: '查看', icon: 'icon-eye-open', group: '20-selected', style: 'btn-primary', order: 100, show: 'single-selected'
            refresh:
                label: '刷新', icon: 'icon-refresh', group: '30-refresh', style: 'btn-purple', show: 'always', order: 100

        if taskType is 'none'
            ops = options.operators
            operators = objects.extend {}, coala.defaultOperators, ops

        for k, v of operators
            if operators[k] is false
                delete operators[k]
            else
                operators[k] = objects.extend {}, coala.defaultOperators[k], operators[k] if k of coala.defaultOperators


        json operators
    )
    r.get('configuration/process/grid/:taskType', (request, taskType) ->
        grid = options['grid']
        if (not grid or not grid[taskType]) and options.labels
            columns = []
            columns.push {name: name, header: value} for name, value of options.labels
            grid = columns: columns
        else if grid and grid[taskType] and options.labels
            grid.columns = setLabelToColModel grid[taskType].columns, options.labels
        json grid

        # # 判断有 grid 的配置信息
        # grid = options['grid']
        # if not grid
        #     # 如果不存在相应的表头信息，则所有的 tab 页统一使用 defaults 所设置的内容
        #     lableName = if options.labels[taskType] then taskType else 'defaults'
        #     columns = []
        #     columns.push {name: name, header: value} for name, value of options.labels[lableName]
        #     grid = columns: columns
        # # 存在 grid 配置信息的情况，目前尚不支持
        # else if grid and options.labels
        #     grid.columns = setLabelToColModel grid.columns, options.labels

        # json grid
    )
    r.get('configuration/process/forms/:formName', (request, formName) ->
        forms = {}
        fieldGroups = []

        if formName is 'show'
            fieldGroups =
                'base-info-group': [
                    'name', 'age', 'sex', 'phone', 'address'
                ],
                'task-info-group': [
                    # '_t_taskId',
                    '_t_taskName',
                    '_t_createTime',
                    '_t_assigneeName'
                ],
                'process-info-group': [
                    '_p_name',
                    '_p_submitter',
                    '_p_startTime',
                    '_p_endTime',
                    {name: '_p_description', type: 'textarea'}
                ],
                'history-group': [
                    'name', 'description'
                ]
            # 追加 scaffold 中配置的 'base-info-group' 信息
            fieldGroups['base-info-group'] = options.fieldGroups['base-info-group']
            forms.show =
                size: options.forms?.show?.size or 'large',
                groups: [
                    {name: 'task-info-group', columns: 3, labelOnTop: true, label: '任务信息'},
                    {name: 'process-info-group', columns: 2},
                    # {name: 'history-group', columns: 6}
                    {name: 'history-group'}
                ],
                tabs: [
                    {id:'process-info-group', title: '流程信息', groups: ['process-info-group']},
                    {id:'history-group', title: '历史信息', groups: ['history-group']}
                ]
            # 追加 scaffold 中的组配置信息和基本信息 tab 下的组信息
            forms.show.groups.unshift options.forms.show.groups[0]
            forms.show.tabs.unshift options.forms.show.tabs[0]
        else if formName is 'complete'
            fieldGroups =
                'task-audit-group': [
                    {name: '_t_pass', type: 'dropdown', defaultValue: '1', source: [{id: 1, text: '通过'}, {id: 0, text: '不通过'}]},
                    {name: '_t_comment', type: 'textarea', colspan: 3, rowspan: 3, heigth: 80}
                ]
            fieldGroups['base-info-group'] = options.fieldGroups['base-info-group']

            forms.complete = options.forms.complete
        else if formName is 'reject'
            fieldGroups =
                'task-reject-group': [
                    {name: '_t_reject_reason', type: 'textarea', colspan: 3, rowspan: 3, heigth: 80}
                ]
            forms.reject =
                groups: [
                    {name: 'task-reject-group', columns: 1}
                ]
        else if formName is 'recall'
            fieldGroups =
                'task-recall-group': [
                    {name: '_t_recall_reason', type: 'textarea', colspan: 3, rowspan: 3, heigth: 80}
                ]
            forms.reject =
                groups: [
                    {name: 'task-recall-group', columns: 1}
                ]
        else
            forms = options.forms
            fieldGroups = options.fieldGroups


        labels =  objects.extend {}, options.labels

        labels._t_reject_reason = '回退原因'
        labels._t_recall_reason = '召回原因'

        labels._t_taskId = '任务id' if not labels._t_taskId
        labels._t_taskName = '任务名称' if not labels._t_taskName
        labels._t_assigneeName = '执行人' if not labels._t_assigneeName
        labels._t_createTime = '创建时间' if not labels._t_createTime
        labels._t_pass = '是否通过' if not labels._t_pass
        labels._t_comment = '意见' if not labels._t_comment

        labels._p_name = '名称' if not labels._p_name
        labels._p_description = '描述' if not labels._p_description
        labels._p_startTime = '开始时间' if not labels._p_startTime
        labels._p_endTime = '结束时间' if not labels._p_endTime
        labels._p_submitter = '发起人' if not labels._p_submitter

        json generateForms(meta, labels, forms, fieldGroups, formName, options)
    )

getJsonFilter = exports.getJsonFilter = (options, type) ->
    return {} unless options.filters
    options.filters[type] or options.filters.defaults or {}

getCurrentUser = ->
    currentUser = 'tom'
    Authentication.setAuthenticatedUserId currentUser
    currentUser

requireScaffoldConfig = exports.requireScaffoldConfig = (path) ->
    path = path.replace /(^\/)|(\/$)/g, ''
    p = path + '.feature/scaffold'
    # 动态载入 feature 配置信息
    options = try
        require p
    catch e
        {}

    options

setLabelToColModel = (colModel, labels) ->
    newModel = []
    for f, i in colModel
        if type(f) is 'string'
            f = name: f
        unless f.header
            if f.name.indexOf('.') isnt -1
                _names = f.name.split '.'
                f.header = labels[_names[0]][_names[1]]
            else
                f.header = labels[f.name]
        newModel.push f
    newModel

getService = (options, entityMeta) ->
    return options.service if options.service and not _.isFunction(options.service)
    service = createService entityMeta.entityClass, entityMeta, options
    if options.service then options.service(service) else service

mountPath = (path, meta) ->
    options = requireScaffoldConfig path
    log.debug "find scaffolding entity:#{meta.entityClass} bind to #{meta.path}, with options:#{JSON.stringify options}"
    log.debug "router --- mountPath options = #{options.doWithRouter}"

    doWithRouter = options.doWithRouter or ->
    options.doWithRouter = (r) ->
        # 首先执行 feature 中定义的 doWithRouter 方法
        doWithRouter r
        mountExtraRoutes r, meta, options, path

    router.attachDomain path, meta, options

# 遍历所有 package 中的实体, meta.path 即实体中 @Scaffold 注解中的参数值
for meta in metas
    mountPath meta.path, meta
    mountPath path, meta for path in meta.otherPaths
