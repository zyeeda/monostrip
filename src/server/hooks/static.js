import path from 'path'

import R from 'ramda'
import staticServe from 'koa-static'

export default ({app, config}) => {
  const options = {
    path: path.join(config.get('appPath'), 'static')
  }

  app.use(staticServe(R.merge(config.get('hooks:static'), options)))
}
