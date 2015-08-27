{Context} = com.zyeeda.cdeio.web.SpringAwareJsgiServlet
{EntityManager, EntityManagerFactory} = javax.persistence
{EntityManagerFactoryUtils} = org.springframework.orm.jpa
{Configuration} = org.hibernate.cfg
{Class, Boolean, String, Integer, Double} = java.lang
{ArrayList, Date, Collection} = java.util
{Example, Order, Projections, MatchMode, Restrictions} = org.hibernate.criterion
{ReflectionUtils, ClassUtils} = org.springframework.util
{FieldMeta} = com.zyeeda.cdeio.web.scaffold
{Search} = org.hibernate.search.jpa
{Authentication} = org.activiti.engine.impl.identity
{SecurityUtils} = org.apache.shiro

{cdeio} = require 'cdeio/config'
{type, typeOf} = require 'cdeio/util/type'
{createConverter} = require 'cdeio/scaffold/converter'
fs = require 'fs'

# Specify date and time format.
#
dateFormat = new java.text.SimpleDateFormat cdeio.dateFormat
timeFormat = new java.text.SimpleDateFormat cdeio.dateTimeFormat

context = Context.getInstance(module)

# Get contextual EntityManager.
#
# The parameter `emfName` is the id of EntityManagerFactory bean specified in
# Spring config file. If ommited, `EntityManagerFactory` will be used.
#
getEntityManager = (emfName) ->
    emfName = 'entityManagerFactory' if not emfName
    emf = context.getBean emfName
    em = EntityManagerFactoryUtils.doGetTransactionalEntityManager emf, null
    # @todo refine this error message
    throw new Error('can not find an EntityManager in current thread') unless em?
    em

# The exported function to create a manager.
#
exports.createManager = (entityClass, emfName) ->
    em = getEntityManager emfName

    # When define custom manager, mixin the base manager members to the custom.
    #
    mixin: (mixins) ->
        for name, value of mixins
            if typeOf(value) is 'function'
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

    # Get all entities without any filter.
    #
    # Some options can be passed:
    # * `firstResult` the offset of the first item
    # * `maxResults` how many items will be fetched
    # * `orderBy` the order-by infomation
    #
    # For example:
    # ```javascript
    # {
    #     firstResult: 0,
    #     maxResults: 10,
    #     orderBy: [
    #         {name: 'asc'},
    #         {age: 'desc'}
    #     ]
    # }
    # ```
    #
    getAll: (option = {}) ->
        #{ Change this to use JPA interfaces.
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

    # Get all entities by some example.
    #
    # The `option` parameter is the as function `getAll`.
    #
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

    # Get all entities by filters.
    #
    # Some options can be passed:
    # * `filters` the searching filter
    # * `firstResult` the offset of the first item
    # * `maxResults` how many items will be fetched
    # * `orderBy` the order-by infomation
    # * `fetchCount` whether to only fetch the count not all the entities, if
    # `fetchCount` is specified the `firstResult`, `maxResults` and `orderBy`
    # options will be omitted
    #
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

    # 查询流程数据
    findByEntity4Process: (option = {}) ->
        filters = option.filters or []
        orders = option.orderBy
        {sql, params} = generateSql4Process entityClass, option, filters, orders
        query = em.createQuery sql
        if not option.fetchCount
            pageInfo = getPageInfo option
            fillPageInfo query, pageInfo
        query.setParameter key, value for key, value of params
        if option.fetchCount then query.getSingleResult() else query.getResultList()

    # Get the Hibernate Search FullTextEntityManager.
    #
    getFullTextEntityManager: ->
        Search.getFullTextEntityManager em

    rebuildIndexes: ->
        @getFullTextEntityManager().createIndexer().startAndWait()

    # Full text search the keyword.
    #
    # The `fields` parameter represents which fields of the entity should be
    # searched, the `keyword` is the search target, `options` can be passed the
    # pagination info, including `firstResult` and `maxResults`.
    #
    # This function will return both the entities of current page and the whole
    # count.
    #
    fullTextSearch: (fields, keyword, options) ->
        clazz = entityClass
        fem = @getFullTextEntityManager()
        {firstResult, maxResults} = options
        paged = firstResult >= 0 and maxResults > 0

        qb = fem.getSearchFactory().buildQueryBuilder().forEntity(clazz).get()
        q = qb.keyword().onFields fields...
        query = q.matching(keyword).createQuery()
        jpaQuery = fem.createFullTextQuery(query, clazz)

        if paged
            jpaQuery.setFirstResult firstResult
            jpaQuery.setMaxResults maxResults

            resultCount: jpaQuery.getResultSize()
            results: jpaQuery.getResultList()
        else
            jpaQuery.getResultList()

    # If calling a manager with an undefined function, this one will be called.
    #
    # This function will try to execute a query as the same name of the function
    # call.
    #
    # For example, if calling `manager.getUserById` function, but it is not
    # defined, then the manager will try to execute a query named `getUserById`.
    # This named query should be declared in orm.xml files.
    #
    # This method can be passed two parameters:
    # * the first one is an object to fill the query parameter, the key is the
    # parameter name and the value is the parameter value;
    # * the second one can be an object, a boolean, a number or a string. If it
    # is an object, it represents the pagination info, including `firstResult`
    # and `maxResults` options. If it is boolean value `true`, it means this
    # query will do an execute-update, like `INSERT`, `UPDATE` and `DELETE`.
    # If it is number 1 or string `singleResult`, it means the query will return
    # a single result.
    #
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


# Get pagination info.
#
getPageInfo = (object) ->
    result =
        firstResult: 0
        maxResults: 0

    pageable = object?.hasOwnProperty('firstResult') and object.hasOwnProperty('maxResults')
    return null if not pageable

    for name of result
        result[name] = object[name]
    result


fillPageInfo = (query, pageInfo) ->
    if pageInfo?
        query.setFirstResult pageInfo.firstResult
        query.setMaxResults pageInfo.maxResults
        return true
    false

# When in development mode, reload orm.xml files.
#
if cdeio.development is true
    fs = require 'fs'
    {Configuration} = org.hibernate.cfg
    namedQueries = {}
    modifyRecord = []

    # Check if the orm files are modified.
    #
    modified = ->
        times = (fs.lastModified name for name in cdeio.orms).map (date) -> date.getTime()
        if times.length != modifyRecord.length
            modifiyRecord = times
            true
        else
            result = modifyRecord.every (value, i) -> value == times[i]
            modifyRecord = times
            !result

    # Reload orm files.
    #
    loadOrms = ->
        #{ This method will reload all orm files without caring about whether
        #{ being modified, so this should be changed to improve performance.
        #{
        config = new Configuration()
        config.addFile file for file in cdeio.orms
        config.buildMappings()

        namedQueries = {}

        queries = config.getNamedQueries()
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

# If the array contains only one item, then unwrap the array and return the
# item.
#
firstIfOnlyOne = (array) ->
    if array.length is 1 then array[0] else array

#{ This function should be replaced with a third party implementation.
#{
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

# 生成流程查询 hql
generateSql4Process = (entityClass, option, filters, orders) ->
    converter = createConverter()
    currentUser = getCurrentUser()
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

    if option.taskType is 'waiting'
        sql = if option.fetchCount then 'select count(ec) ' else 'select ec '
        sql += "from #{entityClass.name} ec, TaskEntity te, IdentityLinkEntity i "
        sql += " where ec.processDefinitionId = substring(te.processDefinitionId,1,length(ec.processDefinitionId)) "
        sql += " and ec.processInstanceId = te.processInstanceId "
        sql += " and te.id = i.taskId "
        sql += " and te.assignee is null "
        sql += " and i.type = 'candidate' "
        groupIds = option.groupIds.join ',' if option.groupIds.length isnt 0
        if groupIds
            sql += " and (i.userId='#{currentUser}' or (i.groupId in (#{groupIds}))) "
        else
            sql += " and i.userId='#{currentUser}' "

    else if option.taskType is 'doing'
        sql = if option.fetchCount then 'select count(ec) ' else 'select ec '
        sql += "from #{entityClass.name} ec, TaskEntity te"
        sql += " where ec.processDefinitionId = substring(te.processDefinitionId,1,length(ec.processDefinitionId)) "
        sql += " and ec.processInstanceId = te.processInstanceId "
        sql += " and te.assignee = '#{currentUser}' "
    else if option.taskType is 'done'
        sql = if option.fetchCount then 'select count(distinct ec) ' else 'select ec '
        sql += "from #{entityClass.name} ec, HistoricTaskInstanceEntity htie "
        sql += " where ec.processDefinitionId = substring(htie.processDefinitionId,1,length(ec.processDefinitionId)) "
        sql += " and htie.assignee='#{currentUser}' "
        sql += " and htie.deleteReason ='completed' "
    # taskType is 'none' , seatch all data
    else
        return generateSql entityClass, option.fetchCount, filters, orders

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

getCurrentUser = ->
    p = SecurityUtils.getSubject().getPrincipal()
    if Authentication.getAuthenticatedUserId()
        if p and Authentication.getAuthenticatedUserId() isnt p.getAccountName()
            currentUser = p.getAccountName()
            Authentication.setAuthenticatedUserId currentUser
        else
            currentUser = Authentication.getAuthenticatedUserId()
    else
        currentUser = p?.getAccountName() or 'tom'
        Authentication.setAuthenticatedUserId currentUser

    currentUser
