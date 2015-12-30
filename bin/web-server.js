#!/usr/bin/env node --harmony

const WebpackIsomorphicTools = require('webpack-isomorphic-tools')

// Enable ES6/7 features.
require('../transpiler')

// Define isomorphic constants.
global.__CLIENT__ = false
global.__DISABLE_SSR__ = false // disable server side rendering for debugging
global.__DEVELOPMENT__ = process.env.NODE_ENV !== 'production'

const bootstrap = () => {
  const rootDir = require('path').resolve(__dirname, '..')
  const options = {
    hook: true,
    ignore: /(\/\.|~$|\.json|\.scss$)/i
  }

  if (__DEVELOPMENT__) {
    if (!require('piping')(options)) {
      return
    }
  }

  global.webpackIsomorphicTools =
    new WebpackIsomorphicTools(require('../webpack/webpack-isomorphic-tools-config'))
    .development(__DEVELOPMENT__)
    .server(rootDir, () => {
      require('../lib/server/web/server')
    })
}

bootstrap()
