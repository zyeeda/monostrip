_ = require 'underscore'
log = require('ringo/logging').getLogger module.id
{Context} = com.zyeeda.cdeio.web.SpringAwareJsgiServlet
{FrontendSettingsCollector} = com.zyeeda.cdeio.web

# Load project scoped config file.
#
# The project config file should export an object named `cdeio`.
#
projectLevelConfigure = try
    require 'config'
catch e
    {}

# Default configuration.
defaultConfigure =

    # Whether the application is in development mode.
    #
    # The server will do the following things if application is in development
    # mode:
    #
    # * reload modified JavaScript files
    # * reload modified ORM files
    #
    development: !Context.getInstance(module).isProductionMode()

    # List all orm.xml files here, they will be dynamicly reloaded when in
    # development mode without restarting the server.
    #
    # For example:
    # ```
    # orms: ['src/main/resources/META-INF/orms/orm.xml']
    # ```
    #
    orms: []

    #{ scaffoldFolderName: '__scaffold__'
    #{ serviceFolderName: '__services__'
    #{ managerFolderName: '__managers__'
    #{ routerFoldername: '__routers__'

    # Service path seperator.
    servicePathSeperator: ':'

    # Default page size for grid pagination.
    defaultPageSize: 10

    # Default date display format.
    dateFormat: 'yyyy-MM-dd'

    # Default datetime display format.
    dateTimeFormat: 'yyyy-MM-dd HH:mm:ss'

    # When boolean field rendered as dropdown list, what default text will be
    # shown. `是` for `true` and `否` for `false`.
    #
    booleanFieldPickerSource: [{id: true, text: '是'}, {id: false, text: '否'}]

    # Default operator buttons located above datagrid. They're 'add', 'show', 'edit', 'del' and 'refresh'.
    #
    # @todo This config should be moved to its own place. No need to configurate
    # this globally.
    #
    defaultOperators:
        add:
            label: '添加', icon: 'icon-plus', group: '10-add', style: 'btn-success', show: 'always', order: 100
        show:
            label: '查看', icon: 'icon-eye-open', group: '20-selected', style: 'btn-info', show: 'single-selected', order: 100
        edit:
            label: '编辑', icon: 'icon-edit', group: '20-selected', style: 'btn-warning', show: 'single-selected', order: 200
        del:
            label: '删除', icon: 'icon-minus', group: '20-selected', style: 'btn-danger', order: 300
        refresh:
            label: '刷新', icon: 'icon-refresh', group: '30-refresh', style: 'btn-purple', show: 'always', order: 100

    # Default function to extract pagination info.
    #
    # @todo This config should be moved to its own place. No need to configurate
    # this globally.
    #
    extractPaginationInfo: (params) ->
        pageSize = params['_pageSize']
        first = params['_first']
        return null if not first
        delete params['_pageSize']
        delete params['_first']

        firstResult: first
        maxResults: pageSize

    # Default function to generate list result JSON object.
    #
    # @todo This config should be moved to its own place. No need to configurate
    # this globally.
    #
    generateListResult: (results, currentPage, pageSize, recordCount, pageCount) ->
        results: results
        recordCount: recordCount

    # Default function to extract order info.
    #
    # For example:
    # ```
    # name-desc: # order by name field in descending direction.
    # score-asc: # order by score field in ascending direction.
    # age:       # order by age field in ascending direction.
    # ```
    #
    # @todo This config should be moved to its own place. No need to configurate
    # this globally.
    #
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

    # Default function to extract filter info.
    #
    # @todo This config should be moved to its own place. No need to configurate
    # this globally.
    #
    extractFilterInfo: (params) ->
        obj = params['_filters']
        delete params['_filters']
        filters = []
        filters[key] = value for key, value of obj
        if not filters or filters.length is 0 then null else filters

exports.cdeio = _.extend defaultConfigure, projectLevelConfigure.cdeio

log.debug "environment variable #{name}:#{value}" for name, value of exports.cdeio

# Get value by key from `.properties` files.
exports.getOptionInProperties = (key) ->
    ctx = Context.getInstance(module)
    context = ctx.getSpringContext().getBeanFactory()
    context.resolveEmbeddedValue('${' + key + '}')

# Define `frontendSettings` readonly property.
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
                result[key] = value ctx
        result
