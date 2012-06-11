
{mark} = require 'coala/mark'

exports.createService = mark('services', 'coala/service').on (baseService, entityClass) ->

    service =
        entityClass: entityClass

        list: (entity, options) ->
            manager = baseService.createManager service.entityClass
            manager.findByExample entity, options

        get: (id) ->
            manager = baseService.createManager service.entityClass
            manager.find id

        create: mark('tx').on (entity) ->
            manager = baseService.createManager service.entityClass
            manager.save entity

        update: mark('tx').on (id, fn) ->
            manager = baseService.createManager service.entityClass
            entity = manager.find id
            fn entity, service
            manager.merge entity

        remove: mark('tx').on (id...) ->
            manager = baseService.createManager service.entityClass
            manager.removeById.apply manager, id
            if id.length is 1 then id[0] else id

    service
