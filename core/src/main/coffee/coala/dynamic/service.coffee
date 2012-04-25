
{mark} = require 'coala/annotation'

exports.createService = mark('services', 'coala/service').on (baseService, entityClass) ->

    entityClass: entityClass

    list: (entity, options) ->
        manager = baseService.createManager @entityClass
        manager.findByExample entity, options

    get: (id) ->
        manager = baseService.createManager @entityClass
        manager.find id

    create: mark('tx').on (entity) ->
        manager = baseService.createManager @entityClass
        manager.save entity

    update: mark('tx').on (id, fn) ->
        manager = baseService.createManager @entityClass
        entity = manager.find id
        fn entity, @
        manager.merge entity

    remove: mark('tx').on (id...) ->
        manager = baseService.createManager @entityClass
        manager.removeById.apply manager, id
