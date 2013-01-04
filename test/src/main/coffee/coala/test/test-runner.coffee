{File} = java.io
test = require 'test'

indent = '----'

exports.run = run

runSuite = (root, name, repo, ind) ->
    ind += indent
    console.log ind, 'Running Suite:', root + '/' + name
    resources = repo.getResources(false)
    console.log ind, 'found test modules:'
    modules = {}
    beforeSuites = []
    afterSuites = []
    for resource in resources
        console.log(ind + indent, resource.getRelativePath()) 
        m = require resource.getRelativePath()
        modules[resource.getRelativePath()] = m
        beforeSuites.push m.beforeSuite if m.beforeSuite
        afterSuites.push m.afterSuite if m.afterSuite

    if beforeSuites.length is 0
        console.log ind + indent, 'No beforeSuite found'
    else
        console.log ind + indent, 'Running beforeSuite'
        bs() for bs in beforeSuites

    for n, m of modules
        console.log ind + indent, 'Running Module:', n
        test.run m

    if afterSuites.length is 0
        console.log ind + indent, 'No afterSuite found'
    else
        console.log ind + indent, 'Running afterSuite'
        as() for as in afterSuites

    console.log ind, 'Suite:', root + '/' + name, 'finished'
    console.log ''

    runSuite root, r.getRelativePath(), r, '' for r in repo.getRepositories()

run = (ctx, engine, modules) ->
    console.log ''
    console.log '================================================================================'
    console.log 'Running Ringo Testing'
    console.log '================================================================================'
    console.log ''

    repos = {}
    for m in modules
        repo = if new File(m).isAbsolute() then engine.findRepository(m, null) else engine.findRepository('../' + m, null)
        repos[m] = repo if repo.exists()

    for name, repo of repos
        runSuite name, '', repo, ''

    console.log ''
    console.log '================================================================================'
    console.log 'Ringo Testing finished'
    console.log '================================================================================'
    console.log ''
