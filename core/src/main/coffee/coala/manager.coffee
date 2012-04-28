
{Example, Order, Projections} = org.hibernate.criterion
{Configuration} = org.hibernate.cfg;
{env} = require 'config'

exports.createManager = (em, entityClass) ->
    _em: em,
    _entityClass: entityClass,

    find: (ids...) ->
        result = for id in ids
            throw new Error('id can not be null') unless id?
            @_em.find @_entityClass, id
        firstIfOnlyOne result

    getReference: (ids...) ->
        result = for id in ids
            throw new Error('id can not be null') unless id?
            @_em.getReference @_entityClass, id
        firstIfOnlyOne result

    merge: (entities...) ->
        result = for entity in entities
            throw new Error('entity can not be null') unless entity?
            @_em.merge entity
        firstIfOnlyOne result

    save: (entities...) ->
        result = for entity in entities
            throw new Error('entity can not be null') unless entity?
            @_em.persist entity
            entity
        firstIfOnlyOne result

    remove: (entities...) ->
        result = for entity in entities
            throw new Error('entity can not be null') unless entity?
            @_em.remove entity
            entity
        firstIfOnlyOne result

    removeById: (ids...) ->
        entities = for id in ids
            @find id
        @remove.apply @, entities

    contains: (entity) ->
        @_em.contains entity

    flush: ->
        @_em.flush()

    refresh: (entities...) ->
        result = for entity in entities
            throw new Error('entity can not be null') unless entity?
            @_em.refresh entity
        firstIfOnlyOne result

    # option can be like this:
    # {
    #     firstResult: 0,
    #     maxResults: 10,
    #     orderBy: [
    #         {property1: 'asc'},
    #         {property2: 'desc'}
    #     ]
    # }
    getAll: (option = {}) ->
        builder = @_em.getCriteriaBuilder()
        query = builder.createQuery @_entityClass
        root = query.from @_entityClass;

        pageInfo = getPageInfo option
        fillPageInfo query, pageInfo

        if option.orderBy
            orders = for order in option.orderBy
                builder[value] root.get property for property, value of order
            query.orderBy orders

        @_em.createQuery(query).getResultList()

    # this implementation use native hibernate session
    findByExample: (example, option = {}) ->
        ex = Example.create(example).excludeZeroes()
        criteria = @_em.getDelegate().createCriteria(@_entityClass).add ex
        if option.fetchCount is true
            criteria.setProjection Projections.rowCount()
            criteria.list().get(0)
        else
            pageInfo = getPageInfo option
            fillPageInfo criteria, pageInfo

            if option.orderBy
                for order in option.orderBy
                    criteria.addOrder Order[value] property for property, value of order
            criteria.list()

    __noSuchMethod__: (name,args) ->
        throw new Error 'can only support one argument call' if args?.length > 1
        option = args[0]

        pageInfo = getPageInfo option

        query = createQuery @_em, name, option
        fillPageInfo query,pageInfo

        singleResult = 'singleResult' in option and option.singleResult
        delete option['singleResult'] if singleResult

        query.setParameter paramName, value for paramName, value of option
        if singleResult then query.getSingleResult() else query.getResultList()


getPageInfo = (object) ->
    result =
        firstResult: 0
        maxResults: 0

    pageable = object?.hasOwnProperty('firstResult') and object.hasOwnProperty('maxResults')
    return null if not pageable

    for name of result
        result[name] = object[name]
    result

fillPageInfo = (query,pageInfo) ->
    if pageInfo?
        query.setFirstResult pageInfo.firstResult
        query.setMaxResults pageInfo.maxResults
        return true
    false

if env.development is true
    fs = require 'fs'
    {Configuration} = org.hibernate.cfg
    namedQueries = {}
    modifyRecord = []

    modified = ->
        times = (fs.lastModified name for name in env.orms).map (date) -> date.getTime()
        if times.length != modifyRecord.length
            modifiyRecord = times
            true
        else
            result =  modifyRecord.every (value, i) -> value == times[i]
            modifyRecord = times
            !result

    loadOrms = ->
        config = new Configuration()
        config.addFile file for file in env.orms
        config.buildMappings()

        queries = config.getNamedQueries()
        namedQueries = {}
        i = queries.keySet().iterator()

        while i.hasNext()
            name = i.next()
            namedQueries[name] = queries.get(name).getQuery()
        namedQueries

    createQuery = (em, name, option) ->
        loadOrms() if modified()
        query = namedQueries[name]
        throw new Error("no query with name:#{name}") unless query?
        em.createQuery query
else
    createQuery = (em, name, option) ->
        em.createNamedQuery name

firstIfOnlyOne = (array) ->
    if array.length is 1 then array[0] else array
