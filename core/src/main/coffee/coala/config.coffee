{objects} = require 'coala/util'
log = require('ringo/logging').getLogger module.id

projectLevelConfigure = try
    require 'config'
catch e
    {}

defaultConfigure =
    development: true
    orms: ['src/main/resources/META-INF/orms/orm.xml']
    scaffoldFolderName: '__scaffold__'
    serviceFolderName: '__services__'
    managerFolderName: '__managers__'
    routerFoldername: '__routers__'
    servicePathSeperator: ':'
    defaultPageSize: 10
    dateFormat: 'yyyy-MM-dd'
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
        restricts = params['_restricts']
        return null if not restricts
        delete params['_restricts']
        JSON.parse restricts

exports.coala = objects.extend defaultConfigure, projectLevelConfigure.coala

log.debug "environment variable #{name}:#{value}" for name, value of exports.coala
