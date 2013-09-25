{Context} = com.zyeeda.coala.web.SpringAwareJsgiServlet
{EntityManager, EntityManagerFactory} = javax.persistence
{EntityManagerFactoryUtils} = org.springframework.orm.jpa
{Configuration} = org.hibernate.cfg
{Class, Boolean, String, Integer, Double} = java.lang
{ArrayList, Date, Collection} = java.util
{Example, Order, Projections, MatchMode, Restrictions} = org.hibernate.criterion
{ReflectionUtils, ClassUtils} = org.springframework.util
{FieldMeta} = com.zyeeda.coala.web.scaffold

{coala} = require 'coala/config'
{type} = require 'coala/util/type'
{createConverter} = require 'coala/scaffold/converter'
fs = require 'fs'

dateFormat = new java.text.SimpleDateFormat 'yyyy-MM-dd'
timeFormat = new java.text.SimpleDateFormat 'yyyy-MM-dd HH:mm:ss'

context = Context.getInstance(module)
# parameter name is the name of EntityManagerFactory which is configed in spring context

getEntityManager = (name) ->
    name = 'entityManagerFactory' if not name
    emf = context.getBean name
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
        root = query.from entityClass

        if option.orderBy
            orders = for order in option.orderBy
                builder[value] root.get property for property, value of order
            query.orderBy orders

        q = em.createQuery query

        pageInfo = getPageInfo option
        fillPageInfo q, pageInfo

        q.getResultList()

    findByExample: (example, option = {}) ->
        ex = Example.create(example).excludeZeroes().enableLike(MatchMode.ANYWHERE)
        criteria = em.getDelegate().createCriteria(entityClass).add ex
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

    findByEntity: (option = {}) ->
        filters = option.filters or []
        orders = option.orderBy
        {sql, params} = generateSql entityClass, option.fetchCount, filters, orders
        query = em.createQuery sql
        if not option.fetchCount
            pageInfo = getPageInfo option
            fillPageInfo query, pageInfo
        query.setParameter key, value for key, value of params
        if option.fetchCount then query.getSingleResult() else query.getResultList()

    __noSuchMethod__: (name, args) ->
        params = args[0] or {}
        pageInfo = args[1]

        if pageInfo is true
            executeUpdate = true
            pageInfo = null
        else if pageInfo is 1 or pageInfo is 'singleResult'
            singleResult = true
            pageInfo = null

        query = createQuery em, name
        fillPageInfo query, pageInfo

        for key, value of params
            query.setParameter key, value

        if executeUpdate
            query.executeUpdate()
        else
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

        queries = config.getNamedSQLQueries()
        i = queries.keySet().iterator()
        while i.hasNext()
            name = i.next()
            namedQueries[name] = queries.get(name).getQuery()

        namedQueries

    createQuery = (em, name) ->
        loadOrms() if modified()
        query = namedQueries[name]
        if query then em.createQuery query else em.createNamedQuery name
else
    createQuery = (em, name) ->
        em.createNamedQuery name

firstIfOnlyOne = (array) ->
    if array.length is 1 then array[0] else array

generateSql = (entityClass, isCount, filters, orders) ->
    converter = createConverter()
    ctx =
        id: 0, prefix: 't', params: {}, joins: {}
        check: (name) ->
            c = entityClass
            for n in name.split '.'
                c = ReflectionUtils.findField c, n
                throw new Error "field: #{name} is not defined in class #{entityClass}" if not c
                if ClassUtils.isAssignable Collection, c.type
                    ctx.join n
                    c = c.getGenericType().getActualTypeArguments()[0]
                else
                    c = c.type
            c
        convert: (name, value) ->
            type = @check name
            converter.convert value, new FieldMeta('', type, false, null)
        join: (name) ->
            @joins[name] = name
            return name
        wrap: (name) ->
            [first, others...] = name.split '.'
            if @joins[first] then name else "#{@prefix}.#{name}"
        flat: (name) ->
            flat = name.replace /\./g, '_'
            flat += '_' + (ctx.id++)

    sql = if isCount then 'select count(t) ' else 'select t '
    sql += "from #{entityClass.name} t "
    if filters.length isnt 0
        conditions = operators.process.apply operators, [ctx, 'and'].concat filters
        sql += "left join t.#{key} #{value} " for key, value of ctx.joins
        sql += 'where ' + conditions
    os = []
    if orders and not isCount
        for order in orders
            os.push "#{ctx.wrap key} #{value}" for key, value of order when ctx.check key
        sql += ' order by ' + os.join(',') if os.length > 0
    sql: sql
    params: ctx.params

operators =
    process: (ctx, op = 'eq', args...) ->
        op = op.toLowerCase()
        @[op].apply @, [ctx].concat args

    two: (op, ctx, name, value, options) ->
        flat = ctx.flat name
        ctx.params[flat] = ctx.convert name, value
        "#{ctx.wrap name} #{op} :#{flat}"
    eq: (args...) -> @two.apply @, ['='].concat args
    ne: (args...) -> @two.apply @, ['!='].concat args
    gt: (args...) -> @two.apply @, ['>'].concat args
    lt: (args...) -> @two.apply @, ['<'].concat args
    ge: (args...) -> @two.apply @, ['>='].concat args
    le: (args...) -> @two.apply @, ['<='].concat args

    twop: (op, ctx, first, second, options) ->
        ctx.check first
        ctx.check second
        flat1 = ctx.flat first
        flat2 = ctx.flat second
        "#{ctx.wrap first} #{op} #{ctx.wrap second}"
    eqp: (args...) -> @twop.apply @, ['='].concat args
    nep: (args...) -> @twop.apply @, ['!='].concat args
    gtp: (args...) -> @twop.apply @, ['>'].concat args
    ltp: (args...) -> @twop.apply @, ['<'].concat args
    gep: (args...) -> @twop.apply @, ['>='].concat args
    lep: (args...) -> @twop.apply @, ['<='].concat args

    like: (ctx, name, value = '', options = {}) ->
        v = if options.mode then (if options.mode is 'start' then value + '%' else '%' + value) else '%' + value + '%'
        @two.apply @, ['like', ctx, name, v, options]
    ilike: (ctx, name, value = '', options = {}) ->
        v = if options.mode then (if options.mode is 'start' then value + '%' else '%' + value) else '%' + value + '%'
        v = v.toLowerCase()
        type = ctx.check name
        flat = ctx.flat name
        ctx.params[flat] = v
        "lower(#{ctx.wrap name}) like :#{flat}"
    between: (ctx, name, start, end) ->
        flat = ctx.flat name
        if start and end
            ctx.params[flat + '_start'] = ctx.convert name, start
            ctx.params[flat + '_end'] = ctx.convert name, end
            "#{ctx.wrap name} between :#{flat}_start and :#{flat}_end"
        else if start and not end
            ctx.params[flat + '_start'] = ctx.convert name, start
            "#{ctx.wrap name} >= :#{flat}_start"
        else if not start and end
            ctx.params[flat + '_end'] = ctx.convert name, end
            "#{ctx.wrap name} <= :#{flat}_end"
        else
            '1=1'
    null: (ctx, name, options) ->
        ctx.check name
        flat = ctx.flat name
        "#{ctx.wrap name} is null"
    notnull: (ctx, name, options) ->
        ctx.check name
        flat = ctx.flat name
        "#{ctx.wrap name} is not null"

    and: (ctx, args...) ->
        terms = (@process.apply @, [ctx].concat item for item in args)
        '(' + terms.join(' and ') + ')'
    or: (ctx, args...) ->
        terms = (@process.apply @, [ctx].concat item for item in args)
        '(' + terms.join(' or ') + ')'
