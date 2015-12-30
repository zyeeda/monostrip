const fontAwesomeConfig = require('./font-awesome.config.js')
const ExtractTextPlugin = require('extract-text-webpack-plugin')
fontAwesomeConfig.styleLoader = ExtractTextPlugin.extract('style', 'css!less')
module.exports = fontAwesomeConfig
