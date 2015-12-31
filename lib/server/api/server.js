import koa from 'koa'
import PrettyError from 'pretty-error'

import config from '../config'
import createLogger from '../logger'
import setup from '../setup'

import rootRouter from './routers'

const app = koa()
setup(app, require('./config.json'))

app.logger = createLogger(config.api.key)
app.use(rootRouter.routes())

app.listen(config.api.port, (err) => {
  if (err) {
    const pretty = new PrettyError()
    app.logger.error(pretty.render(err))
    return
  }

  app.logger.info('%s server is listening on port %d...', config.api.name, config.api.port)
})
