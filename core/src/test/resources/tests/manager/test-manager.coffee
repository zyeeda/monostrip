{createManager} = require 'coala/manager'
{mark} = require 'coala/mark'
{ok} = require 'assert'

{Simple} = com.zyeeda.framework.tests.entities

exports.beforeModule = (context) ->
    context.jdbc.execute "insert into test_simple(F_ID, F_NAME) values('11', 'simple1')"

exports.afterModule = (context) ->
    context.jdbc.execute 'delete from test_simple'

exports.testGet = mark('coala/test/open-em').on ->
    manager = createManager Simple
    simple = manager.find '11'
    ok simple isnt null
    ok simple.name is 'simple1'
