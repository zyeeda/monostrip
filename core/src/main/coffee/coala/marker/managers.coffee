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

        for clazz in attr
            if type(clazz) is 'string'
                managers.push require(clazz).createManager()
            else if type(clazz) is 'class'
                managers.push service.createManager clazz
            else if type(clazz) is 'package'
                throw new Error('package is not supported, please check your entity path:' + clazz)
            else
                throw new Error('unknown manager:' + clazz)
        args = managers.concat args
    fn.apply null, args
