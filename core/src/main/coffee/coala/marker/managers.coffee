# support mark 'managers'
# to inject entity-class-binded-manager or user-defined-manager
# when customize a manager, you must export a function named createManager
# attributes should be a single entry or an array of entries
# each entry should be a entity class or a string which represent the path of the user-defined-manager
# ex. :
# mark('managers', [User, Organize, 'path-to-user-defined-manager'], function(userManager, organizeManager, custom){});

{createService} = require 'coala/service'
{type} = require 'coala/util'

exports.handler = (context, attributes, fn, args) ->
    if attributes
        service = createService()
        attr = if type(attributes) is 'array' then attributes else [attributes]
        managers = []

        managers.push(if type(clazz) is 'string' then require(clazz).createManager() else service.createManager clazz) for clazz in attr
        args = managers.concat args
    fn.apply null, args
