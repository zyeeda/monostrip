import path from 'path'

import bodyParser from 'koa-bodyparser'

import logger from '../../logger'

export default (app) => {
  logger.info(`Setup ${path.basename(__filename, '.js')} hook.`)

  app.use(bodyParser())
}
