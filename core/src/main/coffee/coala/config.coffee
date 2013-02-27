_ = require 'underscore'
log = require('ringo/logging').getLogger module.id
{Context} = com.zyeeda.coala.web.SpringAwareJsgiServlet
{FrontendSettingsCollector} = com.zyeeda.coala.web

projectLevelConfigure = try
    require 'config'
catch e
    {}

defaultConfigure =
    development: !Context.getInstance(module).isProductionMode()
    # orms: ['src/main/resources/META-INF/orms/orm.xml']
    orms: []
    scaffoldFolderName: '__scaffold__'
    serviceFolderName: '__services__'
    managerFolderName: '__managers__'
    routerFoldername: '__routers__'
    servicePathSeperator: ':'
    defaultPageSize: 10
    dateFormat: 'yyyy-MM-dd'
    dateTimeFormat: 'yyyy-MM-dd hh:mm:ss'

    booleanFieldPickerSource: [{id: true, text: '是'}, {id: false, text: '否'}]
    defaultOperators:
        add:
            label: '添加', icon: 'icon-plus', group: 'op', style: 'btn-primary'
        edit:
            label: '编辑', icon: 'icon-edit', group: 'op'
        del:
            label: '删除', icon: 'icon-minus', group: 'op', style: 'btn-danger'
        show:
            label: '查看', icon: 'icon-eye-open', group: 'view'
        refresh:
            label: '刷新', icon: 'icon-refresh', group: 'view'

    extractPaginationInfo: (params) ->
        pageSize = params['_pageSize']
        currentPage = params['_page']
        return null if not currentPage
        delete params['_pageSize']
        delete params['_page']

        firstResult: (currentPage - 1) * pageSize
        maxResults: pageSize
        currentPage: currentPage

    generateListResult: (results, currentPage, pageSize, recordCount, pageCount) ->
        results: results
        page: currentPage
        recordCount: recordCount
        pageCount: pageCount

    extractOrderInfo: (params) ->
        orders = params['_order']
        return null if not orders
        delete params['_order']
        orders = orders.split ','

        result = []
        for order in orders
            continue if order.length is 0
            ts = order.split '-'
            s = if ts.length is 1 then 'asc' else ts[1]
            o = {}
            o[ts[0]] = s
            result.push o

        result

    extractRestrictInfo: (params) ->
        restricts = params['_filters']
        return null if not restricts
        delete params['_filters']
        JSON.parse restricts

# exports.coala = objects.extend defaultConfigure, projectLevelConfigure.coala
exports.coala = _.extend defaultConfigure, projectLevelConfigure.coala

log.debug "environment variable #{name}:#{value}" for name, value of exports.coala


# frontend settings

Object.defineProperty exports, 'frontendSettings',
    get: ->
        result = {}
        obj = projectLevelConfigure.frontendSettings
        return result if not obj

        ctx = Context.getInstance(module)
        context = ctx.getSpringContext().getBeanFactory()
        map = FrontendSettingsCollector.getSettings()

        for key, value of obj
            if _.isString value
                result[key] = try
                    map.get(value) or context.resolveEmbeddedValue('${' + value + '}')
                catch e
                    ''
            else if _.isFunction value
                v = value ctx
                if _.isString v
                    result[key] = v
                else if _.isObject v
                    result[key + '.' + kk] = vv for kk, vv of v
        result
