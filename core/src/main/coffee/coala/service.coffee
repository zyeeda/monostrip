{createManager} = require 'coala/manager'

exports.createService = ->

    createManager: (entityClass, entityManagerFactoryName = false) ->
        createManager entityClass, entityManagerFactoryName

    # invoke manager's __noSuchMethod__
    # args[0] is entity class, args[1] is query options
    __noSuchMethod__: (name,args) ->
        dao = @createManager args[0]
        dao[name] args[1]
