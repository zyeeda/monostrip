require.config({
    paths: {
        CoffeeScript: 'libs/coffee-script',

        cs: 'libs/require/plugins/cs',
        i18n: 'libs/require/plugins/i18n',
        order: 'libs/require/plugins/order',
        text: 'libs/require/plugins/text',

        jquery: 'libs/jquery/jquery',
        jqueryui: 'libs/jquery/ui',
        underscore: 'libs/lodash',
        backbone: 'libs/backbone',

        marked: 'libs/marked-amd',
        highlight: 'libs/highlight/highlight-amd'
    }
});

define([
   'jquery',
   'underscore',
   'marked',
   'highlight',

   'cs!app/main'
], function ($, _, marked, hljs, app) {
    marked.setOptions({
        gfm: true,
        pedantic: false,
        sanitize: true,
        highlight: function(code, lang) {
            if (_.has(hljs.LANGUAGES, lang)) {
                return hljs.highlight(lang, code).value;
            }
            return code;
        }
    });

    $(function() {
        app.start();
    });
});
