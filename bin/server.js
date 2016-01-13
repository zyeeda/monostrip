#!/usr/bin/env node --harmony

require('babel-core/register')

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

  require('../lib/server/server')
}

bootstrap()
