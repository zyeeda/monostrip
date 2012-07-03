{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet

{createRouter} = require 'coala/router'
{coala} = require 'coala/config'

log = require('ringo/logging').getLogger module.id
entityMetaResolver = Context.getInstance(module).getBeanByClass(com.zyeeda.framework.web.scaffold.EntityMetaResolver)

router = exports.router = createRouter()

metas = entityMetaResolver.resolveScaffoldEntities coala.entityPackages

for meta in metas
    path = meta.path
    path = path.replace /(^\/)|(\/$)/g, ''
    [paths..., name] = path.split '/'
    paths.push coala.scaffoldRoot
    paths.push name
    path = paths.join '/'

    options = try
        require path
    catch e
        {}
    log.debug "find scaffolding entity:#{meta.entityClass} bind to #{meta.path}, with options:#{JSON.stringify options}"
    router.attachDomain meta.path, meta.entityClass, options
