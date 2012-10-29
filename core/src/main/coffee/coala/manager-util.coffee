{Class, Boolean, String, Integer, Double} = java.lang
{ArrayList, Date} = java.util
{Example, Order, Projections, MatchMode, Restrictions} = org.hibernate.criterion
{DatetimeUtils} = com.zyeeda.framework.utils
fs = fs || require 'fs'

exports.createUtil = (em, entityClass) ->
    util =
        # this implementation use native hibernate session
        findByEntity: (example, option = {}) ->
            # ex = Example.create(example).excludeZeroes().enableLike(MatchMode.ANYWHERE)
            # criteria = em.getDelegate().createCriteria(entityClass).add ex
            criteria = em.getDelegate().createCriteria(entityClass)
            if option.restricts
                criteria = util.fillRestrict criteria, option.restricts, entityClass
            if option.fetchCount is true
                criteria.setProjection Projections.rowCount()
                criteria.list().get(0)
            else
                pageInfo = util.getPageInfo option
                util.fillPageInfo criteria, pageInfo
                if option.orderBy
                    for order in option.orderBy
                        criteria.addOrder Order[value] property for property, value of order
                criteria.list()

        findBySql: (example, option = {}, sqlPath) ->
            util.findByStatement example, option, sqlPath, 'sql'

        findByHql: (example, option = {}, hqlPath) ->
            util.findByStatement example, option, hqlPath, 'hql'

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
                    _value = new Boolean if restrict.value == '1' then true else false
                else
                    _value = restrict.value
                query.setParameter i + 1, _value
            _len = restricts.length
            pageInfo = util.getPageInfo option
            query.setParameter _len + 1, option.fetchCount
            query.setParameter _len + 2, pageInfo.firstResult
            query.setParameter _len + 3, pageInfo.maxResults
            list = query.resultList
            if option.fetchCount is true
                return list.size()
            results = []
            it = list.iterator()
            while it.hasNext()
                _next = it.next()
                obj = {}
                for f in option.configs.fields
                    obj[f.name] = _next[f.position]
                results.push obj
            results

        findByStatement: (example, option = {}, path, type) ->
            sql = ''
            if option.fetchCount is true
                sql = fs.read path + '.count'
            else
                sql = fs.read path
            sql = sql.replace /\s{2,}|\t|\r|\n/g, ' '
            fields = option.configs.fields
            orderBy = ''
            result = util.joinWhere fields, option.restricts
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
                if option.configs.resultClass? and !option.fetchCount
                    query = em.createQuery sql, Class.forName option.configs.resultClass
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
                            _value = new Boolean if restrict.value == '1' then true else false
                        else
                            _value = restrict.value
                        query.setParameter _next.name, _value
            if option.fetchCount is true
                query.resultList.get(0)
            else
                pageInfo = util.getPageInfo option
                util.fillPageInfo query, pageInfo
                list = query.resultList
                return list if option.configs.resultClass
                results = []
                it = list.iterator()
                while it.hasNext()
                    _next = it.next()
                    obj = {}
                    for f in fields
                        obj[f.name] = _next[f.position]
                    results.push obj
                results

        joinWhere: (fields, restricts) ->
            return where: where, restricts: restricts unless restricts
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
                        _restrict.type = f.type if f.type
                        if _operator == 'LIKE'
                            where += ' ' + f.alias + ' like :' + f.name + ' and'
                            _restrict.value = '%' + restrict.value + '%'
                        else if _operator == 'BETWEEN'
                            _value = restrict.value.split ','
                            _restrict.value = _value[0]
                            newRestrict =
                                name: 'end_' + restrict.name
                                value: _value[1]
                            newRestrict.type = f.type if f.type
                            _restricts.push newRestrict
                            where += ' ' + f.alias + ' between :' + f.name + ' and :' + 'end_' + f.name + ' and'
                        else if _operator == 'IN'
                            _value = restrict.value.split ','
                            _restrict.value = _value.pop(0)
                            inStr = ':' + f.name
                            for r, i in _value
                                newRestrict =
                                    name: 'in_' + i + '_' + restrict.name
                                    value: r
                                newRestrict.type = f.type if f.type
                                _restricts.push newRestrict
                                inStr += ', :in_' + i + '_' + f.name
                            where += ' ' + f.alias + ' in (' + inStr + ') and'
                        else
                            _restrict.value = restrict.value
                            where += ' ' + f.alias + ' ' + operators[_operator] + ' :' + f.name + ' and'
                        _restricts.push _restrict
            where = where.substr 0, where.length - 4
            where: where
            restricts: _restricts

        fillRestrict: (criteria, restrictions, entityClass) ->
            for restrict in restrictions
                _value = undefined; _other = undefined
                if restrict.name and restrict.name.indexOf('.') isnt -1
                    if restrict.value
                        _value = restrict.value
                        _operator = 'eq'
                    else
                        _operator = 'isNull'
                else
                    _pname = restrict.name.charAt(0).toUpperCase() + restrict.name.substring(1)
                    try
                        _type = entityClass.getMethod('get' + _pname).returnType
                    catch e
                        try
                            _type = entityClass.getMethod('is' + _pname).returnType
                        catch ex
                            throw new Error "property #{restrict.name} is not found"
                    _operator = restrict.operator
                    if _type.equals(Date)
                        _tempVal = restrict.value.split ','
                        _value = DatetimeUtils.parseDate(_tempVal[0])
                        if _tempVal[1] then _other = DatetimeUtils.parseDate(_tempVal[1]) else _other = _value
                        _operator = _operator || 'between'
                    else if _type.equals(Boolean) or _type.toString().equals 'boolean'
                        _value = new Boolean if restrict.value == '1' then true else false
                        _operator = _operator || 'eq'
                    else if _type.equals(Integer) or _type.equals(Double) or _type.toString().equals('int') or _type.toString().equals('double')
                        _tempVal =  restrict.value.split ','
                        _value = _tempVal[0]
                        _other = _tempVal[1]
                        _operator = _operator || 'between'
                    else
                        _value = restrict.value if restrict.value
                        _other = restrict.other if restrict.other
                        _operator = _operator || 'like'

                if 'or' == _operator
                    junc = Restrictions.disjunction()
                    criteria = criteria.add fillRestrict junc, _value
                else if 'and' == _operator
                    junc = Restrictions.conjunction()
                    criteria = criteria.add fillRestrict junc, _value
                else if 'not' == _operator
                    criteria = util.fillRestrict criteria, _value
                else if 'in' == _operator
                    _value = new String(restrict.value).split ','
                    criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name, _value
                else if 'like' == _operator or 'ilike' == _operator
                    criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name, _value, MatchMode['ANYWHERE']
                else
                    if _value isnt undefined and _other isnt undefined
                        criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name, _value, _other
                    else if _value isnt undefined
                        criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name, _value
                    else
                        criteria = criteria.add Restrictions[_operator].call Restrictions, restrict.name
            criteria

        getPageInfo: (object) ->
            result =
                firstResult: 0
                maxResults: 0

            pageable = object?.hasOwnProperty('firstResult') and object.hasOwnProperty('maxResults')
            return null if not pageable

            for name of result
                result[name] = object[name]
            result


        fillPageInfo: (query,pageInfo) ->
            if pageInfo?
                query.setFirstResult pageInfo.firstResult
                query.setMaxResults pageInfo.maxResults
                return true
            false
    util
