import path from 'path'

import koa from 'koa'
import onerror from 'koa-onerror'
import locale from 'koa-locale'
import i18n from 'koa-i18n'
import logger from 'koa-bunyan-logger'
import bodyParser from 'koa-bodyparser'
import compress from 'koa-compress'
import fresh from 'koa-fresh'
import etag from 'koa-etag'

import createLogger from './logger'

export default (config) => {
  const app = koa()

  // global error handler
  onerror(app)

  // i18n
  locale(app)
  app.use(i18n(app, {
    directory: path.join(__dirname, '..', 'locales'),
    locales: ['en', 'zh-CN'],
    extension: '.json',
    modes: [
      'header',
      'tld',
      'subdomain',
      'url',
      'cookie',
      'query'
    ]
  }))

  // request logger
  app.use(logger(createLogger(config.key)))
  app.use(logger.requestIdContext())
  app.use(logger.requestLogger())
  app.on('error', (err, ctx) => {
    ctx.log.error(err)
  })

  app.use(bodyParser())
  app.use(compress())
  app.use(fresh())
  app.use(etag())

  return app
}
