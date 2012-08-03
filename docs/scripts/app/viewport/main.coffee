define [
    'cs!app/viewport/routers/viewport'
    'cs!app/viewport/views/viewport'
], (createViewportRouter, createViewportView) ->

    createViewportRouter()

    ->
        createViewportView()
