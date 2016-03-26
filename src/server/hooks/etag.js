import path from 'path'

import fresh from 'koa-fresh'
import etag from 'koa-etag'

import logger from '../../logger'

export default (app) => {
  logger.info('loading hook %s...', path.basename(__filename, '.js'))

  app.use(fresh())
  app.use(etag())
}
