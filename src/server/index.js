import path from 'path'

import koa from 'koa'
import keys from 'lodash.keys'
import values from 'lodash.values'
import PrettyError from 'pretty-error'

import config from '../config'
import logger from '../logger'

import loadModels from './load-models'
import loadOutlines from './load-outlines'

loadModels()
loadOutlines()

const hooks = [
  'global-error-handler',
  'i18n',
  'body-parser',
  'etag',
  'router'
]

logger.debug(`sysPath = ${config.get('sysPath')}`)
logger.debug(`appPath = ${config.get('appPath')}`)

const app = koa()

hooks
  .map(hookName => path.resolve(__dirname, 'hooks', hookName))
  .map(fileName => require(fileName).default)
  .forEach(hook => hook(app))

app.listen(config.get('port'), (err) => {
  if (err) {
    const pretty = new PrettyError()
    logger.error(pretty.render(err))
    return
  }

  logger.info('server is listening on port %d...', config.get('port'))
})

export default app
