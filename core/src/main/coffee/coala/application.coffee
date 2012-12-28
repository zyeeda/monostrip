
{Application} = require 'stick'

{getLogger} = require 'ringo/logging'

{coala} = require 'coala/config'

LOGGER = getLogger module.id

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

exports.create = (module, mountDefaultRouters = true) ->
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
