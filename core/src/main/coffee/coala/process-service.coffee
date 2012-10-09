{coala} = require 'coala/config'
{type} = require 'coala/util'
{KnowledgeBase} = org.drools
{KnowledgeService} = com.zyeeda.framework.knowledge
{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet
{HashMap} = java.util

context = Context.getInstance(module)
# parameter name is the name of KnowledgeBase which is configed in spring context
getKnowledgeBase = (name = false) ->
    kbase = if name then context.getBean name else context.getBeanByClass KnowledgeBase
    throw new Error('can not find an knowledgeBase in current thread') unless kbase?
    kbase

getKnowledgeService = (name = false) ->
    knowledgeService = if name then context.getBean name else context.getBeanByClass KnowledgeService
    throw new Error('can not find an knowledgeService in current thread') unless knowledgeService?
    knowledgeService

exports.createService = ->

    getProcessSession: (id) ->
        ksession = if id then getKnowledgeService().getProcessSession id else getKnowledgeService().getProcessSession()

        startProcess: (name, args) ->
            map = new HashMap()
            map.put key, value for key, value of args
            ksession.startProcess name, map

        __noSuchMethod__: (name, args) ->
            throw new Error("no such method: #{name}") if not ksession[name]
            ksession[name].apply ksession, args
