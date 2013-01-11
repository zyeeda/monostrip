{Context} = com.zyeeda.coala.web.SpringAwareJsgiServlet

{type} = require 'coala/util/type'
{createRouter} = require 'coala/router'
{coala} = require 'coala/config'
{json} = require 'coala/response'
{generateForms} = require 'coala/scaffold/form-generator'

log = require('ringo/logging').getLogger module.id
entityMetaResolver = Context.getInstance(module).getBeanByClass(com.zyeeda.coala.web.scaffold.EntityMetaResolver)

router = exports.router = createRouter()

metas = entityMetaResolver.resolveScaffoldEntities coala.entityPackages

mountExtraRoutes = (router, meta, options) ->
    router.get('configuration/forms/:formName', (request, formName) ->
        json generateForms(meta, options.labels, options.forms, options.fieldGroups, formName, options)
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
        else if grid and options.labels
            grid.colModel = setLabelToColModel grid.colModel, options.labels
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
    router.get('configuration/feature', (request) ->
        feature = options.feature or {}
        feature.style = options.style or 'grid'
        feature.enableFrontendExtension = !!options.enableFrontendExtension

        json feature
    )
    router.get('configuration/fields', (request) ->
        if options.configs and options.configs.fields
            json options.configs.fields
        else
            json {}
    )

requireScaffoldConfig = exports.requireScaffoldConfig = (path) ->
    path = path.replace /(^\/)|(\/$)/g, ''
    [paths..., name] = path.split '/'
    paths.push coala.scaffoldFolderName
    paths.push name
    path = paths.join '/'

    options = try
        require path
    catch e
        {}

    options

setLabelToColModel = (colModel, labels) ->
    newModel = []
    for f, i in colModel
        if type(f) is 'string'
            f = name: f
        unless f.label
            if f.name.indexOf('.') isnt -1
                _names = f.name.split '.'
                f.label = labels[_names[0]][_names[1]]
            else
                f.label = labels[f.name]
        f.alias = f.name unless f.alias
        f.position = i unless f.position
        newModel.push f
    newModel

for meta in metas
    do (meta, mountExtraRoutes) ->
        path = meta.path
        options = requireScaffoldConfig path
        log.debug "find scaffolding entity:#{meta.entityClass} bind to #{meta.path}, with options:#{JSON.stringify options}"
        doWithRouter = options.doWithRouter or ->
        options.doWithRouter = (router) ->
            doWithRouter router
            mountExtraRoutes router, meta, options

        router.attachDomain meta.path, meta.entityClass, options
