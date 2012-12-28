{type} = require 'coala/util/type'

exports.join = (paths..., cleanStartAndEndSlash) ->
    if type(cleanStartAndEndSlash) is 'string'
        paths.push cleanStartAndEndSlash
        cleanStartAndEndSlash = false

    result = ''
    result += '/' + p for p in paths
    result = result.substring 1
    result = result.replace /(\/){2,3}/g, '/'
    result = result.replace /(^\/)|(\/$)/g, '' if cleanStartAndEndSlash
    result
