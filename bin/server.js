#!/usr/bin/env node --harmony

// Enable ES6/7 features.
require('../transpiler')

global.__DEVELOPMENT__ = process.env.NODE_ENV !== 'production'

const bootstrap = () => {
  const options = {
    hook: true,
    ignore: /(\/\.|~$|\.json$)/i
  }

  if (__DEVELOPMENT__) {
    if (!require('piping')(options)) {
      return
    }
  }

  require('../lib/server/api/server')
}

bootstrap()
