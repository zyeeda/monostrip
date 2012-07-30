{coala} = require 'coala/config'

exports.mountTo = (router) ->
    router.mount '/scaffold', 'coala/scaffold/router'
    router.mount '/helper', 'coala/frontend-development-helper-router' if coala.development
    router
