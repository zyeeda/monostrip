require('babel-core/polyfill')

var fs = require('fs')
var path = require('path')
var webpack = require('webpack')
var PrettyError = require('pretty-error')

var pretty = new PrettyError()

var host = process.env.HOST || 'localhost'
var port = parseInt(process.env.PORT) || 4200

var WebpackIsomorphicToolsPlugin = require('webpack-isomorphic-tools/plugin');
var webpackIsomorphicToolsPlugin = new WebpackIsomorphicToolsPlugin(require('./webpack-isomorphic-tools-config'))

var babelrcObject = {}
try {
  // The .babelrc file must be specified like this, not "../.babelrc".
  babelrcObject = JSON.parse(fs.readFileSync('./.babelrc'))
} catch (err) {
  console.error('Error parsing .babelrc.')
  console.error(pretty.render(err))
}

var babelrcObjectDevelopment = (babelrcObject.env && babelrcObject.env.development) || {}
var babelLoaderQuery = Object.assign({}, babelrcObject, babelrcObjectDevelopment)
delete babelLoaderQuery.env

babelLoaderQuery.plugins = babelLoaderQuery.plugins || []
if (babelLoaderQuery.plugins.indexOf('react-transform') < 0) babelLoaderQuery.plugins.push('react-transform')

babelLoaderQuery.extra = babelLoaderQuery.extra || {}
if (!babelLoaderQuery.extra['react-transform']) babelLoaderQuery.extra['react-transform'] = {}
if (!babelLoaderQuery.extra['react-transform'].transforms) babelLoaderQuery.extra['react-transforms'].transforms = []
babelLoaderQuery.extra['react-transform'].transforms.push({
  transform: 'react-transform-hmr',
  imports: ['react'],
  locals: ['module']
})

module.exports = {
  host: host,
  port: port,
  devtool: 'inline-source-map',
  context: path.resolve(__dirname, '..'),
  entry: {
    'main': [
      'webpack-hot-middleware/client?path=http://' + host + ':' + port + '/__webpack_hmr',
      'bootstrap-loader',
      'font-awesome-webpack!./lib/themes/default/font-awesome.config.js',
      './lib/client.js'
    ]
  },
  output: {
    path: path.resolve(__dirname, '../static/dist'), // webpack will output files to this path
    filename: '[name]-[hash:7].js', // the final output filename to the above path
    chunkFilename: '[name]-[chunkhash].js',
    publicPath: 'http://' + host + ':' + port + '/dist/' // the path from where the bundle will be served
  },
  module: {
    loaders: [{
      test: /bootstrap-sass\/assets\/javascripts\//,
      loader: 'imports?jQuery=jquery'
    }, {
      test: /\.js$/,
      exclude: /node_modules/,
      loaders: [
        'babel?' + JSON.stringify(babelLoaderQuery),
        'eslint-loader'
      ]
    }, {
      test: /\.json$/,
      loader: 'json'
    }, {
      test: /\.less$/,
      loaders: [
        'style',
        'css?modules&importLoaders=2&sourceMap&localIdentName=[local]___[hash:base64:5]',
        'autoprefixer?browsers=last 2 version',
        'less?outputStyle=expanded&sourceMap'
      ]
    }, {
      test: /\.scss$/,
      loaders: [
        'style',
        'css?modules&importLoaders=2&sourceMap&localIdentName=[local]___[hash:base64:5]',
        'autoprefixer?browsers=last 2 version',
        'sass?outputStyle=expanded&sourceMap'
      ]
    }, {
      test: /\.woff(2)?(\?v=\d+\.\d+\.\d+)?$/, 
      loader: 'url?limit=10000&mimetype=application/font-woff'
    }, {
      test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
      loader: 'url?limit=10000&mimetype=application/octet-stream'
    }, {
      test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
      loader: 'file'
    }, {
      test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
      loader: 'url?limit=10000&mimetype=image/svg+xml'
    }, {
      test: webpackIsomorphicToolsPlugin.regular_expression('images'),
      loader: 'url?limit=10240'
    }]
  },
  progress: true,
  resolve: {
    modulesDirectories: [
      'lib',
      'node_modules'
    ],
    extensions: ['', '.json', '.js']
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(), // hot reload
    //new webpack.optimize.CommonsChunkPlugin('vendor', 'vendor.bundle.js'),
    new webpack.IgnorePlugin(/webpack-stats\.json$/),
    new webpack.DefinePlugin({
      __CLIENT__: true,
      __DEVELOPMENT__: true,
      __DEVTOOLS__: true // can disable redux-devtools here
    }),
    webpackIsomorphicToolsPlugin.development()
  ]
}
