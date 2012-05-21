define [
    'underscore'
    'backbone'

    'cs!app/viewport/main'
], (_, Backbone, createViewport) ->

    app =
        vent: _.extend {}, Backbone.Events
        start: ->
            createViewport()

    app.vent.on 'viewport:show', ->
        history = Backbone.history
        if history?
            history.start()

    window.app = app
