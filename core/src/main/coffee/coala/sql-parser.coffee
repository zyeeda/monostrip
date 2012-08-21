exports.SqlParser = ->
    parser =
        replaceSpace: (sql)->
            sql.replace /\s{2,}|\t|\r|\n/g, ' '

        trim: (sql) ->
            sql.replace /(^\s*)|(\s*$)/g, ''

        getSelectItems: (sql)->
            String.prototype.trim = ->
                this.replace /(^\s*)|(\s*$)/g, '' 

            regex = /(select)(.+)(from)/i   
            col = regex.exec sql
            throw new Error 'it\'s not a sql statement' unless col
            cols = col[2].trim().split ','
            results = []
            for c in cols
                tempCol = c.trim()
                if tempCol.indexOf('as') != -1
                    results.push tempCol.split("as")[1].trim()
                else if tempCol.indexOf(' ') != -1
                    results.push tempCol.substring(tempCol.lastIndexOf(' ') + 1, tempCol.length)
                else 
                    if tempCol.indexOf('.') != -1
                        results.push tempCol.split('.')[1]
                    else
                        results.push tempCol
            results

        subCond: (sql, nameParam) ->
            a = sql.split ' ' + nameParam
            b = a[0]
            c = b.substr 0, b.lastIndexOf(' ', b.lastIndexOf(' ') - 1)
            d = c.substr 0, c.lastIndexOf(' ')
            e = c.substr c.lastIndexOf(' ') + 1, c.length
            if e == 'where'
                d = d + ' where 0 = 0'
            sql = d + a[1]
            # delete a
            # delete b 
            # delete c
            # delete d 
            # delete e
            print sql
            return sql

    parser
