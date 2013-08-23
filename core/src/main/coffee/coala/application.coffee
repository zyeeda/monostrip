{Application} = require 'stick'

{getLogger} = require 'ringo/logging'

{coala} = require 'coala/config'
{registerHandler} = require 'coala/mark'

LOGGER = getLogger module.id
###
processRoot = (router, repo, prefix) ->
    routersRepo = repo.getChildRepository coala.routerFoldername
    LOGGER.debug "routersRepo.exists = #{routersRepo.exists()}"
    return if not routersRepo.exists()
    routers = routersRepo.getResources false
    for r in routers
        try
            module = r.getModuleName()
            url = prefix + r.getBaseName()
            LOGGER.debug "Mount #{module} to #{url}."
            router.mount url, module
        catch e
            LOGGER.error "Cannot mount module #{r.getModuleName()}.", e
    true

processRepository = (router, repo, prefix) ->
    processRoot router, repo, prefix
    for r in repo.getRepositories()
        processRepository router, r, prefix + r.getName() + '/'
    true
###

processRoot = (router, repo, prefix) ->
    routers = repo.getResources false
    for r in routers
        name = r.baseName
        matches = name.match /^router\-?(\w+)?$/
        if matches
            p = if matches[1] then prefix + '/' + matches[1] else prefix
            module = r.moduleName
            try
                router.mount p, module
                LOGGER.debug "found router:#{r}, mount it to #{p}"
            catch e
                LOGGER.error "Cannot mount module #{r.getModuleName()}.", e

processRepository = (router, repo, prefix) ->
    for r in repo.getRepositories()
        name = r.name
        idx = name.indexOf '.feature'
        if idx is name.length - 8
            processRoot router, r, prefix + name.substring(0, idx)
        else
            processRepository router, r, prefix + r.getName() + '/'

exports.create = (module, mountDefaultRouters = true) ->
    registerHandler "tx", require('coala/marker/tx').handler
    registerHandler "beans", require('coala/marker/beans').handler
    registerHandler "services", require('coala/marker/services').handler
    registerHandler "managers", require('coala/marker/managers').handler
    registerHandler "process", require('coala/marker/process').handler
    registerHandler "knowledge", require('coala/marker/knowledge').handler
    registerHandler "roles", require('coala/marker/permission').handlers.roles
    registerHandler "perms", require('coala/marker/permission').handlers.perms
    registerHandler "RequireGuests", require('coala/marker/permission').handlers.RequireGuests
    registerHandler "RequiresUser", require('coala/marker/permission').handlers.RequiresUser
    registerHandler "RequiresAuth", require('coala/marker/permission').handlers.RequiresAuth
    registerHandler "RequiresPerms", require('coala/marker/permission').handlers.perms
    registerHandler "RequiresRoles", require('coala/marker/permission').handlers.roles

    router = new Application()
    router.configure 'mount'

    if module
        root = module.getRepository('./')
        processRepository router, root, '/'

    if mountDefaultRouters
        router.mount '/helper', 'coala/frontend-helper' if coala.development
        router.mount '/scaffold', 'coala/scaffold/router'
        router.mount '/scaffold/tasks', 'coala/scaffold/task'

    router
