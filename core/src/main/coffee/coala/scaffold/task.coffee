{createRouter} = require 'coala/router'
{mark} = require 'coala/mark'
{json} = require 'coala/response'
{generateForms} = require 'coala/scaffold/form-generator'

{TaskService} = com.zyeeda.framework.bpm
{EntityMetaResolver} = com.zyeeda.framework.web.scaffold

router = exports.router = createRouter()

router.get '/', mark('beans', TaskService).on (taskService, request) ->
    currentUser = ''
    json taskService.getTasks(currentUser)

router.get '/completed',  mark('beans', TaskService).on (taskService, request) ->
    currentUser = ''
    json taskService.getCompletedTasks(currentUser)

router.get '/comments/:taskId',  mark('beans', TaskService).on (taskService, request, taskId) ->
    json taskService.getComments(taskId)

router.get '/configuration/feature', ->
    json type: 'grid'

router.get '/configuration/forms/:taskId', mark('beans', [TaskService, EntityMetaResolver]).on (taskService, resolver, request, taskId) ->
    task = taskService.getTask taskId
    #find task related entity's class
    entityClass = ''
    entityMeta = resolver.resolveClass entityClass
    path = entityMeta.path

    #find scaffold configuration file and require it
    file = ''
    options = require file
    form = generateForms(entityMeta, options.labels, options.forms, task.name)

    json form
