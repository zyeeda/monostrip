var koa = require('koa')
var webpack = require('webpack')
var PrettyError = require('pretty-error')

var webpackConfig = require('./webpack.config.dev')

var pretty = new PrettyError()

var host = webpackConfig.host
var port = webpackConfig.port

var serverConfig = {
  contentBase: 'http://' + host + ':' + port,
  quiet: true,
  noInfo: true,
  hot: true,
  inline: true,
  lazy: false,
  publicPath: webpackConfig.output.publicPath,
  headers: {'Access-Control-Allow-Origin': '*'},
  stats: {colors: true},
  historyApiFallback: true
}

var app = koa()
var compiler = webpack(webpackConfig)
app.use(require('koa-webpack-dev-middleware')(compiler, serverConfig))
app.use(require('koa-webpack-hot-middleware')(compiler))

app.listen(port, function (err) {
  if (err) {
    console.error(pretty.render(err))
    return
  }

  console.info('Webpack dev server listening on port %d', port)
})
