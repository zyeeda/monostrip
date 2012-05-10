{exec} = require 'child_process'

task 'build', 'compile all server side coffees to js files', ->
    exec 'coffee -o core/src/main/resources/ -c core/src/main/coffee/', (err, stdout, stderr) ->
        console.log stdout + stderr

task 'build-to-drivebox', 'compile all server side coffees to js files', ->
    exec 'coffee -o ../zyeeda-drivebox/src/main/javascript/ -c core/src/main/coffee/', (err, stdout, stderr) ->
        console.log stdout + stderr

task 'watch', 'watch for server side coffee file changes', ->
    exec 'coffee -o core/src/main/resources/ -cw core/src/main/coffee/', (err, stdout, stderr) ->
        console.log err
        console.log stdout + stderr
