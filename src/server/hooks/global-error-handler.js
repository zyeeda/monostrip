import path from 'path'

import onerror from 'koa-onerror'

import logger from '../../logger'

export default (app) => {
  logger.info('loading hook %s...', path.basename(__filename, '.js'))

  onerror(app)
}
