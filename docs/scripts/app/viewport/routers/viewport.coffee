define [
    'require'
    'jquery'
    'backbone'
    'marked'
], (req, $, Backbone, marked) ->

    tocCache = {}
    contentCache = {}

    ViewportRouter = Backbone.Router.extend
        routes:
            'user-guide': 'showUserGuide'
            tutorials: 'showTutorials'
            api: 'showApi'

        showUserGuide: ->
            @_show '#userGuideTab', 'text!app/docs/user-guide/index.md'

        showTutorials: ->
            @_activeTab '#tutorialsTab'

        showApi: ->
            @_activeTab '#apiTab'

        _show: (id, url) ->
            @_activeTab id

            if contentCache[id]? and tocCache[id]?
                $('#center').empty().html contentCache[id]
                $('#west').empty().html tocCache[id]
                return

            me = @
            req [url], (content) ->
                tokens = marked.lexer content

                me._renderToc id, tokens
                html = marked.parser tokens
                $('#center').empty().html html

                contentCache[id] = html

        _activeTab: (id) ->
            $('li.active').removeClass 'active'
            $(id).addClass 'active'

        _renderToc: (id, tokens) ->
            div = $ '<div class="well sidebar-nav"></div>'
            ul = $('<ul class="nav nav-list"></ul>').appendTo div

            for token in tokens
                if token.type isnt 'heading'
                    continue

                if token.depth is 1
                    ul.append "<li class='nav-header'>#{token.text}</li>"
                else if token.depth is 2
                    ul.append "<li>#{token.text}</li>"

            $('#west').empty().append div

            tocCache[id] = div.html()

    ->
        new ViewportRouter()
