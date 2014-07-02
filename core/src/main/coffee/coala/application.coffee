logger = require('ringo/logging').getLogger module.id

{Application} = require 'stick'

{coala} = require 'coala/config'
{registerHandler} = require 'coala/mark'

# Process the feature root, and mount path to the router.
#
# * If `router.js` is found, then mount the repository path (with prefix) to the
# router.
# * If `router-xxx.js` is found, then mount the repository path and xxx to the
# router. For example, if `router-bar.js` is found under the repository `foo`,
# then path `foo/bar` will be mounted to the router.
#
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
                logger.debug "found router:#{r}, mount it to #{p}"
            catch e
                logger.error "Cannot mount module #{r.getModuleName()}.", e

# Process all sub repositories under the current.
#
# * If the sub repository ends with `.feature`, then process it as a feature root.
# * If the sub repository doesn't end with `.feature`, process it as another
# repository.
#
processRepository = (router, repo, prefix) ->
    for r in repo.getRepositories()
        name = r.name
        idx = name.indexOf '.feature'
        if idx is name.length - 8 # should check whether name ends with .feature
            processRoot router, r, prefix + name.substring(0, idx)
        else
            processRepository router, r, prefix + r.getName() + '/'

# The exported function to create an coala application.
#
# For example:
# ```
# require('coala/application').create(this);
# ```
#
exports.create = (module, mountDefaultRouters = true) ->
    logger.debug "application is executing ..."
    # Register all marker handlers.
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

    # New a stick application and configurate `mount` middleware.
    router = new Application()
    router.configure 'mount'

    if module
        root = module.getRepository('./')
        processRepository router, root, '/'

    # 配置了三个默认的路径,前端可以直接调用
    if mountDefaultRouters
        router.mount '/helper', 'coala/frontend-helper' # this is useless
        router.mount '/scaffold', 'coala/scaffold/router'
        router.mount '/scaffold/tasks', 'coala/scaffold/task'

    router
