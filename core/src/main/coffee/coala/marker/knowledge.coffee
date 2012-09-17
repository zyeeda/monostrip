###
# mark('knowledge', { sessionId: 123 }).on(function (ksession) { ... });
###

{tx} = require 'coala/tx'

exports.handler = (context, attributes, fn, args) ->
    knowledgeService = context.getBean 'knowledgeService'

    options = attributes[0] or {}
    sessionId = options.sessionId
    tx ->
        ksession = if sessionId then knowledgeService.getKnowledgeSession sessionId else knowledgeService.createKnowledgeSession()
        args.unshift ksession
        fn.apply null, args
