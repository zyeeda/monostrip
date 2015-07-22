{createRouter} = require 'cdeio/router'
{mark} = require 'cdeio/mark'
{stream} = require 'cdeio/response'

{IOUtils} = org.apache.commons.io
diagram = new org.activiti.image.impl.DefaultProcessDiagramGenerator()

log = require('ringo/logging').getLogger module.id
router = exports.router = createRouter()

###
    根据 processDefinitionKey 获取最新版本流程定义的流程图
###
router.get '/diagram/processDefinitionKey/:processDefinitionKey', mark('process').on (process, request, processDefinitionKey) ->
    processDefinition = process.repository.createProcessDefinitionQuery()
    .processDefinitionKey(processDefinitionKey)
    .latestVersion()
    .singleResult()
    bpmnModel = process.repository.getBpmnModel processDefinition.id

    imageStream = diagram.generatePngDiagram bpmnModel
    returnDiagram imageStream, request
###
    根据 processDefinitionId 获取流程图
###
router.get '/diagram/processDefinitionId/:processDefinitionId', mark('process').on (process, request, processDefinitionId) ->
    bpmnModel = process.repository.getBpmnModel processDefinitionId

    imageStream = diagram.generatePngDiagram bpmnModel
    returnDiagram imageStream, request

###
    根据 processInstanceId 获取流程图，如果流程未结束将高亮显示当前的活动节点。
###
router.get '/diagram/processInstanceId/:processInstanceId', mark('process').on (process, request, processInstanceId) ->
    historicProcessInstance = process.history.createHistoricProcessInstanceQuery()
    .processInstanceId(processInstanceId)
    .singleResult()

    bpmnModel = process.repository.getBpmnModel historicProcessInstance.processDefinitionId

    # 流程已结束
    if historicProcessInstance.endTime
        imageStream = diagram.generatePngDiagram bpmnModel
    else
        activeActivityIds = process.runtime.getActiveActivityIds processInstanceId
        imageStream = diagram.generateDiagram bpmnModel, 'png', activeActivityIds

    returnDiagram imageStream, request

# 返回 png 格式图片
returnDiagram = (inputStream, request) ->
    result =
        status: 200
        # headers: 'content-type:image/jpeg; charset=utf-8'
        headers: 'content-type:image/png'
        body: (outputStream) ->
            IOUtils.copy(inputStream, outputStream)
    stream request, result