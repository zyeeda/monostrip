{coala} = require 'coala/config'
{SecurityUtils} = org.apache.shiro

handlers = exports.handlers = {}

handlers.perms = (context, attributes, fn, args) ->
    subject = SecurityUtils.subject
    subject.checkPermissions attributes...
    fn.apply null, args

handlers.roles = (context, attributes, fn, args) ->
    subject = SecurityUtils.subject
    subject.checkRoles attributes...
    fn.apply null, args

handlers.RequiresGuest = (context, attributes, fn, args) ->
    subject = SecurityUtils.subject
    return fn.apply null, args if subject.getPrincipal() is null
    throw new Error 'Operation is not allowed'

handlers.RequiresUser = (context, attributes, fn, args) ->
    subject = SecurityUtils.subject
    return fn.apply null, args if subject.isRemembered() or subject.isAuthenticated()
    throw new Error 'Operation is not allowed'

handlers.RequiresAuth = (context, attributes, fn, args) ->
    subject = SecurityUtils.subject
    return fn.apply null, args if subject.isAuthenticated()
    throw new Error 'Operation is not allowed'
