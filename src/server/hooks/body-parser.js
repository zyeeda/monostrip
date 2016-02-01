import path from 'path'

import bodyParser from 'koa-bodyparser'

import config from '../../config'
import logger from '../../logger'

export default (app) => {
  logger.info(`loading ${path.basename(__filename, '.js')} hook`)

  app.use(bodyParser(config.get('hooks:body-parser')))
}
