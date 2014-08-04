{createRouter} = require 'coala/router'
{mark} = require 'coala/mark'
{stream} = require 'coala/response'

{IOUtils} = org.apache.commons.io
diagram = org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator

log = require('ringo/logging').getLogger module.id
router = exports.router = createRouter()

router.get '/image/:processDefinitionKey', mark('process').on (process, request, processDefinitionKey) ->
    processDefinition = process.repository.createProcessDefinitionQuery()
    .processDefinitionKey(processDefinitionKey)
    .latestVersion()
    .singleResult()
    bpmnModel = process.repository.getBpmnModel processDefinition.id

    imageStream = diagram.generatePngDiagram bpmnModel
    result =
        status: 200
        # headers: 'content-type:image/jpeg; charset=utf-8'
        headers: 'content-type:image/png'
        body: (outStream) ->
            IOUtils.copy(imageStream, outStream)
    stream request, result


