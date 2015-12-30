import PrettyError from 'pretty-error'

import config from '../config'
import createLogger from '../logger'
import createApplication from '../application'

import rootRouter from './routers'

const app = createApplication(config.api)

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
