# support mark 'managers'
# to inject entity-class-binded-manager or user-defined-manager
# when customize a manager, you must export a function named createManager
# attributes should be a single entry or an array of entries
# each entry should be a entity class or a string which represent the path of the user-defined-manager
# ex. :
# mark('managers', [User, Organize, 'path-to-user-defined-manager'], function(userManager, organizeManager, custom){});

{createService} = require 'coala/service'
{type} = require 'coala/util/type'
objects = require 'coala/util/objects'
paths = require 'coala/util/paths'
{coala} = require 'coala/config'

exports.handler = (context, attributes, fn, args) ->
    service = createService()

    managers = []
    for clazz in attributes
        if type(clazz) is 'string'
            names = clazz.split coala.servicePathSeperator
            throw new Error("illegal manager path: #{m}, module:managerName") if names.length isnt 2
            name = paths.join names[0], coala.managerFolderName, names[1]
            manager = require(name).createManager()
            managers.push manager
        else if type(clazz) is 'class'
            managers.push service.createManager clazz
        else if type(clazz) is 'object'
            managers.push service.createManager clazz.entity, clazz.emf
        else if type(clazz) is 'package'
            throw new Error('package is not supported, please check your entity path:' + clazz)
        else
            throw new Error('unknown manager:' + clazz)

    args = managers.concat args
    fn.apply null, args
