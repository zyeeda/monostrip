{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet

{createRouter} = require 'coala/router'
{coala} = require 'coala/config'
{json} = require 'coala/response'
{generateForms} = require 'coala/scaffold/form-generator'

log = require('ringo/logging').getLogger module.id
entityMetaResolver = Context.getInstance(module).getBeanByClass(com.zyeeda.framework.web.scaffold.EntityMetaResolver)

router = exports.router = createRouter()

metas = entityMetaResolver.resolveScaffoldEntities coala.entityPackages

mountExtraRoutes = (router, meta, options) ->
    router.get('configuration/forms/:formName', (request, formName) ->
        json generateForms(meta, options.labels, options.forms, formName)
    )
    router.get('configuration/operators', (request) ->
        operators = options['operators'] or coala.defaultOperators
        json operators
    )
    router.get('configuration/grid', (request) ->
        grid = options['grid']
        if not grid and options.labels
            colModel = []
            colModel.push {name: name, index: name, label: value} for name, value of options.labels
            grid = colModel: colModel

        json grid
    )
    router.get('configuration/picker', (request) ->
        picker = options['picker']
        if not picker and options.labels
            colModel = []
            colModel.push {name: name, index: name, label: value} for name, value of options.labels
            picker = grid:
                colModel: colModel
        json picker
    )
    router.get('configuration/:name', (request, name) ->
        json options[name]
    )

for meta in metas
    do (meta, mountExtraRoutes) ->
        path = meta.path
        path = path.replace /(^\/)|(\/$)/g, ''
        [paths..., name] = path.split '/'
        paths.push coala.scaffoldFolderName
        paths.push name
        path = paths.join '/'

        options = try
            require path
        catch e
            {}
        log.debug "find scaffolding entity:#{meta.entityClass} bind to #{meta.path}, with options:#{JSON.stringify options}"
        doWithRouter = options.doWithRouter or ->
        options.doWithRouter = (router) ->
            doWithRouter router
            mountExtraRoutes router, meta, options

        router.attachDomain meta.path, meta.entityClass, options
