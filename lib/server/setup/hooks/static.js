import path from 'path'

import staticServe from 'koa-static'

export default (app) => {
  const options = {
    path: path.join(__dirname, '..', '..', '..', 'static')
  }

  app.use(staticServe(options.path))
}
