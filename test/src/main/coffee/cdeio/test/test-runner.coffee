test = require 'test'
term = require 'ringo/term'

{File} = java.io
exports.run = run

result =
    suites: 0
    modules: 0
    testsRun: 0
    passed: 0
    errors: 0
    failures: 0
    time: 0

mergeSummary = (summary) ->
    for name, value of summary
        result[name] += value

getIndent = (num) ->
    i = ''
    i += '----' for j in [0...num]
    i

i1 = getIndent 1
i2 = getIndent 2
i3 = getIndent 3
i4 = getIndent 4

writer =
    writeTestBegin: ->
        term.writeln ''
        term.writeln '================================================================================'
        term.writeln 'Running Ringo Testing'
        term.writeln '================================================================================'
        term.writeln ''
    writeTestFinish: ->
        term.writeln '================================================================================'
        term.writeln 'Ringo Testing finished'
        term.writeln '================================================================================'
        term.writeln '> Found', result.suites, 'suites,', result.modules, 'modules. Executed', result.testsRun, 'tests in', result.time, 'ms '
        term.writeln term.BOLD, '> Passed', result.passed + ';', 'Failed', result.failures + ';', 'Errors', result.errors + ';'
        term.writeln '================================================================================'
        term.writeln ''
    writeSuiteStart: (name) ->
        result.suites++
        term.writeln '====>', 'Running Suite:', name, '<===='
    writeSuiteEnd: (name) ->
        term.writeln ''
    writeFoundModules: (modules) ->
        term.writeln i1, 'found test modules:'
        term.writeln i2, name for name, value of modules
    writeModuleStart: (name) ->
        result.modules++
        term.writeln i2, 'Running Module:', name
    writeHeader: ->
    enterScope: (name) ->
    exitScope: (name) ->
    writeTestStart: (name) ->
        term.write i3, '+ Running', name, '... '
    writeTestPassed: (time) ->
        term.writeln term.BOLD, 'PASSED', term.RESET, '(' + time + ' ms)'

    writeTestFailed: (exception) ->
        term.writeln term.BOLD, term.WHITE, term.ONRED, 'FAILED ';
        exception.message.split(/\n/).forEach (line) =>
            term.writeln i4, term.BOLD, term.RED, line
        if exception.stackTrace != null
            exception.stackTrace.forEach (line) =>
                term.writeln i4, term.BOLD, line

    writeSummary: (summary) ->
        mergeSummary summary
        if summary.testsRun > 0
            term.write(i3, "Executed", summary.testsRun, "tests in", summary.time, "ms. ")
            term.writeln(term.BOLD, "Passed", summary.passed + ";", "Failed", summary.failures + ";", "Errors", summary.errors + ";")
        else
            term.writeln(i3, "No tests found")

runSuite = (root, name, repo, ctx) ->
    writer.writeSuiteStart(root + '/' + name)
    resources = repo.getResources(false)
    modules = {}
    beforeSuites = []
    afterSuites = []
    for resource in resources
        path = resource.getRelativePath()
        continue if path.substring(path.length - 3) isnt '.js'
        m = require path
        modules[path] = m
        beforeSuites.push m.beforeSuite if m.beforeSuite
        afterSuites.push m.afterSuite if m.afterSuite

    writer.writeFoundModules modules

    if beforeSuites.length > 0
        bs(ctx) for bs in beforeSuites

    for n, m of modules
        writer.writeModuleStart n
        m.beforeModule(ctx) if m.beforeModule
        test.run m, writer
        m.afterModule(ctx) if m.afterModule

    if afterSuites.length > 0
        as(ctx) for as in afterSuites

    writer.writeSuiteEnd(root + '/' + name)

    runSuite root, r.getRelativePath(), r, ctx for r in repo.getRepositories()

run = (ctx, engine, modules, jdbc) ->
    writer.writeTestBegin()

    repos = {}
    for m in modules
        repo = if new File(m).isAbsolute() then engine.findRepository(m, null) else engine.findRepository(m, null)
        repos[m] = repo if repo.exists()

    for name, repo of repos
        runSuite name, '', repo, jdbc: jdbc

    writer.writeTestFinish()

    if result.errors > 0 or result.failures > 0
        throw new Error('test failure.')
