import path from 'path'

import bodyParser from 'koa-bodyparser'

import config from '../../config'
import logger from '../../logger'

export default (app) => {
  logger.info('loading hook %s...', path.basename(__filename, '.js'))

  app.use(bodyParser(config.get('hooks:body-parser')))
}
