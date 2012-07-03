{objects} = require 'coala/util'
log = require('ringo/logging').getLogger module.id

projectLevelConfigure = try
    require 'config'
catch e
    {}

defaultConfigure =
    development: true,
    orms: ['src/main/resources/META-INF/orms/orm.xml'],
    scaffoldRoot: 'scaffold',
    defaultPageSize: 10,
    defaultOrder: 'asc',
    pageSizeKey: 'pageSize',
    dateFormat: 'yyyy-MM-dd'

exports.coala = objects.extend defaultConfigure, projectLevelConfigure.coala

log.debug "environment variable #{name}:#{value}" for name, value of exports.coala
