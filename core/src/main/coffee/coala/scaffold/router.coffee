{Context} = com.zyeeda.coala.web.SpringAwareJsgiServlet

{type} = require 'coala/util/type'
{createRouter} = require 'coala/router'
{coala} = require 'coala/config'
{json} = require 'coala/response'
{generateForms} = require 'coala/scaffold/form-generator'
objects = require 'coala/util/objects'

log = require('ringo/logging').getLogger module.id
entityMetaResolver = Context.getInstance(module).getBeanByClass(com.zyeeda.coala.web.scaffold.EntityMetaResolver)

router = exports.router = createRouter()

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
        feature.style = options.style or 'grid'
        feature.enableFrontendExtension = !!options.enableFrontendExtension
        feature.haveFilter = !!options.haveFilter

        json feature
    )

    r.get('configuration/fields', (request) ->
        if options.configs and options.configs.fields
            json options.configs.fields
        else
            json {}
    )

    r.get('configuration/export-module', (request) ->
        if options.configs and options.configs.exportModule
            json {exportModule: options.configs.exportModule}
        else
            json {}
    )

requireScaffoldConfig = exports.requireScaffoldConfig = (path) ->
    path = path.replace /(^\/)|(\/$)/g, ''
    p = path + '.feature/scaffold'
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

mountPath = (path, meta) ->
    options = requireScaffoldConfig path
    log.trace "find scaffolding entity:#{meta.entityClass} bind to #{meta.path}, with options:#{JSON.stringify options}"
    doWithRouter = options.doWithRouter or ->
    options.doWithRouter = (r) ->
        doWithRouter r
        mountExtraRoutes r, meta, options, path

    router.attachDomain path, meta, options

for meta in metas
    mountPath meta.path, meta
    mountPath path, meta for path in meta.otherPaths
