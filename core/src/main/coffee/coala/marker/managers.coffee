# support mark 'managers'
# to inject entity-class-binded-manager
# attributes should be an entity class or an array contains many entity class
# ex. :
# mark('managers', [User, Organize], function(userManager, organizeManager){});

{createService} = require 'coala/service'
{type} = require 'coala/util'

exports.handler = (context, attributes, fn, args) ->
    if attributes
        service = createService()
        attr = if type(attributes) == 'array' then attributes else [attributes]
        managers = []

        managers.push service.createManager clazz for clazz in managers
        args = managers.concat args
    fn.apply null, args
