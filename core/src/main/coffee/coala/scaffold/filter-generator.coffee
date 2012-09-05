exports.generateFilters = (configs) ->
    filters = (f.name for f in configs.fields)
    _include = {}
    _include[configs.filterName] = filters
    defaults: 
        include: _include
