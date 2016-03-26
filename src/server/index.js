import path from 'path'

import koa from 'koa'
import PrettyError from 'pretty-error'

import config from '../config'
import logger from '../logger'

import loadModels from './load-models'
import loadOutlines from './load-outlines'

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

loadModels()
loadOutlines(app)

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

  logger.info('%s server is listening on port %d...', config.get('name'), config.get('port'))
})

export default app
