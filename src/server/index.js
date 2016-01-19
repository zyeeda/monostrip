import path from 'path'

import koa from 'koa'
import PrettyError from 'pretty-error'

import logger from '../logger'

import rootRouter from '../routers'

import config from '../config'

const hooks = [
  "global-error-handler",
  "i18n",
  "body-parser",
  "etag"
]

const app = koa()

hooks
  .map(hookName => path.resolve(__dirname, 'hooks', hookName))
  .map(hookFileName => require(hookFileName).default)
  .forEach(hook => hook({app, config}))

app.use(rootRouter.routes())

app.listen(config.port, (err) => {
  if (err) {
    const pretty = new PrettyError()
    logger.error(pretty.render(err))
    return
  }

  logger.info('%s server is listening on port %d...', config.name, config.port)
})
