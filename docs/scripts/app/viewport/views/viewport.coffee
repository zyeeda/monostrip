define [
    'jquery'
    'backbone'

    'cs!libs/zui/layout'

    'text!app/viewport/templates/viewport.html'
], ($, Backbone, layout, tpl) ->

    ViewportView = Backbone.View.extend
        el : 'body'

        render : ->
            $el = $ @el

            $el.html tpl

            layout $el, {
                #defaults:
                    #resizable: true
                north:
                    spacing_open: 0
                    size: 43
                south:
                    closable: false
                    #resizable: false
                    spacing_open: 2
                west:
                    size: 280
            }

            window.app.vent.trigger 'viewport:show'

    ->
        new ViewportView().render()
