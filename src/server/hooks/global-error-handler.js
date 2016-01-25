import path from 'path'

import onerror from 'koa-onerror'

import logger from '../../logger'

export default (app) => {
  logger.info(`Setup ${path.basename(__filename, '.js')} hook.`)

  onerror(app)
}