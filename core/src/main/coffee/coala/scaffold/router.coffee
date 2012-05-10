{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet

{createRouter} = require 'coala/router'
{env} = require 'coala/config'

log = require('ringo/logging').getLogger module.id
entityMetaResolver = Context.getInstance().getBeanByClass(com.zyeeda.framework.web.scaffold.EntityMetaResolver)

router = exports.router = createRouter()

metas = entityMetaResolver.resolveScaffoldEntities env.entityPackages

for meta in metas
    options = try
        require env.scaffoldRoot + meta.path
    catch e
        {}
    log.debug "find scaffolding entity:#{meta.entityClass} bind to #{meta.path}, with options:#{JSON.stringify options}"
    router.attachDomain meta.path, meta.entityClass, options
