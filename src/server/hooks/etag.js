import path from 'path'

import fresh from 'koa-fresh'
import etag from 'koa-etag'

import logger from '../../logger'

export default (app) => {
  logger.info(`Setup ${path.basename(__filename, '.js')} hook.`)

  app.use(fresh())
  app.use(etag())
}
