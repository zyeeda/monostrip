{Class, Boolean, String, Integer, Double} = java.lang
{ArrayList, Date} = java.util
{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet
{EntityManager, EntityManagerFactory} = javax.persistence
{EntityManagerFactoryUtils} = org.springframework.orm.jpa
{Example, Order, Projections, MatchMode, Restrictions} = org.hibernate.criterion
{Configuration} = org.hibernate.cfg
{DatetimeUtils} = com.zyeeda.framework.utils
{coala} = require 'coala/config'
{type, objects} = require 'coala/util'

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
            @findByHql.call @, example, option, dsPath + dsFiles[0]
        else if fs.exists dsPath + dsFiles[1] 
            @findBySql.call @, example, option, dsPath + dsFiles[1]
        else if fs.exists dsPath + dsFiles[2]
            @findByProcedure.call @, example, option, dsPath + dsFiles[2]
        else if fs.exists dsPath + dsFiles[3]
            @findByMethod.call @, example, option, path + dsFiles[3]
        else
            @findByEntity.call @, example, option

    # this implementation use native hibernate session
    findByEntity: (example, option = {}) ->
        # ex = Example.create(example).excludeZeroes().enableLike(MatchMode.ANYWHERE)
        # criteria = em.getDelegate().createCriteria(entityClass).add ex
        criteria = em.getDelegate().createCriteria(entityClass)
        if option.restricts
            criteria = fillRestrict criteria, option.restricts, entityClass
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

    findBySql: (example, option = {}, sqlPath) ->
        @findByQuery.call @, example, option, sqlPath, 'sql'

    findByHql: (example, option = {}, hqlPath) ->
        @findByQuery.call @, example, option, hqlPath, 'hql'

    findByMethod: (example, option = {}, jsPath) ->
        if option.fetchCount is true
            recordCount = require(jsPath).recordCount
            recordCount.call recordCount, example, option
        else
            results = require(jsPath).results
            results.call results, example, option

    findByProcedure: (example, option = {}, sqlPath) ->
        sql = fs.read path        
        sql = sql.replace /\s{2,}|\t|\r|\n/g, ' '
        query = em.createNativeQuery sql
        for restrict, i in restricts
            if restrict.type then _type = restrict.type.toUpperCase() else _type = ''
            if 'DATE' == _type
                _value = DatetimeUtils.parseDate restrict.value
            else if 'TIME' == _type
                _value = DatetimeUtils.parseDatetime restrict.value
            else if 'BOOLEAN' == _type
                _value = new Boolean restrict.value
            else
                _value = restrict.value
        query.setParameter i + 1, _value
        list = query.resultList
        results = []
        it = list.iterator()
        while it.hasNext() 
            _next = it.next()
            obj = {}
            for f in option.configs.fields
                obj[f.name] = _next[f.index]
            results.push obj
        results

    findByQuery: (example, option = {}, path, type) ->
        sql = fs.read path        
        sql = sql.replace /\s{2,}|\t|\r|\n/g, ' '
        fields = option.configs.fields
        orderBy = ''
        result = joinWhere fields, option.restricts
        where = result.where
        restricts = result.restricts
        if where then sql = sql.replace '{{where}}', 'where' + where else sql = sql.replace '{{where}}', 'where 0 = 0'
        if option.orderBy
            for order in option.orderBy
                for property, value of order 
                    for f in fields
                        if f.name == property
                            orderBy += ' ' + f.alias + ' ' + value + ','
        orderBy = orderBy.substr 0, orderBy.length - 1
        if orderBy then sql = sql.replace '{{orderBy}}', 'order by' + orderBy else sql = sql.replace '{{orderBy}}', ''
        if type == 'sql' 
            query = em.createNativeQuery sql
        else
            if v = option.configs.resultClass
                query = em.createQuery sql, Class.forName v
            else
                query = em.createQuery sql
        params = query.parameters
        it = params.iterator()
        while it.hasNext()
            _next = it.next()
            for restrict in restricts
                if _next.name == restrict.name
                    if restrict.type then _type = restrict.type.toUpperCase() else _type = ''
                    if 'DATE' == _type
                        _value = DatetimeUtils.parseDate restrict.value
                    else if 'TIME' == _type
                        _value = DatetimeUtils.parseDatetime restrict.value
                    else if 'BOOLEAN' == _type
                        _value = new Boolean restrict.value
                    else
                        _value = restrict.value
                    query.setParameter _next.name, _value
        if option.fetchCount is true
            query.resultList.size()
        else
            pageInfo = getPageInfo option
            fillPageInfo query, pageInfo
            list = query.resultList
            return list if option.configs.resultClass
            results = []
            it = list.iterator()
            while it.hasNext() 
                _next = it.next()
                obj = {}
                for f in fields
                    obj[f.name] = _next[f.index]
                results.push obj
            results

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

joinWhere = (fields, restricts) ->
    operators = 
        'EQ': '='
        'NE': '<>'
        'GT': '>'
        'LT': '<'
        'GE': '>='
        'LE': '<='
    where = ''
    _restricts = []
    for restrict in restricts
        _restrict = {}
        for f in fields
            if f.name == restrict.name
                if restrict.operator then _operator = restrict.operator.toUpperCase() else _operator = 'LIKE'              
                _restrict = 
                    name: restrict.name
                    operator: _operator
                _restrict.type = restrict.type if restrict.type
                if _operator == 'LIKE'
                    where += ' ' + f.alias + ' like :' + f.name + ' and'
                    _restrict.value = '%' + restrict.value + '%'
                else if _operator == 'BETWEEN'
                    _value = restrict.value.split ','
                    _restrict.value = _value[0]
                    newRestrict = 
                        name: 'end_' + restrict.name
                        value: _value[1]
                    newRestrict.type = restrict.type if restrict.type
                    _restricts.push newRestrict
                    where += ' ' + f.alias + ' between :' + f.name + ' and :' + 'end_' + f.name + ' and'
                else
                     where += ' ' + f.alias + ' ' + operators[_operator] + ' :' + f.name + ' and'
                _restricts.push _restrict
    where = where.substr 0, where.length - 4
    where: where
    restricts: _restricts

defaultRestrict = (criteria, restrict, entityClass) ->
    _type = entityClass.getMethod('get' + restrict.name.charAt(0).toUpperCase() + restrict.name.substring(1)).returnType
    return criteria unless _type?
    if _type.equals(Date)
        _tempVal = restrict.value.split ','
        _value = DatetimeUtils.parseDate(_tempVal[0])
        if _tempVal[1] then _other = DatetimeUtils.parseDate(_tempVal[1]) else _other = _value
        criteria = criteria.add Restrictions['between'].call Restrictions, restrict.name, _value, _other
    else if _type.equals(Boolean) or _type.toString().equals 'boolean'
        _value = new Boolean(restrict.value) if restrict.value
        criteria = criteria.add Restrictions['eq'].call Restrictions, restrict.name, _value
    else if _type.equals(Integer) or _type.equals(Double) or _type.toString().equals('int') or _type.toString().equals('double')
        _tempVal =  restrict.value.split ','
        _value = _tempVal[0]
        _other = _tempVal[1] 
        criteria = criteria.add Restrictions['between'].call Restrictions, restrict.name, _value, _other
    else if _type.equals String
        criteria = criteria.add Restrictions['like'].call Restrictions, restrict.name, restrict.value, MatchMode['ANYWHERE']
    else
        criteria = criteria.add Restrictions['eq'].call Restrictions, restrict.name, restrict.value
    criteria

fillRestrict = (criteria, restrictions, entityClass) ->
    for restrict in restrictions
        unless restrict.operator
            criteria = defaultRestrict criteria, restrict, entityClass
            continue
        _operator = restrict.operator
        if restrict.type
            _type = restrict.type.toUpperCase() 
            if 'DATE' == _type
                 _tempVal =  restrict.value.split ','
                if _tempVal[0]
                    _value = DatetimeUtils.parseDate(_tempVal[0])
                if _tempVal[1]
                    _other = DatetimeUtils.parseDate(_tempVal[1]) 
            else if 'TIME' == _type
                _tempVal =  restrict.value.split ','
                if _tempVal[0]
                    _value = DatetimeUtils.parseDatetime(_tempVal[0])
                if _tempVal[1]
                    _other = DatetimeUtils.parseDatetime(_tempVal[1]) 
            else if 'BOOLEAN' == _type
                _value = new Boolean restrict.value if restrict.value
                _other = new Boolean restrict.other if restrict.other
            else
                _value = restrict.value if restrict.value
                _other = restrict.other if restrict.other
        else
            _value = restrict.value if restrict.value
            _other = restrict.other if restrict.other
        if 'or' == _operator
            junc = Restrictions.disjunction()
            criteria = criteria.add fillRestrict junc, _value
        else if 'and' == _operator
            junc = Restrictions.conjunction()
            criteria = criteria.add fillRestrict junc, _value
        else if 'not' == _operator
            criteria = fillRestrict criteria, _value
        else if 'in' == _operator
            _value = new String(restrict.value).split ','
            criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name, _value
        else if 'like' == _operator or 'ilike' == _operator
            criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name, _value, MatchMode['ANYWHERE']
        else
            if _value and _other
                criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name, _value, otherVal
            else if restrict.value
                criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name, _value 
            else
                criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name
    criteria



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
