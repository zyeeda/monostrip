{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet
{EntityManager, EntityManagerFactory} = javax.persistence
{EntityManagerFactoryUtils} = org.springframework.orm.jpa
{Configuration} = org.hibernate.cfg
{coala} = require 'coala/config'
{type, objects} = require 'coala/util'
{createUtil} = require 'coala/manager-util'

entityMetaResolver = Context.getInstance(module).getBeanByClass(com.zyeeda.framework.web.scaffold.EntityMetaResolver)


context = Context.getInstance(module)
# parameter name is the name of EntityManagerFactory which is configed in spring context
getEntityManager = (name = false) ->
    emf = if name then context.getBean name else context.getBeanByClass EntityManagerFactory
    em = EntityManagerFactoryUtils.doGetTransactionalEntityManager emf, null
    throw new Error('can not find an EntityManager in current thread') unless em?
    em


exports.createManager = (entityClass, name) ->
    em = getEntityManager name

    mixin: (mixins) ->
        for name, value of mixins
            if type(value) is 'function'
                @[name] = value.bind @, em
        @

    find: (ids...) ->
        result = for id in ids
            throw new Error('id can not be null') unless id?
            em.find entityClass, id
        firstIfOnlyOne result

    getReference: (ids...) ->
        result = for id in ids
            throw new Error('id can not be null') unless id?
            em.getReference entityClass, id
        firstIfOnlyOne result

    merge: (entities...) ->
        result = for entity in entities
            throw new Error('entity can not be null') unless entity?
            em.merge entity
        firstIfOnlyOne result

    save: (entities...) ->
        result = for entity in entities
            throw new Error('entity can not be null') unless entity?
            em.persist entity
            entity
        firstIfOnlyOne result

    remove: (entities...) ->
        result = for entity in entities
            throw new Error('entity can not be null') unless entity?
            em.remove entity
            entity
        firstIfOnlyOne result

    removeById: (ids...) ->
        entities = for id in ids
            @find id
        @remove.apply @, entities

    contains: (entity) ->
        em.contains entity

    flush: ->
        em.flush()

    clear: ->
        em.clear()

    refresh: (entities...) ->
        result = for entity in entities
            throw new Error('entity can not be null') unless entity?
            em.refresh entity
        firstIfOnlyOne result

    createNamedQuery: (name) ->
        createQuery em, name

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
        builder = em.getCriteriaBuilder()
        query = builder.createQuery entityClass
        root = query.from entityClass;

        if option.orderBy
            orders = for order in option.orderBy
                builder[value] root.get property for property, value of order
            query.orderBy orders

        q = em.createQuery query

        pageInfo = getPageInfo option
        fillPageInfo q, pageInfo

        q.getResultList()

    findByExample: (example, option = {}) ->
        managerUtil = new createUtil em, entityClass
        meta = entityMetaResolver.resolveEntity entityClass
        path = meta.path
        path = path.replace /(^\/)|(\/$)/g, ''
        [paths..., name] = path.split '/'
        paths.push coala.scaffoldFolderName
        paths.push name
        path = paths.join '/'
        dsPath = coala.appPath + path
        dsFiles = ['.ds.hql', '.ds.sql', '.ds.sp', '.ds.js']
        if fs.exists dsPath + dsFiles[0]
            managerUtil.findByHql example, option, dsPath + dsFiles[0]
        else if fs.exists dsPath + dsFiles[1] 
            managerUtil.findBySql example, option, dsPath + dsFiles[1]
        else if fs.exists dsPath + dsFiles[2]
            managerUtil.findByProcedure example, option, dsPath + dsFiles[2]
        else if fs.exists dsPath + dsFiles[3]
            managerUtil.findByMethod example, option, path + dsFiles[3]
        else
            managerUtil.findByEntity example, option

    __noSuchMethod__: (name,args) ->
        throw new Error 'can only support one argument call' if args?.length > 1
        option = args[0]

        pageInfo = getPageInfo option

        query = createQuery em, name, option
        fillPageInfo query,pageInfo

        singleResult = 'singleResult' of option and option.singleResult
        delete option['singleResult'] if singleResult

        for paramName, value of option
            query.setParameter paramName, value if paramName isnt 'firstResult' and paramName isnt 'maxResults'

        if name.substring(0, 4) is 'find'
            if singleResult then query.getSingleResult() else query.getResultList()
        else
            query.executeUpdate()

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

if coala.development is true
    fs = require 'fs'
    {Configuration} = org.hibernate.cfg
    namedQueries = {}
    modifyRecord = []

    modified = ->
        times = (fs.lastModified name for name in coala.orms).map (date) -> date.getTime()
        if times.length != modifyRecord.length
            modifiyRecord = times
            true
        else
            result =  modifyRecord.every (value, i) -> value == times[i]
            modifyRecord = times
            !result

    loadOrms = ->
        config = new Configuration()
        config.addFile file for file in coala.orms
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
