paths = require 'coala/util/paths'
{json} = require 'coala/response'
{coala} = require 'coala/config'

servletContext = null
scriptFolder = '/scripts'

router = exports.router = require('coala/router').createRouter()
exports.setScriptFolder = (path) ->
    scriptFolder = path

router.get '/development', ->
    json coala.development

router.get '/', (request) ->
    servletContext = request.env.servletRequest.getSession(true).getServletContext() if servletContext is null

    {root} = request.params
    root = paths.join scriptFolder, root
    root = root + '/' if root.charAt(root.length - 1) isnt '/'
    files = {}

    findFile = (folder) ->
        fs = servletContext.getResourcePaths folder
        it = fs.iterator()
        fileHolder = files[folder] or files[folder] = {}
        while it.hasNext()
            file = it.next()
            if file.charAt(file.length - 1) is '/'
                findFile file
            else
                idx = file.lastIndexOf '/'
                filename = file.substring (idx + 1)
                idx = filename.lastIndexOf '.'
                withoutExt = if idx is -1 then filename else filename.substring 0, idx

                fileHolder[filename] = true
                fileHolder[withoutExt] = true
        null

    findFile root
    json files

#    {path} = request.params
#    fullPath = paths.join scriptFolder, path
#    idx = fullPath.lastIndexOf '/'
#    folder = fullPath.substring 0, idx + 1
#
#    ps = servletContext.getResourcePaths folder
#    exists = false
#
#    print fullPath, ps
#    it = ps.iterator()
#    while it.hasNext()
#        p = it.next()
#        continue if p.charAt(p.length - 1) is '/'
#        if p is fullPath
#            exists = true
#            break
#
#        idx = p.lastIndexOf '.'
#        p = if idx is -1 then p else p.substring 0, idx
#        if p is fullPath
#            exists = true
#            break
#
#    json exists: exists
