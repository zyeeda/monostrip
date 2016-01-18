import koa from 'koa'
import PrettyError from 'pretty-error'

import createLogger from '../logger'
import setup from './setup'

import rootRouter from '../routers'

import config from '../config'

const app = koa()

setup({app, config})

app.logger = createLogger(config.key)
app.use(rootRouter.routes())

app.listen(config.port, (err) => {
  if (err) {
    const pretty = new PrettyError()
    app.logger.error(pretty.render(err))
    return
  }

  app.logger.info('%s server is listening on port %d...', config.name, config.port)
})
