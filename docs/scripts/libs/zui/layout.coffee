_define = ($) ->
    proto =
        on: (event, func) ->
            parts = event.split '.'
            if parts.length is 2
                if func?
                    @unwrap().options[parts[0]]["on#{parts[1]}"] = func
                    return
                else
                    @unwrap().options[parts[0]]["on#{parts[1]}"]

        off: (event) ->
            parts = event.split '.'
            if parts.length is 2
                @unwrap().options[parts[0]]["on#{parts[1]}"] = null

        unwrap: ->
            if @_layout? then @_layout else @_layout = @$el.data 'layout'

    (selector, options) ->
        $el = $ selector
        layout = $el.data 'zui.layout'
        unless layout?
            layout = $el.layout options
            $el.data 'zui.layout', $.extend $el: $el, proto

        layout

if define? and define.amd?
    define [
        'jquery'
        'jqueryui/core'
        'jqueryui/draggable'
        'jqueryui/effects/core'
        'jqueryui/effects/slide'
        'jqueryui/effects/drop'
        'jqueryui/effects/scale'

        'order!libs/jquery/layout/jquery.layout'
    ], _define
else
    $.zui.layout = _define $
