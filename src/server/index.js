import path from 'path'

import koa from 'koa'
import PrettyError from 'pretty-error'

import config from '../config'
import logger from '../logger'

const hooks = [
  "global-error-handler",
  "i18n",
  "body-parser",
  "etag",
  "router"
]

const app = koa()

logger.info('start loading hooks...')

hooks
  .map(hookName => path.resolve(__dirname, 'hooks', hookName))
  .map(hookFileName => require(hookFileName).default)
  .forEach(hook => hook({app, config}))

logger.info('load hooks finished...')

app.listen(config.get('port'), (err) => {
  if (err) {
    const pretty = new PrettyError()
    logger.error(pretty.render(err))
    return
  }

  logger.info('%s server is listening on port %d...', config.get('name'), config.get('port'))
})

cdeio.app = app

export default app
